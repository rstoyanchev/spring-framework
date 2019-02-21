/*
 * Copyright 2002-2019 the original author or authors.
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
package org.springframework.messaging.handler.invocation.reactive;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Mono;

import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.messaging.handler.MessagingAdviceBean;
import org.springframework.messaging.handler.invocation.AbstractExceptionHandlerMethodResolver;
import org.springframework.util.Assert;

/**
 * Help to initialize and invoke an {@link InvocableHandlerMethod}, and to then
 * apply return value handling and exception handling. Holds all necessary
 * configuration necessary to do so.
 *
 * @author Rossen Stoyanchev
 * @since 5.2
 */
class InvocableHandlerMethodHelper {

	private static Log logger = LogFactory.getLog(InvocableHandlerMethodHelper.class);


	private final HandlerMethodArgumentResolverComposite argumentResolvers =
			new HandlerMethodArgumentResolverComposite();

	private final HandlerMethodReturnValueHandlerComposite returnValueHandlers =
			new HandlerMethodReturnValueHandlerComposite();

	private ReactiveAdapterRegistry reactiveAdapterRegistry = ReactiveAdapterRegistry.getSharedInstance();

	private final Function<Class<?>, AbstractExceptionHandlerMethodResolver> exceptionMethodResolverFactory;

	private final Map<Class<?>, AbstractExceptionHandlerMethodResolver> exceptionHandlerCache =
			new ConcurrentHashMap<>(64);

	private final Map<MessagingAdviceBean, AbstractExceptionHandlerMethodResolver> exceptionHandlerAdviceCache =
			new LinkedHashMap<>(64);


	public InvocableHandlerMethodHelper(Function<Class<?>, AbstractExceptionHandlerMethodResolver> resolverFactory) {
		this.exceptionMethodResolverFactory = resolverFactory;
	}


	public HandlerMethodArgumentResolverComposite getArgumentResolvers() {
		return this.argumentResolvers;
	}

	public HandlerMethodReturnValueHandlerComposite getReturnValueHandlers() {
		return this.returnValueHandlers;
	}

	public void setReactiveAdapterRegistry(ReactiveAdapterRegistry registry) {
		Assert.notNull(registry, "ReactiveAdapterRegistry is required");
		this.reactiveAdapterRegistry = registry;
	}

	public ReactiveAdapterRegistry getReactiveAdapterRegistry() {
		return this.reactiveAdapterRegistry;
	}

	protected void registerExceptionHandlerAdvice(
			MessagingAdviceBean bean, AbstractExceptionHandlerMethodResolver resolver) {

		this.exceptionHandlerAdviceCache.put(bean, resolver);
	}


	/**
	 * Create an {@link InvocableHandlerMethod}, invoke it, apply return value
	 * handling and also exception handling.
	 *
	 * @param handlerMethod the target handler method to invoke
	 * @param message the message to handle
	 * @return completion handle
	 */
	public Mono<Void> invoke(HandlerMethod handlerMethod, Message<?> message) {
		InvocableHandlerMethod invocable = new InvocableHandlerMethod(handlerMethod);
		invocable.setArgumentResolvers(this.argumentResolvers.getResolvers());
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking " + invocable.getShortLogMessage());
		}
		return invocable.invoke(message)
				.flatMap(value -> {
					MethodParameter returnType = invocable.getReturnType();
					return this.returnValueHandlers.handleReturnValue(value, returnType, message);
				})
				.onErrorResume(ex -> handleException(ex, message, handlerMethod));
	}

	private Mono<? extends Void> handleException(Throwable ex, Message<?> message, HandlerMethod handlerMethod) {
		InvocableHandlerMethod exInvocable = findExceptionHandler(handlerMethod, ex);
		if (exInvocable == null) {
			return Mono.error(ex);
		}
		return exInvocable.invoke(message, ex)
				.flatMap(value -> {
					MethodParameter returnType = exInvocable.getReturnType();
					return this.returnValueHandlers.handleReturnValue(value, returnType, message);
				});
	}

	/**
	 * Find an exception handling method for the given exception.
	 * <p>The default implementation searches methods in the class hierarchy of
	 * the HandlerMethod first and if not found, it continues searching for
	 * additional handling methods registered via
	 * {@link #registerExceptionHandlerAdvice}.
	 * @param handlerMethod the method where the exception was raised
	 * @param ex the exception raised or signaled
	 * @return a method to handle the exception, or {@code null}
	 */
	@Nullable
	private InvocableHandlerMethod findExceptionHandler(HandlerMethod handlerMethod, Throwable ex) {
		if (logger.isDebugEnabled()) {
			logger.debug("Searching for methods to handle " + ex.getClass().getSimpleName());
		}
		Class<?> beanType = handlerMethod.getBeanType();
		AbstractExceptionHandlerMethodResolver resolver = this.exceptionHandlerCache.get(beanType);
		if (resolver == null) {
			resolver = this.exceptionMethodResolverFactory.apply(beanType);
			this.exceptionHandlerCache.put(beanType, resolver);
		}
		InvocableHandlerMethod exceptionHandlerMethod = null;
		Method method = resolver.resolveMethod(ex);
		if (method != null) {
			exceptionHandlerMethod = new InvocableHandlerMethod(handlerMethod.getBean(), method);
		}
		else {
			for (MessagingAdviceBean advice : this.exceptionHandlerAdviceCache.keySet()) {
				if (advice.isApplicableToBeanType(beanType)) {
					resolver = this.exceptionHandlerAdviceCache.get(advice);
					method = resolver.resolveMethod(ex);
					if (method != null) {
						exceptionHandlerMethod = new InvocableHandlerMethod(advice.resolveBean(), method);
						break;
					}
				}
			}
		}
		if (exceptionHandlerMethod != null) {
			logger.debug("Found exception handler " + exceptionHandlerMethod.getShortLogMessage());
			exceptionHandlerMethod.setArgumentResolvers(this.argumentResolvers.getResolvers());
		}
		else {
			logger.error("No exception handling method", ex);
		}
		return exceptionHandlerMethod;
	}

}
