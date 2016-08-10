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

import org.springframework.test.web.http.result.HttpStatusResultMatchers;

/**
 * Factory for assertions on the response status.
 * <p>An instance of this class is typically accessed via
 * {@link MockMvcResultMatchers#status}.
 *
 * @author Keesun Baik
 * @author Rossen Stoyanchev
 * @author Sebastien Deleuze
 * @author Brian Clozel
 * @since 3.2
 */
public class StatusResultMatchers extends HttpStatusResultMatchers {

	/**
	 * Protected constructor.
	 * Use {@link MockMvcResultMatchers#status()}.
	 */
	protected StatusResultMatchers() {
	}

}
