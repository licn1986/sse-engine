package com.bally.evolve.core.sse.engine.config;

import com.bally.evolve.core.sse.engine.SseSubscription;
import com.bally.evolve.core.sse.engine.event.EventPublisher;
import com.bally.evolve.core.sse.engine.event.EventWebFluxPublisher;
import com.bally.evolve.core.sse.engine.event.EventWebFluxSubscriber;
import com.bally.evolve.core.sse.engine.monitoring.SseRegistryMonitor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks.Many;

/**
 * Only used with Spring WebFlux in context
 * <p>
 * If you need to use with Spring MVC, look {@code SseEngineWebMvcBeanConfig}
 * 
 */
@Slf4j
@Configuration
public class SseEngineWebFluxBeanConfig {
  //
  //  @Autowired
  //  @Qualifier("sseRegistryMonitor")
  //  private SseRegistryMonitor sseRegistryMonitor;

  @Bean
  public Map<String, Collection<SseSubscription<Many<ServerSentEvent<String>>>>> sseRegistry() {
    return new ConcurrentHashMap<>(20);
  }

  @Bean
  public EventPublisher eventPublisher() {
    return new EventWebFluxPublisher();
  }

  @Bean
  public EventWebFluxSubscriber eventSubscriber() {
    return new EventWebFluxSubscriber();
  }

  @Bean("sseRegistryMonitor")
  public SseRegistryMonitor sseRegistryMonitor() {
    return new SseRegistryMonitor();
  }

  @Scheduled(fixedDelayString = "#{sseEngineProps.getEventFluxRegistryCleanupFixedDelay()}")
  protected void scheduleSseRegistryMonitoring() {
    log.info("clear flux registry...");
    sseRegistryMonitor().cleanupEventFluxRegistry();
  }
}
