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

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockServletContext;

/**
 *
 * @author Rob Winch
 * @since 5.0
 */
public class MockHttpBuilders {

	public static MockHttp filterChainSetup(MockFilterChain filterChain) {
		return new MockHttp(filterChain, new MockServletContext());
	}
	
	public static DefaultMockHttpBuilder servletSetup(Servlet servlet) {
		DefaultMockHttpBuilder builder = new DefaultMockHttpBuilder();
		return builder.servlet(servlet);
	}
	
	public static DefaultMockHttpBuilder filtersSetup(Filter... filters) {
		DefaultMockHttpBuilder builder = new DefaultMockHttpBuilder();
		return builder.addFilters(filters);
	}
	
	public static DefaultMockHttpBuilder filterSetup(Filter filter, String... urlPatterns) {
		DefaultMockHttpBuilder builder = new DefaultMockHttpBuilder();
		return builder.addFilter(filter, urlPatterns);
	}
}
