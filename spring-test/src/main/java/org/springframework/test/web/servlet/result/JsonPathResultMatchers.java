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

package org.springframework.test.web.servlet.result;

import org.hamcrest.Matcher;
import org.springframework.test.web.http.result.HttpJsonPathResultMatchers;

import com.jayway.jsonpath.JsonPath;

/**
 * Factory for assertions on the response content using
 * <a href="https://github.com/jayway/JsonPath">JsonPath</a> expressions.
 * <p>An instance of this class is typically accessed via
 * {@link MockMvcResultMatchers#jsonPath(String, Matcher)} or
 * {@link MockMvcResultMatchers#jsonPath(String, Object...)}.
 *
 * @author Rossen Stoyanchev
 * @author Craig Andrews
 * @author Sam Brannen
 * @author Brian Clozel
 * @since 3.2
 */
public class JsonPathResultMatchers  extends HttpJsonPathResultMatchers {

	/**
	 * Protected constructor.
	 * <p>Use {@link MockMvcResultMatchers#jsonPath(String, Object...)} or
	 * {@link MockMvcResultMatchers#jsonPath(String, Matcher)}.
	 * @param expression the {@link JsonPath} expression; never {@code null} or empty
	 * @param args arguments to parameterize the {@code JsonPath} expression with,
	 * using formatting specifiers defined in {@link String#format(String, Object...)}
	 */
	protected JsonPathResultMatchers(String expression, Object... args) {
		super(expression, args);
	}
}
