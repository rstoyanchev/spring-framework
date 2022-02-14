/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;


/**
 * Expose HTTP error response details including a status, response headers, and
 * an RFC 7808 formatted {@link ProblemDetail} body.
 *
 * <p>Typically implemented by exceptions that encapsulate HTTP error response
 * details. Consider using the {@link ErrorResponseException} as a default
 * implementation or as a base class to create a more specific exception.
 *
 * @author Rossen Stoyanchev
 * @since 6.0
 * @see ErrorResponseException
 */
public interface ErrorResponse {

	/**
	 * Return the HTTP status for the error response.
	 * @throws IllegalArgumentException for an unknown HTTP status code
	 */
	default HttpStatus getStatus() {
		return HttpStatus.valueOf(getRawStatusCode());
	}

	/**
	 * Return the HTTP status value for the error response, potentially
	 * non-standard and not resolvable through the {@link HttpStatus} enum.
	 */
	int getRawStatusCode();

	/**
	 * Return headers to use for the response.
	 */
	default HttpHeaders getHeaders() {
		return HttpHeaders.EMPTY;
	}

	/**
	 * Return the RFC 7807 formatted, error response details, matching to the
	 * specific response {@link #getRawStatusCode() status}.
	 */
	ProblemDetail getBody();

}
