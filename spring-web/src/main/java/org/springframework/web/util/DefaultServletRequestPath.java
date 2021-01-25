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

import java.util.List;

import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 *
 * @author Rossen Stoyanchev
 * @since 5.3.4
 */
class DefaultServletRequestPath implements ServletRequestPath {

	private final PathContainer fullPath;

	private final PathContainer contextPath;

	private final PathContainer servletPath;

	private final PathContainer pathWithinApplication;


	DefaultServletRequestPath(String rawPath, @Nullable String contextPath, String servletPath) {
		Assert.notNull(servletPath, "`servletPath` is required");
		RequestPath path1 = RequestPath.parse(rawPath, contextPath);
		RequestPath path2 = RequestPath.parse(rawPath, contextPath + servletPath);

		this.fullPath = path1;
		this.contextPath = path1.contextPath();
		this.servletPath = path2.subPath(path1.contextPath().elements().size(), path2.contextPath().elements().size());
		this.pathWithinApplication = path2.pathWithinApplication();
	}


	// PathContainer methods..

	@Override
	public String value() {
		return this.fullPath.value();
	}

	@Override
	public List<Element> elements() {
		return this.fullPath.elements();
	}


	// RequestPath and ServletRequestPath methods..

	@Override
	public PathContainer contextPath() {
		return this.contextPath;
	}

	@Override
	public PathContainer servletPath() {
		return this.servletPath;
	}

	@Override
	public PathContainer pathWithinApplication() {
		return this.pathWithinApplication;
	}

	@Override
	public ServletRequestPath modifyContextPath(String contextPath) {
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		DefaultServletRequestPath otherPath= (DefaultServletRequestPath) other;
		return (this.fullPath.equals(otherPath.fullPath) &&
				this.contextPath.equals(otherPath.contextPath) &&
				this.servletPath.equals(otherPath.servletPath) &&
				this.pathWithinApplication.equals(otherPath.pathWithinApplication));
	}

	@Override
	public int hashCode() {
		int result = this.fullPath.hashCode();
		result = 31 * result + this.contextPath.hashCode();
		result = 31 * result + this.servletPath.hashCode();
		result = 31 * result + this.pathWithinApplication.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return this.fullPath.toString();
	}

}
