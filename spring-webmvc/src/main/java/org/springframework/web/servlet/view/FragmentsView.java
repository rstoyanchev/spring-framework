/*
 * Copyright 2002-2024 the original author or authors.
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

package org.springframework.web.servlet.view;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.SmartView;
import org.springframework.web.servlet.View;

/**
 * {@link View} that contains a collection of {@link ModelAndView}s that
 * represent individual fragments to render.
 *
 * @author Rossen Stoyanchev
 * @since 6.2
 */
public class FragmentsView implements SmartView {

	private final Collection<ModelAndView> modelAndViews;

	private final Set<ModelAndView> resolvedModelAndViews = new LinkedHashSet<>();


	public FragmentsView(Collection<ModelAndView> modelAndViews) {
		this.modelAndViews = modelAndViews;
	}


	@Override
	public boolean isRedirectView() {
		return false;
	}

	@Override
	public void initNestedViews(ViewResolutionCallback callback) throws Exception {
		for (ModelAndView mv : this.modelAndViews) {
			View view = callback.resolveViewName(mv);
			this.resolvedModelAndViews.add(new ModelAndView(view, mv.getModel()));
		}
	}

	@Override
	public void render(
			Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		HttpServletResponse nonClosingResponse = new NonClosingHttpServletResponse(response);

		for (ModelAndView mv : this.resolvedModelAndViews) {
			View view = mv.getView();
			Assert.notNull(view, "No resolved View");
			view.render(mv.getModel(), request, nonClosingResponse);
			response.flushBuffer();
		}
	}

	@Override
	public String toString() {
		return "MultiView " + this.modelAndViews;
	}


	/**
	 * Wraps the response to apply {@link NonClosingServletOutputStream}.
	 */
	private final static class NonClosingHttpServletResponse extends HttpServletResponseWrapper {

		@Nullable
		private ServletOutputStream os;

		public NonClosingHttpServletResponse(HttpServletResponse response) {
			super(response);
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			if (this.os == null) {
				this.os = new NonClosingServletOutputStream(getResponse().getOutputStream());
			}
			return this.os;
		}
	}


	/**
	 * Wraps the {@code OutputStream} to prevent it from being closed.
	 */
	private final static class NonClosingServletOutputStream extends ServletOutputStream {

		private final ServletOutputStream os;

		public NonClosingServletOutputStream(ServletOutputStream os) {
			this.os = os;
		}

		@Override
		public void write(int b) throws IOException {
			this.os.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			this.os.write(b, off, len);
		}

		@Override
		public boolean isReady() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			throw new UnsupportedOperationException();
		}
	}

}
