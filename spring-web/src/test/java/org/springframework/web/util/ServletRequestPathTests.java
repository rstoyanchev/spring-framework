/*
 * Copyright 2002-2021 the original author or authors.
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
package org.springframework.web.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ServletRequestPath}.
 *
 * @author Rossen Stoyanchev
 */
public class ServletRequestPathTests {

	@Test
	void parse() {
		// basic
		testParse("/app/servlet/a/b/c", "/app", "/servlet", "/a/b/c");

		// no context path
		testParse("/servlet/a/b/c", "", "/servlet", "/a/b/c");

		// context and servlet path only
		testParse("/a/b/aServlet/bServlet", "/a/b", "/aServlet/bServlet", "");

		// trailing slash
		testParse("/app/servlet/a/", "/app", "/servlet", "/a/");
		testParse("/app/servlet/a//", "/app", "/servlet", "/a//");
	}

	private void testParse(String requestUri, String contextPath, String servletPath, String pathWithinApplication) {
		ServletRequestPath requestPath = ServletRequestPath.parse(requestUri, contextPath, servletPath);
		assertThat(requestPath.contextPath().value()).isEqualTo(contextPath);
		assertThat(requestPath.servletPath().value()).isEqualTo(servletPath);
		assertThat(requestPath.pathWithinApplication().value()).isEqualTo(pathWithinApplication);
	}

}
