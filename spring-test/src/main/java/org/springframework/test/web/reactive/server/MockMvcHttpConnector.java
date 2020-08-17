/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.test.web.reactive.server;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import javax.servlet.http.Cookie;

import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.mock.http.client.reactive.MockClientHttpResponse;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;

/**
 *
 * @author Rossen Stoyanchev
 * @since 5.3
 */
public class MockMvcHttpConnector implements ClientHttpConnector {

	private final MockMvc mockMvc;


	public MockMvcHttpConnector(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}


	@Override
	public Mono<ClientHttpResponse> connect(
			HttpMethod method, URI uri, Function<? super ClientHttpRequest, Mono<Void>> requestCallback) {

		RequestBuilder requestBuilder = adaptRequest(method, uri, requestCallback);
		try {
			MvcResult mvcResult = this.mockMvc.perform(requestBuilder).andReturn();
			if (mvcResult.getRequest().isAsyncStarted()) {
				mvcResult.getAsyncResult(Duration.ofSeconds(5).toMillis());
				mvcResult = this.mockMvc.perform(asyncDispatch(mvcResult)).andReturn();
			}
			MockHttpServletResponse servletResponse = mvcResult.getResponse();
			MockClientHttpResponse clientResponse = adaptResponse(servletResponse);
			return Mono.just(clientResponse);
		}
		catch (Exception ex) {
			return Mono.error(ex);
		}
	}

	private RequestBuilder adaptRequest(
			HttpMethod method, URI uri, Function<? super ClientHttpRequest, Mono<Void>> requestCallback) {

		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.request(method, uri);
		MockClientHttpRequest httpRequest = new MockClientHttpRequest(method, uri);

		httpRequest.setWriteHandler(dataBuffers ->
				DataBufferUtils.join(dataBuffers)
						.doOnNext(buffer -> {
							byte[] content = new byte[buffer.readableByteCount()];
							buffer.read(content);
							DataBufferUtils.release(buffer);
							requestBuilder.content(content);
						})
						.then());

		requestCallback.apply(httpRequest).block();

		requestBuilder.headers(httpRequest.getHeaders());
		for (List<HttpCookie> cookies : httpRequest.getCookies().values()) {
			for (HttpCookie cookie : cookies) {
				requestBuilder.cookie(new Cookie(cookie.getName(), cookie.getValue()));
			}
		}

		return requestBuilder;
	}

	private MockClientHttpResponse adaptResponse(MockHttpServletResponse servletResponse) {
		MockClientHttpResponse clientResponse = new MockClientHttpResponse(servletResponse.getStatus());
		for (String header : servletResponse.getHeaderNames()) {
			for (String value : servletResponse.getHeaders(header)) {
				clientResponse.getHeaders().add(header, value);
			}
		}
		if (servletResponse.getForwardedUrl() != null) {
			clientResponse.getHeaders().add("Forwarded-Url", servletResponse.getForwardedUrl());
		}
		for (Cookie cookie : servletResponse.getCookies()) {
			ResponseCookie httpCookie =
					ResponseCookie.fromClientResponse(cookie.getName(), cookie.getValue())
							.maxAge(Duration.ofSeconds(cookie.getMaxAge()))
							.domain(cookie.getDomain())
							.path(cookie.getPath())
							.secure(cookie.getSecure())
							.httpOnly(cookie.isHttpOnly())
							.build();
			clientResponse.getCookies().add(httpCookie.getName(), httpCookie);
		}
		byte[] bytes = servletResponse.getContentAsByteArray();
		DefaultDataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(bytes);
		clientResponse.setBody(Mono.just(dataBuffer));
		return clientResponse;
	}

}
