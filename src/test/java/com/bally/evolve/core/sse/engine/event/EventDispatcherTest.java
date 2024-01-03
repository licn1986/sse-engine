package com.bally.evolve.core.sse.engine.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.bally.evolve.core.sse.engine.config.SseEngineBeanConfig;
import com.bally.evolve.core.sse.engine.event.model.Event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(classes = {SseEngineBeanConfig.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class EventDispatcherTest {

  @Autowired
  @Qualifier("eventDispatcher")
  EventDispatcher eventDispatcher;

  @Test
  void testDispatchEvent_EventInQueue() {
    // Setup
    Event event = new Event("test-event", "SUBID#1", "test payload");

    // Execute
    eventDispatcher.dispatchEvent(event);

    // Assert
    assertThat(eventDispatcher.eventQueue).isNotEmpty();
  }
}
