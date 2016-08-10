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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.web.HttpResult;
import org.springframework.test.web.HttpResultHandler;
import org.springframework.util.CollectionUtils;

/**
 * Static factory methods for {@link ResultHandler}-based result actions.
 *
 * <h3>Eclipse Users</h3>
 * <p>Consider adding this class as a Java editor favorite. To navigate to
 * this setting, open the Preferences and type "favorites".
 *
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @since 3.2
 */
public abstract class MockHttpResultHandlers {

	private static final Log logger = LogFactory.getLog("org.springframework.test.web.servlet.result");


	/**
	 * Log {@link MvcResult} details as a {@code DEBUG} log message via
	 * Apache Commons Logging using the log category
	 * {@code org.springframework.test.web.servlet.result}.
	 * @since 4.2
	 * @see #print()
	 * @see #print(OutputStream)
	 * @see #print(Writer)
	 */
	public static HttpResultHandler<HttpResult> log() {
		return new LoggingResultHandler();
	}

	/**
	 * Print {@link MvcResult} details to the "standard" output stream.
	 * @see System#out
	 * @see #print(OutputStream)
	 * @see #print(Writer)
	 * @see #log()
	 */
	public static HttpResultHandler<HttpResult> print() {
		return print(System.out);
	}

	/**
	 * Print {@link MvcResult} details to the supplied {@link OutputStream}.
	 * @since 4.2
	 * @see #print()
	 * @see #print(Writer)
	 * @see #log()
	 */
	public static HttpResultHandler<HttpResult> print(OutputStream stream) {
		return new PrintWriterPrintingHttpResultHandler(new PrintWriter(stream, true));
	}

	/**
	 * Print {@link MvcResult} details to the supplied {@link Writer}.
	 * @since 4.2
	 * @see #print()
	 * @see #print(OutputStream)
	 * @see #log()
	 */
	public static HttpResultHandler<HttpResult> print(Writer writer) {
		return new PrintWriterPrintingHttpResultHandler(new PrintWriter(writer, true));
	}


	/**
	 * A {@link PrintingHttpResultHandler} that writes to a {@link PrintWriter}.
	 */
	private static class PrintWriterPrintingHttpResultHandler extends PrintingHttpResultHandler<HttpResult> {

		public PrintWriterPrintingHttpResultHandler(final PrintWriter writer) {
			super(new ResultValuePrinter() {
				@Override
				public void printHeading(String heading) {
					writer.println();
					writer.println(String.format("%s:", heading));
				}
				@Override
				public void printValue(String label, Object value) {
					if (value != null && value.getClass().isArray()) {
						value = CollectionUtils.arrayToList(value);
					}
					writer.println(String.format("%17s = %s", label, value));
				}
			});
		}
	}


	/**
	 * A {@link ResultHandler} that logs {@link MvcResult} details at
	 * {@code DEBUG} level via Apache Commons Logging.
	 *
	 * <p>Delegates to a {@link PrintWriterPrintingHttpResultHandler} for
	 * building the log message.
	 *
	 * @since 4.2
	 */
	private static class LoggingResultHandler implements HttpResultHandler<HttpResult> {

		@Override
		public void handle(HttpResult result) throws Exception {
			if (logger.isDebugEnabled()) {
				StringWriter stringWriter = new StringWriter();
				HttpResultHandler<HttpResult> printingResultHandler =
						new PrintWriterPrintingHttpResultHandler(new PrintWriter(stringWriter));
				printingResultHandler.handle(result);
				logger.debug("MvcResult details:\n" + stringWriter);
			}
		}
	}

}
