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
package org.springframework.web.cors.reactive;

import reactor.core.publisher.Mono;

import org.springframework.web.server.ServerWebExchange;

/**
 * Provides handling CORS pre-flight requests.
 *
 * @author Rossen Stoyanchev
 * @since 5.3.4
 */
public interface PreFlightHandler {

	/**
	 * Handle a pre-flight request by finding the CORS configuration that matches
	 * the expected actual request, and applying it. After the call, the response
	 * should be rejected with {@link org.springframework.http.HttpStatus#FORBIDDEN},
	 * or have CORS headers applied.
	 * @param exchange the exchange for the request
	 * @return a completion handle
	 */
	Mono<Void> handlePreFlight(ServerWebExchange exchange);

}
