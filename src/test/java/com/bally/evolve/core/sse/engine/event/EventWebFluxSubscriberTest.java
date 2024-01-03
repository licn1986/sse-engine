package com.bally.evolve.core.sse.engine.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bally.evolve.core.sse.engine.SseSubscription;
import com.bally.evolve.core.sse.engine.config.SseEngineBeanConfig;
import com.bally.evolve.core.sse.engine.config.SseEngineWebFluxBeanConfig;
import com.bally.evolve.core.sse.engine.event.model.DelayedEvent;
import com.bally.evolve.core.sse.engine.event.model.Event;
import com.bally.evolve.core.sse.engine.exception.InvalidSubscriptionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.DelayQueue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks.Many;

@SpringBootTest(classes = {SseEngineBeanConfig.class, SseEngineWebFluxBeanConfig.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class EventWebFluxSubscriberTest {

  @Autowired
  @Qualifier("eventSubscriber")
  EventSubscriber<Many<ServerSentEvent<String>>> eventSubscriber;

  @Autowired DelayQueue<DelayedEvent<Event>> eventQueue;

  @Test
  void testSubscribeFlux_NullSubscription() {
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
  void testSubscribeFlux_RegistryContainedFlux() throws InvalidSubscriptionException {
    // setup
    String subscriptionId = "ID#1";

    // execute
    Flux<ServerSentEvent<String>> eventFlux =
        eventSubscriber.subscribe(subscriptionId).getSubscription().asFlux();

    // verify
    Collection<SseSubscription<Many<ServerSentEvent<String>>>> collectionSseSubscription =
        eventSubscriber.lookupSubscription(subscriptionId);
    Collection<Flux<ServerSentEvent<String>>> collectionFlux = new ArrayList<>();
    collectionSseSubscription.stream()
        .forEach(
            sseSub -> {
              collectionFlux.add(sseSub.getSubscription().asFlux());
            });

    assertTrue(
        collectionFlux.contains(eventFlux),
        String.format("The flux for subscripition %s not found.", subscriptionId));
  }
}
