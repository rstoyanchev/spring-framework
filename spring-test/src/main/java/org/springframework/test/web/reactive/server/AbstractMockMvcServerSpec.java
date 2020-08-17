/*
 * Copyright 2002-2020 the original author or authors.
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
package org.springframework.test.web.reactive.server;

import javax.servlet.Filter;

import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.test.web.servlet.DispatcherServletCustomizer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;

/**
 *
 */
abstract class AbstractMockMvcServerSpec<B extends MockMvcTestClient.MockMvcServerSpec<B>>
		implements MockMvcTestClient.MockMvcServerSpec<B> {

	protected abstract ConfigurableMockMvcBuilder<?> getMockMvcBuilder();


	@Override
	public <T extends B> T addFilters(Filter... filters) {
		getMockMvcBuilder().addFilters(filters);
		return self();
	}

	public final <T extends B> T addFilter(Filter filter, String... urlPatterns) {
		getMockMvcBuilder().addFilter(filter, urlPatterns);
		return self();
	}

	@Override
	public <T extends B> T dispatchOptions(boolean dispatchOptions) {
		getMockMvcBuilder().dispatchOptions(dispatchOptions);
		return self();
	}

	@Override
	public <T extends B> T addDispatcherServletCustomizer(DispatcherServletCustomizer customizer) {
		getMockMvcBuilder().addDispatcherServletCustomizer(customizer);
		return self();
	}

	@Override
	public <T extends B> T apply(MockMvcConfigurer configurer) {
		getMockMvcBuilder().apply(configurer);
		return self();
	}

	@SuppressWarnings("unchecked")
	private <T extends B> T self() {
		return (T) this;
	}

	@Override
	public WebTestClient.Builder configureClient() {
		MockMvc mockMvc = getMockMvcBuilder().build();
		ClientHttpConnector connector = new MockMvcHttpConnector(mockMvc);
		return new DefaultWebTestClientBuilder(connector);
	}

	@Override
	public WebTestClient build() {
		return configureClient().build();
	}

}
