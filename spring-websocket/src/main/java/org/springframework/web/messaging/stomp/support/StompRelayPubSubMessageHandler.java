/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.messaging.stomp.support;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.web.messaging.MessageType;
import org.springframework.web.messaging.PubSubChannelRegistry;
import org.springframework.web.messaging.PubSubHeaders;
import org.springframework.web.messaging.converter.CompositeMessageConverter;
import org.springframework.web.messaging.converter.MessageConverter;
import org.springframework.web.messaging.service.AbstractPubSubMessageHandler;
import org.springframework.web.messaging.stomp.StompCommand;
import org.springframework.web.messaging.stomp.StompHeaders;

import reactor.core.Environment;
import reactor.core.Promise;
import reactor.fn.Consumer;
import reactor.tcp.TcpClient;
import reactor.tcp.TcpConnection;
import reactor.tcp.encoding.DelimitedCodec;
import reactor.tcp.encoding.StandardCodecs;
import reactor.tcp.netty.NettyTcpClient;


/**
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class StompRelayPubSubMessageHandler extends AbstractPubSubMessageHandler {

	private MessageChannel<Message<?>> clientChannel;

	private final StompMessageConverter stompMessageConverter = new StompMessageConverter();

	private MessageConverter payloadConverter;

	private final TcpClient<String, String> tcpClient;

	private final Map<String, RelaySession> relaySessions = new ConcurrentHashMap<String, RelaySession>();


	/**
	 * @param clientChannel a channel for sending messages from the remote message broker
	 *        back to clients
	 */
	public StompRelayPubSubMessageHandler(PubSubChannelRegistry registry) {

		Assert.notNull(registry, "registry is required");
		this.clientChannel = registry.getClientOutputChannel();

		this.tcpClient = new TcpClient.Spec<String, String>(NettyTcpClient.class)
				.using(new Environment())
				.codec(new DelimitedCodec<String, String>((byte) 0, true, StandardCodecs.STRING_CODEC))
				.connect("127.0.0.1", 61613)
				.get();

		this.payloadConverter = new CompositeMessageConverter(null);
	}

	public void setMessageConverters(List<MessageConverter> converters) {
		this.payloadConverter = new CompositeMessageConverter(converters);
	}

	@Override
	protected Collection<MessageType> getSupportedMessageTypes() {
		return null;
	}

	@Override
	public void handleConnect(final Message<?> message) {

		final String sessionId = (String) message.getHeaders().get(PubSubHeaders.SESSION_ID);

		final RelaySession session = new RelaySession();
		relaySessions.put(sessionId, session);

		Promise<TcpConnection<String, String>> promise = this.tcpClient.open();

		promise.onSuccess(new Consumer<TcpConnection<String,String>>() {
			@Override
			public void accept(TcpConnection<String, String> connection) {
				session.setTcpConnection(connection);
				StompHeaders headers = StompHeaders.fromMessageHeaders(message.getHeaders());
				// TODO Configuration of relay Heartbeat
				headers.setHeartbeat(0,  0);

				forwardMessage(message, StompCommand.CONNECT, headers, connection);
			}
		});

		promise.consume(new Consumer<TcpConnection<String,String>>() {
			@Override
			public void accept(TcpConnection<String, String> connection) {
				connection.in().consume(new Consumer<String>() {
					@Override
					public void accept(String stompFrame) {
						if (stompFrame.isEmpty()) {
							// TODO: why are we getting empty frames?
							return;
						}
						Message<byte[]> message = stompMessageConverter.toMessage(stompFrame, sessionId);

						if (StompCommand.CONNECTED == StompHeaders.fromMessageHeaders(message.getHeaders()).getStompCommand()) {
							session.connected();
						} else {
							clientChannel.send(message);
						}
					}
				});
			}
		});

		// TODO: ATM no way to detect closed socket

//		StompHeaders stompHeaders = StompHeaders.create(StompCommand.ERROR);
//		stompHeaders.setMessage("Socket closed, STOMP session=" + sessionId);
//		stompHeaders.setSessionId(sessionId);
//		Message<byte[]> errorMessage = new GenericMessage<byte[]>(new byte[0], stompHeaders.toMessageHeaders());
//		getClientChannel().send(errorMessage);

	}

	private void forwardMessage(Message<?> message, StompCommand command) {
		StompHeaders headers = StompHeaders.fromMessageHeaders(message.getHeaders());
		String sessionId = headers.getSessionId();
		RelaySession session = relaySessions.get(sessionId);

		if (session != null) {
			forwardMessage(message, command, headers, session);
		} else {
			logger.warn("No relay session exists for " + sessionId + ". Message '" + message + "' cannot be forwarded");
		}
	}

	private void forwardMessage(Message<?> message, StompCommand command, StompHeaders headers, RelaySession session) {
		TcpConnection<String, String> connection = session.awaitTcpConnection();
		if (connection != null) {
			forwardMessage(message, command, headers, connection);
		} else {
			logger.warn("TCP connection await interrupted. Message " + message + " has not been forwarded");
		}
	}

	private void forwardMessage(Message<?> message, StompCommand command, StompHeaders headers, TcpConnection<String, String> connection) {
		if (logger.isTraceEnabled()) {
			logger.trace("Forwarding message " + message);
		}
		try {
			headers.setStompCommandIfNotSet(StompCommand.SEND);

			MediaType contentType = headers.getContentType();
			byte[] payload = this.payloadConverter.convertToPayload(message.getPayload(), contentType);
			Message<byte[]> byteMessage = MessageBuilder.fromPayloadAndHeaders(payload, headers.toMessageHeaders()).build();
			byte[] bytesToWrite = this.stompMessageConverter.fromMessage(byteMessage);

			connection.send(new String(bytesToWrite, Charset.forName("UTF-8")));

		}
		catch (Throwable ex) {
			logger.error("Failed to forward message " + message, ex);
			return;
		}
	}

	@Override
	public void handlePublish(Message<?> message) {
		forwardMessage(message, StompCommand.SEND);
	}

	@Override
	public void handleSubscribe(Message<?> message) {
		forwardMessage(message, StompCommand.SUBSCRIBE);
	}

	@Override
	public void handleUnsubscribe(Message<?> message) {
		forwardMessage(message, StompCommand.UNSUBSCRIBE);
	}

	@Override
	public void handleDisconnect(Message<?> message) {
		forwardMessage(message, StompCommand.DISCONNECT);
	}

	@Override
	public void handleOther(Message<?> message) {
		StompCommand command = (StompCommand) message.getHeaders().get(PubSubHeaders.PROTOCOL_MESSAGE_TYPE);
		Assert.notNull(command, "Expected STOMP command: " + message.getHeaders());
		forwardMessage(message, command);
	}

	// TODO:

/*	@Override
	public void handleClientConnectionClosed(String sessionId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Client connection closed for STOMP session=" + sessionId + ". Clearing relay session.");
		}
		clearRelaySession(sessionId);
	}
*/
	private static final class RelaySession {

		private final Object monitor = new Object();

		private boolean connected;

		private TcpConnection<String, String> tcpConnection;

		private void connected() {
			synchronized(monitor) {
				connected = true;
				if (fullyConnected()) {
					monitor.notifyAll();
				}
			}
		}

		private void setTcpConnection(TcpConnection<String, String> tcpConnection) {
			synchronized(monitor) {
				this.tcpConnection = tcpConnection;
				if (fullyConnected()) {
					monitor.notifyAll();
				}
			}
		}

		private TcpConnection<String, String> awaitTcpConnection() {
			synchronized(monitor) {
				while (!fullyConnected()) {
					try {
						monitor.wait();
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						// TODO Is returning null the right way to handle being interrupted?
						return null;
					}
				}
				return tcpConnection;
			}
		}

		private boolean fullyConnected() {
			synchronized(monitor) {
				return connected && tcpConnection != null;
			}
		}
	}
}
