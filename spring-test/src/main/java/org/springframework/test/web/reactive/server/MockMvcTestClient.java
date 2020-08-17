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

import java.util.function.Supplier;

import javax.servlet.Filter;

import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.test.web.servlet.DispatcherServletCustomizer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.validation.Validator;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 *
 * @author Rossen Stoyanchev
 * @since 5.3
 */
public interface MockMvcTestClient {

	/**
	 *
	 * @param controllers
	 * @return
	 */
	static ControllerSpec bindToController(Object... controllers) {
		return new DefaultControllerMockMvcSpec(controllers);
	}

	/**
	 *
	 * @param context
	 * @return
	 */
	static MockMvcServerSpec<?> bindToApplicationContext(WebApplicationContext context) {
		return new ApplicationContextMockMvcSpec(context);
	}

	/**
	 *
	 * @param mockMvc
	 * @return
	 */
	static WebTestClient.Builder bindTo(MockMvc mockMvc) {
		ClientHttpConnector connector = new MockMvcHttpConnector(mockMvc);
		return new DefaultWebTestClientBuilder(connector);
	}


	/**
	 * Base specification for configuring {@link MockMvc}, and a simple facade
	 * around {@link ConfigurableMockMvcBuilder}.
	 *
	 * @param <B> a self reference to the builder type
	 */
	interface MockMvcServerSpec<B extends MockMvcServerSpec<B>> {

		/**
		 * Add a global filter.
		 * <p>This is delegated to
		 * {@link ConfigurableMockMvcBuilder#addFilters(Filter...)}.
		 */
		<T extends B> T addFilters(Filter... filters);

		/**
		 * Add a filter for specific URL patterns.
		 * <p>This is delegated to
		 * {@link ConfigurableMockMvcBuilder#addFilter(Filter, String...)}.
		 */
		<T extends B> T addFilter(Filter filter, String... urlPatterns);

		/**
		 * Whether to handle HTTP OPTIONS requests.
		 * <p>This is delegated to
		 * {@link ConfigurableMockMvcBuilder#dispatchOptions(boolean)}.
		 */
		<T extends B> T dispatchOptions(boolean dispatchOptions);

		/**
		 * Allow customization of {@code DispatcherServlet}.
		 * <p>This is delegated to
		 * {@link ConfigurableMockMvcBuilder#addDispatcherServletCustomizer(DispatcherServletCustomizer)}.
		 */
		<T extends B> T addDispatcherServletCustomizer(DispatcherServletCustomizer customizer);

		/**
		 * Add a {@code MockMvcConfigurer} that automates MockMvc setup.
		 * <p>This is delegated to
		 * {@link ConfigurableMockMvcBuilder#apply(MockMvcConfigurer)}.
		 */
		<T extends B> T apply(MockMvcConfigurer configurer);

		/**
		 * Proceed to configure and build the test client.
		 */
		WebTestClient.Builder configureClient();

		/**
		 * Shortcut to build the test client.
		 */
		WebTestClient build();
	}


	/**
	 * Specification for configuring {@link MockMvc} to test one or more
	 * controllers directly, and a simple facade around
	 * {@link StandaloneMockMvcBuilder}.
	 */
	interface ControllerSpec extends MockMvcServerSpec<ControllerSpec> {

		ConfigurableMockMvcBuilder<?> getMockMvcBuilder();

		/**
		 * Register {@link org.springframework.web.bind.annotation.ControllerAdvice}
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setControllerAdvice(Object...)}.
		 */
		ControllerSpec setControllerAdvice(Object... controllerAdvice);

		/**
		 * Set the message converters to use.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setMessageConverters(HttpMessageConverter[])}.
		 */
		ControllerSpec setMessageConverters(HttpMessageConverter<?>... messageConverters);

		/**
		 * Provide a custom {@link Validator}.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setValidator(Validator)}.
		 */
		ControllerSpec setValidator(Validator validator);

		/**
		 * Provide a conversion service.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setConversionService(FormattingConversionService)}.
		 */
		ControllerSpec setConversionService(FormattingConversionService conversionService);

		/**
		 * Add global interceptors.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#addInterceptors(HandlerInterceptor...)}.
		 */
		ControllerSpec addInterceptors(HandlerInterceptor... interceptors);

		/**
		 * Add interceptors for specific patterns.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#addMappedInterceptors(String[], HandlerInterceptor...)}.
		 */
		ControllerSpec addMappedInterceptors(
				@Nullable String[] pathPatterns, HandlerInterceptor... interceptors);

		/**
		 * Set a ContentNegotiationManager.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setContentNegotiationManager(ContentNegotiationManager)}.
		 */
		ControllerSpec setContentNegotiationManager(ContentNegotiationManager manager);

		/**
		 * Specify the timeout value for async execution.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setAsyncRequestTimeout(long)}.
		 */
		ControllerSpec setAsyncRequestTimeout(long timeout);

		/**
		 * Provide custom argument resolvers.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setCustomArgumentResolvers(HandlerMethodArgumentResolver...)}.
		 */
		ControllerSpec setCustomArgumentResolvers(HandlerMethodArgumentResolver... argumentResolvers);

		/**
		 * Provide custom return value handlers.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setCustomReturnValueHandlers(HandlerMethodReturnValueHandler...)}.
		 */
		ControllerSpec setCustomReturnValueHandlers(HandlerMethodReturnValueHandler... handlers);

		/**
		 * Set the HandlerExceptionResolver types to use.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setHandlerExceptionResolvers(HandlerExceptionResolver...)}.
		 */
		ControllerSpec setHandlerExceptionResolvers(HandlerExceptionResolver... exceptionResolvers);

		/**
		 * Set up view resolution.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setViewResolvers(ViewResolver...)}.
		 */
		ControllerSpec setViewResolvers(ViewResolver... resolvers);

		/**
		 * Set up a single {@link ViewResolver} with a fixed view.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setSingleView(View)}.
		 */
		ControllerSpec setSingleView(View view);

		/**
		 * Provide the LocaleResolver to use.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setLocaleResolver(LocaleResolver)}.
		 */
		ControllerSpec setLocaleResolver(LocaleResolver localeResolver);

		/**
		 * Provide a custom FlashMapManager.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setFlashMapManager(FlashMapManager)}.
		 */
		ControllerSpec setFlashMapManager(FlashMapManager flashMapManager);

		/**
		 * Enable URL path matching with parsed
		 * {@link org.springframework.web.util.pattern.PathPattern PathPatterns}.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setPatternParser(PathPatternParser)}.
		 */
		void setPatternParser(PathPatternParser parser);

		/**
		 * Whether to match trailing slashes.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setUseTrailingSlashPatternMatch(boolean)}.
		 */
		ControllerSpec setUseTrailingSlashPatternMatch(boolean useTrailingSlashPatternMatch);

		/**
		 * Configure placeholder values to use.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#addPlaceholderValue(String, String)}.
		 */
		ControllerSpec addPlaceholderValue(String name, String value);

		/**
		 * Configure factory for a custom {@link RequestMappingHandlerMapping}.
		 * <p>This is delegated to
		 * {@link StandaloneMockMvcBuilder#setCustomHandlerMapping(Supplier)}.
		 */
		ControllerSpec setCustomHandlerMapping(Supplier<RequestMappingHandlerMapping> factory);
	}

}