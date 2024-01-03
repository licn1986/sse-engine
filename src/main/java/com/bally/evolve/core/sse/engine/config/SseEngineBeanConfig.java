package com.bally.evolve.core.sse.engine.config;

import com.bally.evolve.core.sse.engine.SseEngineProperties;
import com.bally.evolve.core.sse.engine.event.EventDispatcher;
import com.bally.evolve.core.sse.engine.event.model.DelayedEvent;
import com.bally.evolve.core.sse.engine.event.model.Event;
import com.bally.evolve.core.sse.engine.monitoring.EventQueueMonitor;

import java.util.concurrent.DelayQueue;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class SseEngineBeanConfig {

  @Bean("sseEngineProps")
  public SseEngineProperties sseEngineProps() {
    return new SseEngineProperties();
  }

  @Bean("eventQueue")
  public DelayQueue<DelayedEvent<Event>> eventQueue() {
    return new DelayQueue<>();
  }

  @Bean("eventDispatcher")
  public EventDispatcher eventDispatcher() {
    return new EventDispatcher();
  }

  @Bean("eventQueueMonitor")
  public EventQueueMonitor eventQueueMonitor() {
    return new EventQueueMonitor();
  }
}
