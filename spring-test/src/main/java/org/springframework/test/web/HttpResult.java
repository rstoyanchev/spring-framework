/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License; Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing; software
 * distributed under the License is distributed on an "AS IS" BASIS;
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND; either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.web;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Provides access to the result of an executed request.
 *
 * @author Rossen Stoyanchev
 * @since 3.2
 */
public interface HttpResult {

	/**
	 * Return the performed request.
	 * @return the request, never {@code null}
	 */
	MockHttpServletRequest getRequest();

	/**
	 * Return the resulting response.
	 * @return the response, never {@code null}
	 */
	MockHttpServletResponse getResponse();

	/**
	 * Get the result of async execution. This method will wait for the async result
	 * to be set for up to the amount of time configured on the async request,
	 * i.e. {@link org.springframework.mock.web.MockAsyncContext#getTimeout()}.
	 * @throws IllegalStateException if the async result was not set
	 */
	Object getAsyncResult();

	/**
	 * Get the result of async execution. This method will wait for the async result
	 * to be set for up to the specified amount of time.
	 * @param timeToWait how long to wait for the async result to be set, in
	 * 	milliseconds; if -1, then the async request timeout value is used,
	 *  i.e.{@link org.springframework.mock.web.MockAsyncContext#getTimeout()}.
	 * @throws IllegalStateException if the async result was not set
	 */
	Object getAsyncResult(long timeToWait);

}
