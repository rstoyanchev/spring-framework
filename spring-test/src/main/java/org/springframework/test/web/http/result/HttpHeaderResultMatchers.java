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

package org.springframework.test.web.http.result;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.hamcrest.Matcher;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.HttpResult;
import org.springframework.test.web.HttpResultMatcher;

import static org.hamcrest.MatcherAssert.*;
import static org.springframework.test.util.AssertionErrors.*;

/**
 * Factory for response header assertions.
 * <p>An instance of this class is available via
 * {@link MockMvcHttpResultMatcher<HttpResult>s#header}.
 *
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @author Brian Clozel
 * @since 3.2
 */
public class HttpHeaderResultMatchers {


	/**
	 * Protected constructor.
	 * See {@link MockMvcHttpResultMatcher<HttpResult>s#header()}.
	 */
	protected HttpHeaderResultMatchers() {
	}


	/**
	 * Assert the primary value of the response header with the given Hamcrest
	 * String {@code Matcher}.
	 */
	public HttpResultMatcher<HttpResult> string(final String name, final Matcher<? super String> matcher) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				assertThat("Response header " + name, result.getResponse().getHeader(name), matcher);
			}
		};
	}

	/**
	 * Assert the values of the response header with the given Hamcrest
	 * Iterable {@link Matcher}.
	 * @since 4.3
	 */
	public <T> HttpResultMatcher<HttpResult> stringValues(final String name, final Matcher<Iterable<String>> matcher) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				List<String> values = result.getResponse().getHeaders(name);
				assertThat("Response header " + name, values, matcher);
			}
		};
	}

	/**
	 * Assert the primary value of the response header as a String value.
	 */
	public HttpResultMatcher<HttpResult> string(final String name, final String value) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				assertEquals("Response header " + name, value, result.getResponse().getHeader(name));
			}
		};
	}

	/**
	 * Assert the values of the response header as String values.
	 * @since 4.3
	 */
	public HttpResultMatcher<HttpResult> stringValues(final String name, final String... values) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				List<Object> actual = result.getResponse().getHeaderValues(name);
				assertEquals("Response header " + name, Arrays.asList(values), actual);
			}
		};
	}

	/**
	 * Assert that the named response header does not exist.
	 * @since 4.0
	 */
	public HttpResultMatcher<HttpResult> doesNotExist(final String name) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				assertTrue("Response should not contain header " + name,
						!result.getResponse().containsHeader(name));
			}
		};
	}

	/**
	 * Assert the primary value of the named response header as a {@code long}.
	 * <p>The {@link HttpResultMatcher<HttpResult>} returned by this method throws an
	 * {@link AssertionError} if the response does not contain the specified
	 * header, or if the supplied {@code value} does not match the primary value.
	 */
	public HttpResultMatcher<HttpResult> longValue(final String name, final long value) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				MockHttpServletResponse response = result.getResponse();
				assertTrue("Response does not contain header " + name, response.containsHeader(name));
				assertEquals("Response header " + name, value, Long.parseLong(response.getHeader(name)));
			}
		};
	}

	/**
	 * Assert the primary value of the named response header as a date String,
	 * using the preferred date format described in RFC 7231.
	 * <p>The {@link HttpResultMatcher<HttpResult>} returned by this method throws an
	 * {@link AssertionError} if the response does not contain the specified
	 * header, or if the supplied {@code value} does not match the primary value.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.1.1">Section 7.1.1.1 of RFC 7231</a>
	 * @since 4.2
	 */
	public HttpResultMatcher<HttpResult> dateValue(final String name, final long value) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
				format.setTimeZone(TimeZone.getTimeZone("GMT"));
				String formatted = format.format(new Date(value));
				MockHttpServletResponse response = result.getResponse();
				assertTrue("Response does not contain header " + name, response.containsHeader(name));
				assertEquals("Response header " + name, formatted, response.getHeader(name));
			}
		};
	}

}
