package com.bally.evolve.core.sse.engine.event;

import com.bally.evolve.core.sse.engine.SseEngineProperties;
import com.bally.evolve.core.sse.engine.SseSubscription;
import com.bally.evolve.core.sse.engine.event.model.DelayedEvent;
import com.bally.evolve.core.sse.engine.event.model.Event;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class EventSubscriber<T> {

  @Autowired
  @Qualifier("sseEngineProps")
  SseEngineProperties sseEngineProps;

  @Autowired
  @Qualifier("eventQueue")
  DelayQueue<DelayedEvent<Event>> eventQueue;

  @Autowired Map<String, Collection<SseSubscription<T>>> sseRegistry;

  public abstract SseSubscription<T> subscribe(String subscriptionId);

  public abstract SseSubscription<T> subscribe(
      String subscriptionId,
      Consumer<SseSubscription<T>> notifySubscriptionPublished,
      Consumer<SseSubscription<T>> notifySubscriptionRemoved);

  public Collection<SseSubscription<T>> lookupSubscription(String subscriptionId) {
    return sseRegistry.get(subscriptionId);
  }

  public void remove(final String subscriptionId, final SseSubscription<T> sseSubscription) {
    Collection<SseSubscription<T>> listSseSubscription = sseRegistry.get(subscriptionId);
    if (listSseSubscription != null) {
      listSseSubscription.remove(sseSubscription);
      log.info("Removed emitter for subscriptionId: " + subscriptionId);
    }
  }
}
