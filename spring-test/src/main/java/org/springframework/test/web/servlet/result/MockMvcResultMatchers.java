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

import org.springframework.test.web.http.result.MockHttpResultMatchers;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * Static factory methods for {@link ResultMatcher}-based result actions.
 *
 * <h3>Eclipse Users</h3>
 * <p>Consider adding this class as a Java editor favorite. To navigate to
 * this setting, open the Preferences and type "favorites".
 *
 * @author Rossen Stoyanchev
 * @author Brian Clozel
 * @author Sam Brannen
 * @since 3.2
 */
public abstract class MockMvcResultMatchers extends MockHttpResultMatchers {

	/**
	 * Access to assertions for the handler that handled the request.
	 */
	public static HandlerResultMatchers handler() {
		return new HandlerResultMatchers();
	}

	/**
	 * Access to model-related assertions.
	 */
	public static ModelResultMatchers model() {
		return new ModelResultMatchers();
	}

	/**
	 * Access to assertions on the selected view.
	 */
	public static ViewResultMatchers view() {
		return new ViewResultMatchers();
	}

	/**
	 * Access to flash attribute assertions.
	 */
	public static FlashAttributeResultMatchers flash() {
		return new FlashAttributeResultMatchers();
	}
}
