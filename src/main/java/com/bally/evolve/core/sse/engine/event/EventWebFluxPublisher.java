package com.bally.evolve.core.sse.engine.event;

import com.bally.evolve.core.sse.engine.SseSubscription;
import com.bally.evolve.core.sse.engine.event.model.Event;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.publisher.Sinks.Many;

@Slf4j
public class EventWebFluxPublisher extends EventPublisher {

  @Autowired
  // @Qualifier("sseManyFluxRegistry")
  Map<String, Collection<SseSubscription<Many<ServerSentEvent<String>>>>> sseRegistry;

  @Override
  protected int publish(Event event) {
    String susbscriptionId = event.getSubscriptionId();
    Collection<SseSubscription<Many<ServerSentEvent<String>>>> listSseSubscription =
        sseRegistry.computeIfAbsent(susbscriptionId, key -> new ConcurrentLinkedQueue<>());
    AtomicInteger publishCount = new AtomicInteger();
    listSseSubscription.forEach(
        sseSubscription -> {
          ServerSentEvent<String> serverSentEvent =
              ServerSentEvent.<String>builder()
                  .id(event.getEventId())
                  .event(event.getName())
                  .data(event.toString())
                  .build();
          EmitResult emitResult = sseSubscription.getSubscription().tryEmitNext(serverSentEvent);
          log.info("SseManyFlux->tryEmitNext() result: " + emitResult.isSuccess());
          if (emitResult.isSuccess()) {
            publishCount.incrementAndGet();
            log.info("SseManyFlux->tryEmitNext() succeeded for event: " + event.toString());
            Consumer<SseSubscription<Many<ServerSentEvent<String>>>> subscriptionPublished =
                sseSubscription.getSubscriptionPublished();
            if (subscriptionPublished != null) {
              sseSubscription.setCurrentEvent(event);
              CompletableFuture.runAsync(
                  () -> {
                    subscriptionPublished.accept(sseSubscription);
                  });
            }
          } else {
            log.error("SseManyFlux->tryEmitNext() failed for event: " + event.toString());
          }
        });
    return publishCount.get();
  }
}
