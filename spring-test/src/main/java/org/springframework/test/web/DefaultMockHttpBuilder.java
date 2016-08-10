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

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;
import org.springframework.util.Assert;

import com.thoughtworks.selenium.webdriven.commands.NoOp;

/**
 * 
 * @author Rob Winch
 * @since 5.0
 */
public class DefaultMockHttpBuilder {

	private List<Filter> filters = new ArrayList<>();
	
	private Servlet servlet = new NoOpServlet();

	private RequestBuilder defaultRequestBuilder;

	private final List<ResultMatcher> globalResultMatchers = new ArrayList<>();

	private final List<ResultHandler> globalResultHandlers = new ArrayList<>();

	private Boolean dispatchOptions = Boolean.TRUE;

	private final List<MockMvcConfigurer> configurers = new ArrayList<>(4);
	
	DefaultMockHttpBuilder() {}

	public final DefaultMockHttpBuilder servlet(Servlet servlet) {
		this.servlet = servlet;
		return this;
	}

	public final DefaultMockHttpBuilder addFilters(Filter... filters) {
		Assert.notNull(filters, "filters cannot be null");

		for (Filter f : filters) {
			Assert.notNull(f, "filters cannot contain null values");
			this.filters.add(f);
		}
		return this;
	}

	public final DefaultMockHttpBuilder addFilter(Filter filter, String... urlPatterns) {

		Assert.notNull(filter, "filter cannot be null");
		Assert.notNull(urlPatterns, "urlPatterns cannot be null");

		if (urlPatterns.length > 0) {
			filter = new PatternMappingFilterProxy(filter, urlPatterns);
		}

		this.filters.add(filter);
		return this;
	}

	public final DefaultMockHttpBuilder defaultRequest(RequestBuilder requestBuilder) {
		this.defaultRequestBuilder = requestBuilder;
		return this;
	}

	public final DefaultMockHttpBuilder alwaysExpect(ResultMatcher resultMatcher) {
		this.globalResultMatchers.add(resultMatcher);
		return this;
	}

	public final DefaultMockHttpBuilder alwaysDo(ResultHandler resultHandler) {
		this.globalResultHandlers.add(resultHandler);
		return this;
	}

	public final DefaultMockHttpBuilder dispatchOptions(boolean dispatchOptions) {
		this.dispatchOptions = dispatchOptions;
		return this;
	}
//
//	@SuppressWarnings("unchecked")
//	public final DefaultMockHttpBuilder apply(MockHttpConfigurer configurer) {
//		configurer.afterConfigurerAdded(this);
//		this.configurers.add(configurer);
//		return this;
//	}


	/**
	 * Build a {@link org.springframework.test.web.servlet.MockMvc} instance.
	 */
	public final MockHttp build() {
		ServletContext servletContext = new MockServletContext();

//		for (MockMvcConfigurer configurer : this.configurers) {
//			RequestPostProcessor processor = configurer.beforeMockMvcCreated(this, wac);
//			if (processor != null) {
//				if (this.defaultRequestBuilder == null) {
//					this.defaultRequestBuilder = MockMvcRequestBuilders.get("/");
//				}
//				if (this.defaultRequestBuilder instanceof ConfigurableSmartRequestBuilder) {
//					((ConfigurableSmartRequestBuilder) this.defaultRequestBuilder).with(processor);
//				}
//			}
//		}

		Filter[] filterArray = this.filters.toArray(new Filter[this.filters.size()]);

		MockFilterChain chain = new MockFilterChain(servlet, filterArray);
		
		return new MockHttp(chain, servletContext);
	}
}
