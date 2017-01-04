/*
 * Copyright 2002-2017 the original author or authors.
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
package org.springframework.test.web.servlet;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * An implementation of {@link MockMvcOperations} that makes actual HTTP calls
 * with a {@link RestTemplate} and also applies adaptation of
 * {@link RequestEntity} and {@link ResponseEntity} to
 * {@link MockHttpServletRequest} and {@link MockHttpServletResponse}.
 *
 * <p>This allows creating test fixtures with {@link MockMvcOperations} that
 * can be set up and executed either with a running server, through this class,
 * or without a running server via {@link MockMvc}.
 *
 * @author Rossen Stoyanchev
 */
public class RestTemplateEndpointTester implements MockMvcOperations {

	private final RestTemplate restTemplate;


	public RestTemplateEndpointTester(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}


	// TODO: default RequestBuilder

	// TODO: default ResultMatcher's and ResultHandler's


	@Override
	public ResultActions perform(RequestBuilder requestBuilder) throws Exception {

		MockHttpServletRequest request = requestBuilder.buildRequest(new MockServletContext());
		RequestEntity<?> requestEntity = adaptRequest(request);

		ResponseEntity<byte[]> responseEntity = this.restTemplate.exchange(requestEntity, byte[].class);
		MvcResult mvcResult = new EntityMvcResult(request, responseEntity);

		return new ResultActions() {

			@Override
			public ResultActions andExpect(ResultMatcher matcher) throws Exception {
				matcher.match(mvcResult);
				return this;
			}

			@Override
			public ResultActions andDo(ResultHandler handler) throws Exception {
				handler.handle(mvcResult);
				return this;
			}

			@Override
			public MvcResult andReturn() {
				return mvcResult;
			}
		};
	}

	private RequestEntity<?> adaptRequest(MockHttpServletRequest request) throws URISyntaxException {
		HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
		URI url = new URI(request.getRequestURL().toString());
		RequestEntity.BodyBuilder builder = RequestEntity.method(httpMethod, url);
		// TODO: init request...
		return builder.build();
	}



	private static class EntityMvcResult implements MvcResult {

		private final MockHttpServletRequest request;

		private final MockHttpServletResponse response;


		EntityMvcResult(MockHttpServletRequest request, ResponseEntity<byte[]> responseEntity) {
			this.request = request;
			this.response = adaptResponse(responseEntity);
		}

		private static MockHttpServletResponse adaptResponse(ResponseEntity<byte[]> responseEntity) {
			MockHttpServletResponse response = new MockHttpServletResponse();
			response.setStatus(responseEntity.getStatusCode().value());
			// TODO: init response...
			return response;
		}


		@Override
		public MockHttpServletRequest getRequest() {
			return this.request;
		}

		@Override
		public MockHttpServletResponse getResponse() {
			return this.response;
		}

		@Override
		public Object getHandler() {
			throw new UnsupportedOperationException("N/A");
		}

		@Override
		public HandlerInterceptor[] getInterceptors() {
			throw new UnsupportedOperationException("N/A");
		}

		@Override
		public ModelAndView getModelAndView() {
			throw new UnsupportedOperationException("N/A");
		}

		@Override
		public Exception getResolvedException() {
			throw new UnsupportedOperationException("N/A");
		}

		@Override
		public FlashMap getFlashMap() {
			throw new UnsupportedOperationException("N/A");
		}

		@Override
		public Object getAsyncResult() {
			throw new UnsupportedOperationException("N/A");
		}

		@Override
		public Object getAsyncResult(long timeToWait) {
			throw new UnsupportedOperationException("N/A");
		}

	}

}
