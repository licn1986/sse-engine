package com.bally.evolve.core.sse.engine.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import com.bally.evolve.core.sse.engine.SseEngineProperties;
import com.bally.evolve.core.sse.engine.config.SseEngineBeanConfig;
import com.bally.evolve.core.sse.engine.event.EventDispatcher;
import com.bally.evolve.core.sse.engine.event.model.Event;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {SseEngineBeanConfig.class})
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
class EventQueueMonitorTest {

  @Autowired SseEngineProperties sseEngineProps;

  @Autowired EventDispatcher eventDispatcher;

  @Autowired EventQueueMonitor eventQueueMonitor;

  @BeforeEach
  void setup() {
    Awaitility.await().untilTrue(isEventQueueClearedUp());
  }

  private AtomicBoolean isEventQueueClearedUp() {
    eventQueueMonitor.eventQueue.clear();
    return new AtomicBoolean(eventQueueMonitor.eventQueue.isEmpty());
  }

  @Test
  void testCleanupDelayedEvents() {
    // Setup
    EventQueueMonitorTest eventMonitorTestSpy = spy(this);
    Event event1 = new Event("1001", "test-event", "SUBID#1", "test payload");
    Event event2 = new Event("1002", "test-event", "SUBID#2", "test payload");
    Event event3 = new Event("1003", "test-event", "SUBID#3", "test payload");
    sseEngineProps.setEventTimeout(0L);
    eventDispatcher.dispatchEvent(
        event1, eventMonitorTestSpy::notifyEventSent, eventMonitorTestSpy::notifyEventExpired);
    sseEngineProps.setEventTimeout(5L);
    eventDispatcher.dispatchEvent(
        event2, eventMonitorTestSpy::notifyEventSent, eventMonitorTestSpy::notifyEventExpired);
    sseEngineProps.setEventTimeout(10L);
    eventDispatcher.dispatchEvent(
        event3, eventMonitorTestSpy::notifyEventSent, eventMonitorTestSpy::notifyEventExpired);

    Awaitility.await()
        .pollDelay(2, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(eventQueueMonitor.eventQueue).hasSize(3));

    // Execute
    eventQueueMonitor.cleanupDelayedEvents();

    Awaitility.await()
        .pollDelay(2, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(eventQueueMonitor.eventQueue).isEmpty());

    // Assert
    // assertThat(eventQueueMonitor.eventQueue).isEmpty();
    Mockito.verify(eventMonitorTestSpy, times(0)).notifyEventSent(ArgumentMatchers.any());
    Mockito.verify(eventMonitorTestSpy, times(1)).notifyEventExpired(event1);
    Mockito.verify(eventMonitorTestSpy, times(1)).notifyEventExpired(event2);
    Mockito.verify(eventMonitorTestSpy, times(1)).notifyEventExpired(event3);
  }

  void notifyEventSent(Event event) {
    log.info("Notification of the event sent. " + event.toString());
  }

  void notifyEventExpired(Event event) {
    log.info("Notification of the event expired. " + event.toString());
  }
}
