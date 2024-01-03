package com.bally.evolve.core.sse.engine.config;

import com.bally.evolve.core.sse.engine.SseEngineProperties;
import com.bally.evolve.core.sse.engine.event.EventPublisher;
import com.bally.evolve.core.sse.engine.monitoring.EventQueueMonitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableAutoConfiguration
@EnableScheduling
public class SseEngineScheduledConfig {

  @Autowired SseEngineProperties sseEngineProps;

  @Autowired
  @Qualifier("eventPublisher")
  EventPublisher eventPublisher;

  @Autowired
  @Qualifier("eventQueueMonitor")
  EventQueueMonitor eventQueueMonitor;

  @Scheduled(fixedDelayString = "#{sseEngineProps.getEventPublishFixedDelay()}")
  protected void scheduleEventPublishing() {
    eventPublisher.publishEvents();
  }

  @Scheduled(fixedDelayString = "#{sseEngineProps.getEventQueueCleanupFixedDelay()}")
  protected void scheduleEventQueueMonitoring() {
    eventQueueMonitor.cleanupDelayedEvents();
  }
}
