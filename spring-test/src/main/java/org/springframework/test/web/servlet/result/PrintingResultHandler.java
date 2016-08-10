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

package org.springframework.test.web.servlet.result;

import javax.servlet.http.HttpServletRequest;

import org.springframework.test.web.http.result.PrintingHttpResultHandler;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Result handler that prints {@link MvcResult} details to a given output
 * stream &mdash; for example: {@code System.out}, {@code System.err}, a
 * custom {@code java.io.PrintWriter}, etc.
 *
 * <p>An instance of this class is typically accessed via one of the
 * {@link MockMvcResultHandlers#print print} or {@link MockMvcResultHandlers#log log}
 * methods in {@link MockMvcResultHandlers}.
 *
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @since 3.2
 */
public class PrintingResultHandler extends PrintingHttpResultHandler<MvcResult> implements ResultHandler {

	/**
	 * Protected constructor.
	 * @param printer a {@link ResultValuePrinter} to do the actual writing
	 */
	protected PrintingResultHandler(ResultValuePrinter printer) {
		super(printer);
	}

	/**
	 * Print {@link MvcResult} details.
	 */
	@Override
	protected final void printResult(MvcResult result) throws Exception {
		this.getPrinter().printHeading("Handler");
		printHandler(result.getHandler(), result.getInterceptors());

		this.getPrinter().printHeading("Async");
		printAsyncResult(result);

		this.getPrinter().printHeading("Resolved Exception");
		printResolvedException(result.getResolvedException());

		this.getPrinter().printHeading("ModelAndView");
		printModelAndView(result.getModelAndView());

		this.getPrinter().printHeading("FlashMap");
		printFlashMap(RequestContextUtils.getOutputFlashMap(result.getRequest()));
	}

	protected void printAsyncResult(MvcResult result) throws Exception {
		HttpServletRequest request = result.getRequest();
		this.getPrinter().printValue("Async started", request.isAsyncStarted());
		Object asyncResult = null;
		try {
			asyncResult = result.getAsyncResult(0);
		}
		catch (IllegalStateException ex) {
			// Not set
		}
		this.getPrinter().printValue("Async result", asyncResult);
	}

	/**
	 * Print the handler.
	 */
	protected void printHandler(Object handler, HandlerInterceptor[] interceptors) throws Exception {
		if (handler == null) {
			this.getPrinter().printValue("Type", null);
		}
		else {
			if (handler instanceof HandlerMethod) {
				HandlerMethod handlerMethod = (HandlerMethod) handler;
				this.getPrinter().printValue("Type", handlerMethod.getBeanType().getName());
				this.getPrinter().printValue("Method", handlerMethod);
			}
			else {
				this.getPrinter().printValue("Type", handler.getClass().getName());
			}
		}
	}

	/**
	 * Print exceptions resolved through a HandlerExceptionResolver.
	 */
	protected void printResolvedException(Exception resolvedException) throws Exception {
		if (resolvedException == null) {
			this.getPrinter().printValue("Type", null);
		}
		else {
			this.getPrinter().printValue("Type", resolvedException.getClass().getName());
		}
	}

	/**
	 * Print the ModelAndView.
	 */
	protected void printModelAndView(ModelAndView mav) throws Exception {
		this.getPrinter().printValue("View name", (mav != null) ? mav.getViewName() : null);
		this.getPrinter().printValue("View", (mav != null) ? mav.getView() : null);
		if (mav == null || mav.getModel().size() == 0) {
			this.getPrinter().printValue("Model", null);
		}
		else {
			for (String name : mav.getModel().keySet()) {
				if (!name.startsWith(BindingResult.MODEL_KEY_PREFIX)) {
					Object value = mav.getModel().get(name);
					this.getPrinter().printValue("Attribute", name);
					this.getPrinter().printValue("value", value);
					Errors errors = (Errors) mav.getModel().get(BindingResult.MODEL_KEY_PREFIX + name);
					if (errors != null) {
						this.getPrinter().printValue("errors", errors.getAllErrors());
					}
				}
			}
		}
	}

	/**
	 * Print "output" flash attributes.
	 */
	protected void printFlashMap(FlashMap flashMap) throws Exception {
		if (ObjectUtils.isEmpty(flashMap)) {
			this.getPrinter().printValue("Attributes", null);
		}
		else {
			for (String name : flashMap.keySet()) {
				this.getPrinter().printValue("Attribute", name);
				this.getPrinter().printValue("value", flashMap.get(name));
			}
		}
	}

}
