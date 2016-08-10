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

package org.springframework.test.web.http.result;

import javax.servlet.http.Cookie;

import org.hamcrest.Matcher;
import org.springframework.test.web.HttpResult;
import org.springframework.test.web.HttpResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.MatcherAssert.*;
import static org.springframework.test.util.AssertionErrors.*;

/**
 * Factory for response cookie assertions.
 * <p>An instance of this class is typically accessed via
 * {@link MockMvcResultMatchers#cookie}.
 *
 * @author Rossen Stoyanchev
 * @author Thomas Bruyelle
 * @since 3.2
 */
public class HttpCookieResultMatchers {

	/**
	 * Protected constructor.
	 * Use {@link MockMvcResultMatchers#cookie()}.
	 */
	protected HttpCookieResultMatchers() {
	}


	/**
	 * Assert a cookie value with the given Hamcrest {@link Matcher}.
	 */
	public HttpResultMatcher<HttpResult> value(final String name, final Matcher<? super String> matcher) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				Cookie cookie = result.getResponse().getCookie(name);
				assertTrue("Response cookie not found: " + name, cookie != null);
				assertThat("Response cookie", cookie.getValue(), matcher);
			}
		};
	}

	/**
	 * Assert a cookie value.
	 */
	public HttpResultMatcher<HttpResult> value(final String name, final String expectedValue) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				Cookie cookie = result.getResponse().getCookie(name);
				assertTrue("Response cookie not found: " + name, cookie != null);
				assertEquals("Response cookie", expectedValue, cookie.getValue());
			}
		};
	}

	/**
	 * Assert a cookie exists. The existence check is irrespective of whether
	 * max age is 0 (i.e. expired).
	 */
	public HttpResultMatcher<HttpResult> exists(final String name) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				Cookie cookie = result.getResponse().getCookie(name);
				assertTrue("No cookie with name: " + name, cookie != null);
			}
		};
	}

	/**
	 * Assert a cookie does not exist. Note that the existence check is
	 * irrespective of whether max age is 0, i.e. expired.
	 */
	public HttpResultMatcher<HttpResult> doesNotExist(final String name) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				Cookie cookie = result.getResponse().getCookie(name);
				assertTrue("Unexpected cookie with name " + name, cookie == null);
			}
		};
	}

	/**
	 * Assert a cookie's maxAge with a Hamcrest {@link Matcher}.
	 */
	public HttpResultMatcher<HttpResult> maxAge(final String name, final Matcher<? super Integer> matcher) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				Cookie cookie = result.getResponse().getCookie(name);
				assertTrue("No cookie with name: " + name, cookie != null);
				assertThat("Response cookie maxAge", cookie.getMaxAge(), matcher);
			}
		};
	}

	/**
	 * Assert a cookie's maxAge value.
	 */
	public HttpResultMatcher<HttpResult> maxAge(final String name, final int maxAge) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) {
				Cookie cookie = result.getResponse().getCookie(name);
				assertTrue("No cookie with name: " + name, cookie != null);
				assertEquals("Response cookie maxAge", maxAge, cookie.getMaxAge());
			}
		};
	}

	/**
	 * Assert a cookie path with a Hamcrest {@link Matcher}.
	 */
	public HttpResultMatcher<HttpResult> path(final String name, final Matcher<? super String> matcher) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) throws Exception {
				Cookie cookie = result.getResponse().getCookie(name);
				assertThat("Response cookie path", cookie.getPath(), matcher);
			}
		};
	}

	public HttpResultMatcher<HttpResult> path(final String name, final String path) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) throws Exception {
				Cookie cookie = result.getResponse().getCookie(name);
				assertEquals("Response cookie path", path, cookie.getPath());
			}
		};
	}

	/**
	 * Assert a cookie's domain with a Hamcrest {@link Matcher}.
	 */
	public HttpResultMatcher<HttpResult> domain(final String name, final Matcher<? super String> matcher) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) throws Exception {
				Cookie cookie = result.getResponse().getCookie(name);
				assertThat("Response cookie domain", cookie.getDomain(), matcher);
			}
		};
	}

	/**
	 * Assert a cookie's domain value.
	 */
	public HttpResultMatcher<HttpResult> domain(final String name, final String domain) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) throws Exception {
				Cookie cookie = result.getResponse().getCookie(name);
				assertEquals("Response cookie domain", domain, cookie.getDomain());
			}
		};
	}

	/**
	 * Assert a cookie's comment with a Hamcrest {@link Matcher}.
	 */
	public HttpResultMatcher<HttpResult> comment(final String name, final Matcher<? super String> matcher) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) throws Exception {
				Cookie cookie = result.getResponse().getCookie(name);
				assertThat("Response cookie comment", cookie.getComment(), matcher);
			}
		};
	}

	/**
	 * Assert a cookie's comment value.
	 */
	public HttpResultMatcher<HttpResult> comment(final String name, final String comment) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) throws Exception {
				Cookie cookie = result.getResponse().getCookie(name);
				assertEquals("Response cookie comment", comment, cookie.getComment());
			}
		};
	}

	/**
	 * Assert a cookie's version with a Hamcrest {@link Matcher}
	 */
	public HttpResultMatcher<HttpResult> version(final String name, final Matcher<? super Integer> matcher) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) throws Exception {
				Cookie cookie = result.getResponse().getCookie(name);
				assertThat("Response cookie version", cookie.getVersion(), matcher);
			}
		};
	}

	/**
	 * Assert a cookie's version value.
	 */
	public HttpResultMatcher<HttpResult> version(final String name, final int version) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) throws Exception {
				Cookie cookie = result.getResponse().getCookie(name);
				assertEquals("Response cookie version", version, cookie.getVersion());
			}
		};
	}

	/**
	 * Assert whether the cookie must be sent over a secure protocol or not.
	 */
	public HttpResultMatcher<HttpResult> secure(final String name, final boolean secure) {
		return new HttpResultMatcher<HttpResult>() {
			@Override
			public void match(HttpResult result) throws Exception {
				Cookie cookie = result.getResponse().getCookie(name);
				assertEquals("Response cookie secure", secure, cookie.getSecure());
			}
		};
	}

}
