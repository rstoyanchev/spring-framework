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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.springframework.test.web.http.result.MockHttpResultMatchers.*;
import static org.springframework.test.web.http.request.MockHttpRequestBuilders.*;
import static org.springframework.test.web.http.result.MockHttpResultHandlers.*;

/**
 *
 * @author rwinch
 * @since 5.0
 */
public class MockHttpTests {

	@Test
	public void hello() throws Exception {
		MyFilter filter = new MyFilter();

		MockHttp mockHttp = MockHttpBuilders
				.filtersSetup(filter)
				.build();

		mockHttp.perform(get("/"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(forwardedUrl(null))
			.andExpect(redirectedUrl(null))
			.andExpect(header().doesNotExist("missing"))
			.andExpect(content().string(""));
	}

	static class MyFilter extends OncePerRequestFilter {
		@Override
		protected void doFilterInternal(HttpServletRequest request,
				HttpServletResponse response, FilterChain filterChain)
						throws ServletException, IOException {
			filterChain.doFilter(request, response);
		}

	}
}
