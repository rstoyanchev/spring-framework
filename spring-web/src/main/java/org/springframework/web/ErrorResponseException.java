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


import java.net.URI;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;


/**
 * {@link RuntimeException} that implements {@link ErrorResponse} to expose
 * an HTTP status, response headers, and an RFC 7808 formatted
 * {@link ProblemDetail} body.
 *
 * <p>The exception can be used as is or also extended as more specific
 * exceptions that pre-populate error response details.
 *
 * @author Rossen Stoyanchev
 * @since 6.0
 */
@SuppressWarnings("serial")
public class ErrorResponseException extends NestedRuntimeException implements ErrorResponse {

	private final int status;

	private final HttpHeaders headers = new HttpHeaders();

	@Nullable
	private ProblemDetail body;


	public ErrorResponseException(HttpStatus status) {
		this(status, null);
	}

	public ErrorResponseException(HttpStatus status, @Nullable Throwable cause) {
		this(status.value(), null);
	}

	public ErrorResponseException(int status, @Nullable Throwable cause) {
		super(null, cause);
		this.status = status;
	}


	/**
	 *
	 * @param type
	 */
	public void setType(@Nullable URI type) {
		this.body = initBody().withType(type);
	}

	/**
	 *
	 * @param title
	 */
	public void setTitle(@Nullable String title) {
		this.body = initBody().withTitle(title);
	}

	/**
	 *
	 * @param detail
	 */
	public void setDetail(@Nullable String detail) {
		this.body = initBody().withDetail(detail);
	}

	/**
	 *
	 * @param instance
	 */
	public void setInstance(@Nullable URI instance) {
		this.body = initBody().withInstance(instance);
	}

	private ProblemDetail initBody() {
		return (this.body != null ? this.body : ProblemDetail.forRawStatusCode(this.status));
	}

	@Override
	public int getRawStatusCode() {
		return this.status;
	}

	@Override
	public HttpHeaders getHeaders() {
		return this.headers;
	}

	@Override
	public ProblemDetail getBody() {
		this.body = initBody();
		return this.body;
	}

	@Override
	public String getMessage() {
		HttpStatus code = HttpStatus.resolve(this.status);
		String msg = (code != null ? code.toString() : String.valueOf(this.status));
		return NestedExceptionUtils.buildMessage(msg, getCause());
	}

}
