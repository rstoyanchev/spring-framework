/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http.codec.protobuf;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.converter.protobuf.ExtensionRegistryInitializer;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;

/**
 * A {@code Decoder} that reads {@link com.google.protobuf.Message}s
 * using <a href="https://developers.google.com/protocol-buffers/">Google Protocol Buffers</a>.
 *
 * Streams deserialized via
 * {@link #decode(Publisher, ResolvableType, MimeType, Map)} are expected to use
 * <a href="https://developers.google.com/protocol-buffers/docs/techniques?hl=en#streaming">delimited Protobuf messages</a>
 * with the size of each message specified before the message itself. Single values deserialized
 * via {@link #decodeToMono(Publisher, ResolvableType, MimeType, Map)} are expected to use
 * regular Protobuf message format (without the size prepended before the message).
 *
 * <p>To generate {@code Message} Java classes, you need to install the {@code protoc} binary.
 *
 * <p>This decoder requires Protobuf 3 or higher, and supports
 * {@code "application/x-protobuf"} and {@code "application/octet-stream"} with the official
 * {@code "com.google.protobuf:protobuf-java"} library.
 *
 * @author Sébastien Deleuze
 * @since 5.1
 * @see ProtobufEncoder
 */
public class ProtobufDecoder extends ProtobufCodecSupport implements Decoder<Message> {

	private static final ConcurrentHashMap<Class<?>, Method> methodCache = new ConcurrentHashMap<>();

	private final ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();

	/**
	 * Construct a new {@code ProtobufDecoder}.
	 */
	public ProtobufDecoder() {
		this(null);
	}

	/**
	 * Construct a new {@code ProtobufDecoder} with an initializer that allows the
	 * registration of message extensions.
	 * @param registryInitializer an initializer for message extensions
	 */
	public ProtobufDecoder(@Nullable ExtensionRegistryInitializer registryInitializer) {
		if (registryInitializer != null) {
			registryInitializer.initializeExtensionRegistry(this.extensionRegistry);
		}
	}

	@Override
	public boolean canDecode(ResolvableType elementType, MimeType mimeType) {
		return Message.class.isAssignableFrom(elementType.getRawClass()) && supportsMimeType(mimeType);
	}

	@Override
	public Flux<Message> decode(Publisher<DataBuffer> inputStream, ResolvableType elementType,
			MimeType mimeType, Map<String, Object> hints) {
		return Flux
				.from(inputStream)
				.concatMap(new Function<DataBuffer, Publisher<? extends Message>>() {

			private DataBuffer output;
			private int messageBytesToRead;

			@Override
			public Publisher<? extends Message> apply(DataBuffer input) {

				try {
					if (output == null) {
						int firstByte = input.read();
						if (firstByte != -1) {
							messageBytesToRead = CodedInputStream.readRawVarint32(firstByte, input.asInputStream());
							output = input.factory().allocateBuffer(messageBytesToRead);
						}
					}
					int chunkBytesToRead = messageBytesToRead >= input.readableByteCount() ?
							input.readableByteCount() : messageBytesToRead;
					int remainingBytesToRead = input.readableByteCount() - chunkBytesToRead;
					output.write(input.slice(input.readPosition(), chunkBytesToRead));
					messageBytesToRead -= chunkBytesToRead;
					Message message = null;
					if (messageBytesToRead == 0) {
						Message.Builder builder = getMessageBuilder(elementType.getRawClass());
						builder.mergeFrom(CodedInputStream.newInstance(output.asByteBuffer()), extensionRegistry);
						message = builder.build();
						DataBufferUtils.release(output);
						output = null;
					}
					if (remainingBytesToRead > 0) {
						return Mono.justOrEmpty(message).concatWith(
								apply(input.slice(input.readPosition() + chunkBytesToRead, remainingBytesToRead)));
					} else {
						return Mono.justOrEmpty(message);
					}
				}
				catch (IOException ex) {
					throw new DecodingException("I/O error while parsing input stream", ex);
				}
				catch (Exception ex) {
					throw new DecodingException("Could not read Protobuf message: " + ex.getMessage(), ex);
				}
			}
		});
	}

	@Override
	public Mono<Message> decodeToMono(Publisher<DataBuffer> inputStream, ResolvableType elementType,
			MimeType mimeType, Map<String, Object> hints) {
		return DataBufferUtils.join(inputStream).map(dataBuffer -> {
					try {
						Message.Builder builder = getMessageBuilder(elementType.getRawClass());
						builder.mergeFrom(CodedInputStream.newInstance(dataBuffer.asByteBuffer()), this.extensionRegistry);
						Message message = builder.build();
						DataBufferUtils.release(dataBuffer);
						return message;
					}
					catch (IOException ex) {
						throw new DecodingException("I/O error while parsing input stream", ex);
					}
					catch (Exception ex) {
						throw new DecodingException("Could not read Protobuf message: " + ex.getMessage(), ex);
					}
				}
		);
	}

	/**
	 * Create a new {@code Message.Builder} instance for the given class.
	 * <p>This method uses a ConcurrentHashMap for caching method lookups.
	 */
	private static Message.Builder getMessageBuilder(Class<?> clazz) throws Exception {
		Method method = methodCache.get(clazz);
		if (method == null) {
			method = clazz.getMethod("newBuilder");
			methodCache.put(clazz, method);
		}
		return (Message.Builder) method.invoke(clazz);
	}

	@Override
	public List<MimeType> getDecodableMimeTypes() {
		return getMimeTypes();
	}




}
