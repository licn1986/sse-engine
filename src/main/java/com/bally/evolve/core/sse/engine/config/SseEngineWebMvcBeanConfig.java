package com.bally.evolve.core.sse.engine.config;

import com.bally.evolve.core.sse.engine.SseSubscription;
import com.bally.evolve.core.sse.engine.event.EventWebMvcPublisher;
import com.bally.evolve.core.sse.engine.event.EventWebMvcSubscriber;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Only used with Spring MVC in context
 * <p>
 * If you need to use with Spring WebFlux, look {@code SseEngineWebFluxBeanConfig}
 * 
 */
@Configuration
public class SseEngineWebMvcBeanConfig {

  @Bean("sseRegistry")
  public Map<String, Collection<SseSubscription<SseEmitter>>> sseRegistry() {
    return new ConcurrentHashMap<>(20);
  }

  @Bean
  public EventWebMvcPublisher eventPublisher() {
    return new EventWebMvcPublisher();
  }

  @Bean
  public EventWebMvcSubscriber eventSubscriber() {
    return new EventWebMvcSubscriber();
  }
}
