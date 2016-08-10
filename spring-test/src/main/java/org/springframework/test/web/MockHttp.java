/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.test.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import org.springframework.beans.Mergeable;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.SmartRequestBuilder;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 *
 * @author Rob Winch
 * @since 5.0
 */
public class MockHttp {
	private final MockFilterChain filterChain;

	private final ServletContext servletContext;

	private RequestBuilder defaultRequestBuilder;

	private List<HttpResultMatcher<HttpResult>> defaultResultMatchers = new ArrayList<>();

	private List<HttpResultHandler<HttpResult>> defaultResultHandlers = new ArrayList<>();

	MockHttp(MockFilterChain filterChain, ServletContext servletContext) {
		this.filterChain = filterChain;
		this.servletContext = servletContext;
	}



	/**
	 * A default request builder merged into every performed request.
	 * @see org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder#defaultRequest(RequestBuilder)
	 */
	void setDefaultRequest(RequestBuilder requestBuilder) {
		this.defaultRequestBuilder = requestBuilder;
	}

	/**
	 * Expectations to assert after every performed request.
	 * @see org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder#alwaysExpect(ResultMatcher)
	 */
	void setGlobalResultMatchers(List<HttpResultMatcher<HttpResult>> resultMatchers) {
		Assert.notNull(resultMatchers, "resultMatchers is required");
		this.defaultResultMatchers = resultMatchers;
	}

	/**
	 * General actions to apply after every performed request.
	 * @see org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder#alwaysDo(ResultHandler)
	 */
	void setGlobalResultHandlers(List<HttpResultHandler<HttpResult>> resultHandlers) {
		Assert.notNull(resultHandlers, "resultHandlers is required");
		this.defaultResultHandlers = resultHandlers;
	}

	/**
	 * Perform a request and return a type that allows chaining further
	 * actions, such as asserting expectations, on the result.
	 *
	 * @param requestBuilder used to prepare the request to execute;
	 * see static factory methods in
	 * {@link org.springframework.test.web.servlet.request.MockMvcRequestBuilders}
	 *
	 * @return an instance of {@link ResultActions}; never {@code null}
	 *
	 * @see org.springframework.test.web.servlet.request.MockMvcRequestBuilders
	 * @see org.springframework.test.web.servlet.result.MockMvcResultMatchers
	 */
	public HttpResultActions perform(RequestBuilder requestBuilder) throws Exception {
		filterChain.reset();

		if (this.defaultRequestBuilder != null) {
			if (requestBuilder instanceof Mergeable) {
				requestBuilder = (RequestBuilder) ((Mergeable) requestBuilder).merge(this.defaultRequestBuilder);
			}
		}

		MockHttpServletRequest request = requestBuilder.buildRequest(this.servletContext);
		MockHttpServletResponse response = new MockHttpServletResponse();

		if (requestBuilder instanceof SmartRequestBuilder) {
			request = ((SmartRequestBuilder) requestBuilder).postProcessRequest(request);
		}

		final HttpResult httpResult = new DefaultHttpResult(request, response);

		RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

		filterChain.doFilter(request, response);

		if (DispatcherType.ASYNC.equals(request.getDispatcherType()) &&
				request.getAsyncContext() != null & !request.isAsyncStarted()) {

			request.getAsyncContext().complete();
		}

		applyDefaultResultActions(httpResult);

		RequestContextHolder.setRequestAttributes(previousAttributes);

		return new HttpResultActions() {

			@Override
			public HttpResultActions andExpect(HttpResultMatcher<HttpResult> matcher) throws Exception {
				matcher.match(httpResult);
				return this;
			}

			@Override
			public HttpResultActions andDo(HttpResultHandler<HttpResult> handler) throws Exception {
				handler.handle(httpResult);
				return this;
			}

			@Override
			public HttpResult andReturn() {
				return httpResult;
			}
		};
	}

	private void applyDefaultResultActions(HttpResult mvcResult) throws Exception {

		for (HttpResultMatcher<HttpResult> matcher : this.defaultResultMatchers) {
			matcher.match(mvcResult);
		}

		for (HttpResultHandler<HttpResult> handler : this.defaultResultHandlers) {
			handler.handle(mvcResult);
		}
	}
}
