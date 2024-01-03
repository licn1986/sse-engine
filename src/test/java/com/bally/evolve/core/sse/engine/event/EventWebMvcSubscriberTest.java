package com.bally.evolve.core.sse.engine.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bally.evolve.core.sse.engine.SseSubscription;
import com.bally.evolve.core.sse.engine.config.SseEngineBeanConfig;
import com.bally.evolve.core.sse.engine.config.SseEngineWebMvcBeanConfig;
import com.bally.evolve.core.sse.engine.event.model.DelayedEvent;
import com.bally.evolve.core.sse.engine.event.model.Event;
import com.bally.evolve.core.sse.engine.exception.InvalidSubscriptionException;

import java.util.Collection;
import java.util.concurrent.DelayQueue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@SpringBootTest(classes = {SseEngineBeanConfig.class, SseEngineWebMvcBeanConfig.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class EventWebMvcSubscriberTest {
  @Autowired
  @Qualifier("eventSubscriber")
  EventSubscriber<SseEmitter> eventSubscriber;

  @Autowired DelayQueue<DelayedEvent<Event>> eventQueue;

  @Test
  void testSubscribeEmitter_NullSubscription() {
    // setup
    String subscriptionId = null;

    // execute & assert
    assertThatThrownBy(
            () -> {
              eventSubscriber.subscribe(subscriptionId);
            })
        .isInstanceOf(InvalidSubscriptionException.class)
        .hasMessage("The param susbscriptionId cannot be null.");
  }

  @Test
  void testSubscribeEmitter_RegistryContainedEmitter() throws InvalidSubscriptionException {
    // setup
    String subscriptionId = "ID#1";

    // execute
    SseSubscription<SseEmitter> sseSubscription = eventSubscriber.subscribe(subscriptionId);

    // verify
    Collection<SseSubscription<SseEmitter>> collectionSseSubscription =
        eventSubscriber.lookupSubscription(subscriptionId);
    assertThat(sseSubscription).isNotNull();
    assertTrue(
        collectionSseSubscription.contains(sseSubscription),
        String.format("The emitter for subscripition %s not found.", subscriptionId));
  }

  @Test
  void testRemoveEmitter() throws InvalidSubscriptionException {
    // Setup
    String subscriptionId = "SUBID#1";
    SseSubscription<SseEmitter> sseSubscription = eventSubscriber.subscribe(subscriptionId);
    assertThat(sseSubscription).isNotNull();
    assertThat(eventSubscriber.lookupSubscription(subscriptionId)).contains(sseSubscription);

    // Execute
    eventSubscriber.remove(subscriptionId, sseSubscription);

    // Assert
    assertThat(eventSubscriber.lookupSubscription(subscriptionId)).doesNotContain(sseSubscription);
  }
}
