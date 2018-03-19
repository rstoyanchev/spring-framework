package org.springframework.http.codec.protobuf;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import org.springframework.core.ResolvableType;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.protobuf.ProtobufEncoder;
import org.springframework.lang.Nullable;

/**
 * {@code HttpMessageWriter} that can write a protobuf {@link Message} and adds
 * {@code X-Protobuf-Schema} and {@code X-Protobuf-Message} headers.
 *
 * <p>For {@code HttpMessageReader} just use {@code new DecoderHttpMessageReader(new ProtobufDecoder())}.
 *
 * TODO Should we use "X-Protobuf-Schema" and "X-Protobuf-Message" like Spring MVC, or customized Content-Type like https://www.charlesproxy.com/documentation/using-charles/protocol-buffers/ ?
 * TODO If we should use customized Content-Type, we need to think how can we specify "delimited=true" only for streams without breaking current API
 *
 * @author Sébastien Deleuze
 * @since 5.1
 * @see ProtobufEncoder
 */
public class ProtobufHttpMessageWriter extends EncoderHttpMessageWriter<Message> {

	private static final ConcurrentHashMap<Class<?>, Method> methodCache = new ConcurrentHashMap<>();

	private static final String X_PROTOBUF_SCHEMA_HEADER = "X-Protobuf-Schema";

	private static final String X_PROTOBUF_MESSAGE_HEADER = "X-Protobuf-Message";

	public ProtobufHttpMessageWriter() {
		super(new ProtobufEncoder());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Mono<Void> write(Publisher<? extends Message> inputStream, ResolvableType elementType,
			@Nullable MediaType mediaType, ReactiveHttpOutputMessage message, Map<String, Object> hints) {

		try {
			Message.Builder builder = getMessageBuilder(elementType.getRawClass());
			Descriptors.Descriptor descriptor = builder.getDescriptorForType();
			message.getHeaders().add(X_PROTOBUF_SCHEMA_HEADER, descriptor.getFile().getName());
			message.getHeaders().add(X_PROTOBUF_MESSAGE_HEADER, descriptor.getFullName());
			return super.write(inputStream, elementType, mediaType, message, hints);
		}
		catch (Exception ex) {
			return Mono.error(new DecodingException("Could not read Protobuf message: " + ex.getMessage(), ex));
		}
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

}
