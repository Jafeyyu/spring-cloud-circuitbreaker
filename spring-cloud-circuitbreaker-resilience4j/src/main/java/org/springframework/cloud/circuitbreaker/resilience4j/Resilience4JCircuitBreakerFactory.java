/*
 * Copyright 2013-2018 the original author or authors.
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

package org.springframework.cloud.circuitbreaker.resilience4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.util.Assert;

/**
 * @author Ryan Baxter
 */
public class Resilience4JCircuitBreakerFactory extends
		CircuitBreakerFactory<Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> {

	private Function<String, Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration> defaultConfiguration = id -> new Resilience4JConfigBuilder(
			id).circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
					.timeLimiterConfig(TimeLimiterConfig.ofDefaults()).build();

	private CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();

	private ExecutorService executorService = Executors.newCachedThreadPool();

	private Map<String, Customizer<CircuitBreaker>> circuitBreakerCustomizers = new HashMap<>();

	@Override
	protected Resilience4JConfigBuilder configBuilder(String id) {
		return new Resilience4JConfigBuilder(id);
	}

	@Override
	public void configureDefault(
			Function<String, Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration> defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}

	public void configureCircuitBreakerRegistry(CircuitBreakerRegistry registry) {
		this.circuitBreakerRegistry = registry;
	}

	protected CircuitBreakerRegistry getCircuitBreakerRegistry() {
		return circuitBreakerRegistry;
	}

	public void configureExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public Resilience4JCircuitBreaker create(String id) {
		Assert.hasText(id, "A CircuitBreaker must have an id.");
		Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration config = getConfigurations()
				.computeIfAbsent(id, defaultConfiguration);
		return new Resilience4JCircuitBreaker(id, config.getCircuitBreakerConfig(), config.getTimeLimiterConfig(),
				circuitBreakerRegistry, executorService, Optional.ofNullable(circuitBreakerCustomizers.get(id)));
	}

	public void addCircuitBreakerCustomizer(Customizer<CircuitBreaker> customizer, String... ids) {
		for (String id : ids) {
			circuitBreakerCustomizers.put(id, customizer);
		}
	}

}
