/*
 * Copyright 2002-2015 the original author or authors.
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

/**
 * Allows applying actions, such as expectations, on the result of an executed
 * request.
 *
 * <p>See static factory methods in
 * {@link org.springframework.test.web.servlet.result.MockMvcResultMatchers} and
 * {@link org.springframework.test.web.servlet.result.MockMvcResultHandlers}.
 *
 * @author Rossen Stoyanchev
 * @since 3.2
 */
public interface HttpResultActions {

	/**
	 * Perform an expectation.
	 *
	 * <h4>Example</h4>
	 * <pre class="code">
	 * static imports: MockMvcRequestBuilders.*, MockMvcResultMatchers.*
	 *
	 * mockMvc.perform(get("/person/1"))
	 *   .andExpect(status().isOk())
	 *   .andExpect(content().contentType(MediaType.APPLICATION_JSON))
	 *   .andExpect(jsonPath("$.person.name").value("Jason"));
	 *
	 * mockMvc.perform(post("/form"))
	 *   .andExpect(status().isOk())
	 *   .andExpect(redirectedUrl("/person/1"))
	 *   .andExpect(model().size(1))
	 *   .andExpect(model().attributeExists("person"))
	 *   .andExpect(flash().attributeCount(1))
	 *   .andExpect(flash().attribute("message", "success!"));
	 * </pre>
	 */
	HttpResultActions andExpect(HttpResultMatcher<HttpResult> matcher) throws Exception;

	/**
	 * Perform a general action.
	 *
	 * <h4>Example</h4>
	 * <pre class="code">
	 * static imports: MockMvcRequestBuilders.*, MockMvcResultMatchers.*
	 *
	 * mockMvc.perform(get("/form")).andDo(print());
	 * </pre>
	 */
	HttpResultActions andDo(HttpResultHandler<HttpResult> handler) throws Exception;

	/**
	 * Return the result of the executed request for direct access to the results.
	 *
	 * @return the result of the request
	 */
	HttpResult andReturn();

}
