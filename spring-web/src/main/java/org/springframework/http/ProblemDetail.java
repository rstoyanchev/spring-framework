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
package org.springframework.http;

import java.net.URI;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Representation of an RFC 7807, HTTP error response that includes the
 * RFC-defined fields. For an extended response with more fields, create a
 * subclass that exposes those fields.
 *
 * @author Rossen Stoyanchev
 * @since 6.0
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7807">RFC 7807</a>
 * @see org.springframework.web.ErrorResponse
 * @see org.springframework.web.ErrorResponseException
 */
public class ProblemDetail {

	public static final URI BLANK_TYPE = URI.create("about:blank");


	private URI type = BLANK_TYPE;

	@Nullable
	private String title;

	private int status;

	@Nullable
	private String detail;

	@Nullable
	private URI instance;


	/**
	 * Protected constructor for subclasses.
	 * <p>To create a {@link ProblemDetail} instance, use static factory methods,
	 * {@link #forStatus(HttpStatus)} or {@link #forRawStatusCode(int)}.
	 * @param rawStatusCode the response status to use
	 */
	protected ProblemDetail(int rawStatusCode) {
		this.status = rawStatusCode;
	}


	/**
	 * Return the problem type.
	 * <p>By default, this is {@link #BLANK_TYPE}.
	 */
	public URI getType() {
		return this.type;
	}

	/**
	 * Return the problem title.
	 * <p>By default, if not explicitly set and the status is standard, this is
	 * sourced from the {@link HttpStatus#getReasonPhrase()}.
	 */
	@Nullable
	public String getTitle() {
		if (this.title == null) {
			HttpStatus httpStatus = HttpStatus.resolve(status);
			if (httpStatus != null) {
				return httpStatus.getReasonPhrase();
			}
		}
		return this.title;
	}

	/**
	 * Return the response status for the problem.
	 */
	public int getStatus() {
		return this.status;
	}

	/**
	 * Return the problem detail, or {@code null}.
	 */
	@Nullable
	public String getDetail() {
		return this.detail;
	}

	/**
	 * Return the problem instance, or {@code null}.
	 * <p>By default, when {@code ProblemDetail} is returned from an
	 * {@code @ExceptionHandler} method, this is initialized to the request path.
	 */
	@Nullable
	public URI getInstance() {
		return this.instance;
	}


	// Setters for deserialization

	/**
	 * Setter for the {@link #getType() problem type}.
	 * @param type the problem type
	 * @see #withType(URI)
	 */
	public void setType(URI type) {
		Assert.notNull(type, "'type' is required");
		this.type = type;
	}

	/**
	 * Setter for the {@link #getTitle() problem title}.
	 * @param title the problem title
	 * @see #withTitle(String)
	 */
	public void setTitle(@Nullable String title) {
		this.title = title;
	}

	/**
	 * Setter for the {@link #getStatus() problem status}.
	 * @param status the problem status
	 * @see #withStatus(HttpStatus)
	 * @see #withRawStatusCode(int)
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Setter for the {@link #getDetail() problem detail}.
	 * @param detail the problem detail
	 * @see #withDetail(String)
	 */
	public void setDetail(@Nullable String detail) {
		this.detail = detail;
	}

	/**
	 * Setter for the {@link #getInstance() problem instance}.
	 * @param instance the problem instance
	 * @see #withInstance(URI)
	 */
	public void setInstance(@Nullable URI instance) {
		this.instance = instance;
	}


	// Convenience methods for chained initialization

	/**
	 * Set the problem type.
	 * <p>By default, this is {@link #BLANK_TYPE}.
	 * @param type the problem type
	 * @return the same instance for chained initialization
	 */
	public ProblemDetail withType(URI type) {
		setType(type);
		return this;
	}

	/**
	 * Set the problem title.
	 * <p>By default, if not explicitly set and the status is standard, this is
	 * sourced from the {@link HttpStatus#getReasonPhrase()}.
	 * @param title the problem title
	 * @return the same instance for chained initialization
	 */
	public ProblemDetail withTitle(@Nullable String title) {
		setTitle(title);
		return this;
	}

	/**
	 * Set the response status code for the problem.
	 * @param status the response status code for the problem
	 * @return the same instance for chained initialization
	 */
	public ProblemDetail withStatus(HttpStatus status) {
		Assert.notNull(status, "HttpStatus is required");
		setStatus(status.value());
		return this;
	}

	/**
	 * Set the response status value for the problem.
	 * @param status the response status value for the problem
	 * @return the same instance for chained initialization
	 */
	public ProblemDetail withRawStatusCode(int status) {
		setStatus(status);
		return this;
	}

	/**
	 * Set the problem detail.
	 * @param detail the problem detail
	 * @return the same instance for chained initialization
	 */
	public ProblemDetail withDetail(@Nullable String detail) {
		setDetail(detail);
		return this;
	}

	/**
	 * Set the problem instance URI.
	 * <p>By default, if this isn't set, when {@code ProblemDetail} is returned
	 * from an {@code @ExceptionHandler} method, this is initialized to the
	 * request path.
	 * @param instance the problem instance URI
	 * @return the same instance for chained initialization
	 */
	public ProblemDetail withInstance(@Nullable URI instance) {
		setInstance(instance);
		return this;
	}


	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + initToStringContent() + "]";
	}

	/**
	 * Return a String representation of the {@code ProblemDetail} fields.
	 * Subclasses can override this to append extended fields.
	 */
	protected String initToStringContent() {
		return "type='" + this.type + "'" +
				", title='" + getTitle() + "'" +
				", status=" + getStatus() +
				", detail='" + getDetail() + "'" +
				", instance='" + getInstance() + "'";
	}


	// Static factory methods

	/**
	 * Create a {@code ProblemDetail} instance with the given status code.
	 */
	public static ProblemDetail forStatus(HttpStatus status) {
		Assert.notNull(status, "HttpStatus is required");
		return forRawStatusCode(status.value());
	}

	/**
	 * Create a {@code ProblemDetail} instance with the given status value.
	 */
	public static ProblemDetail forRawStatusCode(int status) {
		return new ProblemDetail(status);
	}

}
