package com.bally.evolve.core.sse.engine.event;

import com.bally.evolve.core.sse.engine.SseSubscription;
import com.bally.evolve.core.sse.engine.event.model.Event;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventWebMvcPublisher extends EventPublisher {
  @Autowired
  // @Qualifier("sseEmitterRegistry")
  Map<String, Collection<SseSubscription<SseEmitter>>> sseRegistry;

  protected int publish(Event event) {
    String susbscriptionId = event.getSubscriptionId();
    Collection<SseSubscription<SseEmitter>> listSseSubscription =
        sseRegistry.computeIfAbsent(susbscriptionId, key -> new ConcurrentLinkedQueue<>());
    AtomicInteger publishCount = new AtomicInteger();

    listSseSubscription.forEach(
        sseSubscription -> {
          try {
            sseSubscription
                .getSubscription()
                .send(
                    SseEmitter.event()
                        .id(event.getEventId())
                        .name(event.getName())
                        .data(event.toString()));
            publishCount.incrementAndGet();
            log.info("SseEmitter->send() succeeded for event: " + event.toString());
            Consumer<SseSubscription<SseEmitter>> subscriptionPublished =
                sseSubscription.getSubscriptionPublished();
            if (subscriptionPublished != null) {
              sseSubscription.setCurrentEvent(event);
              subscriptionPublished.accept(sseSubscription);
            }
          } catch (IOException e) {
            log.error("SseEmitter->send() failed for event: " + event.toString());
          }
        });

    return publishCount.get();
  }
}
