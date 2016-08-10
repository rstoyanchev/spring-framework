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

import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.springframework.test.web.http.result.HttpXpathResultMatchers;

/**
 * Factory for assertions on the response content using XPath expressions.
 * <p>An instance of this class is typically accessed via
 * {@link MockMvcResultMatchers#xpath}.
 *
 * @author Rossen Stoyanchev
 * @since 3.2
 */
public class XpathResultMatchers extends HttpXpathResultMatchers {

	/**
	 * Protected constructor, not for direct instantiation. Use
	 * {@link MockMvcResultMatchers#xpath(String, Object...)} or
	 * {@link MockMvcResultMatchers#xpath(String, Map, Object...)}.
	 * @param expression the XPath expression
	 * @param namespaces XML namespaces referenced in the XPath expression, or {@code null}
	 * @param args arguments to parameterize the XPath expression with using the
	 * formatting specifiers defined in {@link String#format(String, Object...)}
	 */
	protected XpathResultMatchers(String expression, Map<String, String> namespaces, Object ... args)
			throws XPathExpressionException {
		super(expression, namespaces, args);
	}
}