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

package org.springframework.test.web.servlet.result;

import org.springframework.test.web.http.result.HttpRequestResultMatchers;

/**
 * Factory for assertions on the request.
 * <p>An instance of this class is typically accessed via
 * {@link MockMvcResultMatchers#request}.
 *
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @since 3.2
 */
public class RequestResultMatchers extends HttpRequestResultMatchers {

	/**
	 * Protected constructor.
	 * <p>Use {@link MockMvcResultMatchers#request()}.
	 */
	protected RequestResultMatchers() {
	}
}
