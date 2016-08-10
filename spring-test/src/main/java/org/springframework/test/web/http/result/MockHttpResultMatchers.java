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

import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.hamcrest.Matcher;
import org.springframework.test.web.HttpResult;
import org.springframework.test.web.HttpResultMatcher;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.AntPathMatcher;

import static org.springframework.test.util.AssertionErrors.*;

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
public abstract class MockHttpResultMatchers {

	private static final AntPathMatcher pathMatcher = new AntPathMatcher();


	/**
	 * Access to request-related assertions.
	 */
	public static HttpRequestResultMatchers request() {
		return new HttpRequestResultMatchers();
	}

	/**
	 * Asserts the request was forwarded to the given URL.
	 * <p>This methods accepts only exact matches.
	 * @param expectedUrl the exact URL expected
	 */
	public static HttpResultMatcher<HttpResult> forwardedUrl(final String expectedUrl) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				assertEquals("Forwarded URL", expectedUrl, result.getResponse().getForwardedUrl());
			}
		};
	}

	/**
	 * Asserts the request was forwarded to the given URL.
	 * <p>This methods accepts {@link org.springframework.util.AntPathMatcher}
	 * expressions.
	 * @param urlPattern an AntPath expression to match against
	 * @since 4.0
	 * @see org.springframework.util.AntPathMatcher
	 */
	public static HttpResultMatcher<HttpResult> forwardedUrlPattern(final String urlPattern) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				assertTrue("AntPath expression", pathMatcher.isPattern(urlPattern));
				assertTrue("Forwarded URL does not match the expected URL pattern",
						pathMatcher.match(urlPattern, result.getResponse().getForwardedUrl()));
			}
		};
	}

	/**
	 * Asserts the request was redirected to the given URL.
	 * <p>This methods accepts only exact matches.
	 * @param expectedUrl the exact URL expected
	 */
	public static HttpResultMatcher<HttpResult> redirectedUrl(final String expectedUrl) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				assertEquals("Redirected URL", expectedUrl, result.getResponse().getRedirectedUrl());
			}
		};
	}

	/**
	 * Asserts the request was redirected to the given URL.
	 * <p>This method accepts {@link org.springframework.util.AntPathMatcher}
	 * expressions.
	 * @param expectedUrl an AntPath expression to match against
	 * @see org.springframework.util.AntPathMatcher
	 * @since 4.0
	 */
	public static HttpResultMatcher<HttpResult> redirectedUrlPattern(final String expectedUrl) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				assertTrue("AntPath expression",pathMatcher.isPattern(expectedUrl));
				assertTrue("Redirected URL",
						pathMatcher.match(expectedUrl, result.getResponse().getRedirectedUrl()));
			}
		};
	}

	/**
	 * Access to response status assertions.
	 */
	public static HttpStatusResultMatchers status() {
		return new HttpStatusResultMatchers();
	}

	/**
	 * Access to response header assertions.
	 */
	public static HttpHeaderResultMatchers header() {
		return new HttpHeaderResultMatchers();
	}

	/**
	 * Access to response body assertions.
	 */
	public static HttpContentResultMatchers content() {
		return new HttpContentResultMatchers();
	}

	/**
	 * Access to response body assertions using a
	 * <a href="https://github.com/jayway/JsonPath">JsonPath</a> expression
	 * to inspect a specific subset of the body.
	 * <p>The JSON path expression can be a parameterized string using
	 * formatting specifiers as defined in
	 * {@link String#format(String, Object...)}.
	 * @param expression the JSON path expression, optionally parameterized with arguments
	 * @param args arguments to parameterize the JSON path expression with
	 */
	public static HttpJsonPathResultMatchers jsonPath(String expression, Object ... args) {
		return new HttpJsonPathResultMatchers(expression, args);
	}

	/**
	 * Access to response body assertions using a
	 * <a href="https://github.com/jayway/JsonPath">JsonPath</a> expression
	 * to inspect a specific subset of the body and a Hamcrest matcher for
	 * asserting the value found at the JSON path.
	 * @param expression the JSON path expression
	 * @param matcher a matcher for the value expected at the JSON path
	 */
	public static <T> HttpResultMatcher<HttpResult> jsonPath(String expression, Matcher<T> matcher) {
		return new HttpJsonPathResultMatchers(expression).value(matcher);
	}

	/**
	 * Access to response body assertions using an XPath expression to
	 * inspect a specific subset of the body.
	 * <p>The XPath expression can be a parameterized string using formatting
	 * specifiers as defined in {@link String#format(String, Object...)}.
	 * @param expression the XPath expression, optionally parameterized with arguments
	 * @param args arguments to parameterize the XPath expression with
	 */
	public static HttpXpathResultMatchers xpath(String expression, Object... args) throws XPathExpressionException {
		return new HttpXpathResultMatchers(expression, null, args);
	}

	/**
	 * Access to response body assertions using an XPath expression to
	 * inspect a specific subset of the body.
	 * <p>The XPath expression can be a parameterized string using formatting
	 * specifiers as defined in {@link String#format(String, Object...)}.
	 * @param expression the XPath expression, optionally parameterized with arguments
	 * @param namespaces namespaces referenced in the XPath expression
	 * @param args arguments to parameterize the XPath expression with
	 */
	public static HttpXpathResultMatchers xpath(String expression, Map<String, String> namespaces, Object... args)
			throws XPathExpressionException {

		return new HttpXpathResultMatchers(expression, namespaces, args);
	}

	/**
	 * Access to response cookie assertions.
	 */
	public static HttpCookieResultMatchers cookie() {
		return new HttpCookieResultMatchers();
	}

}
