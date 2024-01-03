package com.bally.evolve.core.sse.engine.event;

import com.bally.evolve.core.sse.engine.SseSubscription;
import com.bally.evolve.core.sse.engine.exception.InvalidSubscriptionException;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.publisher.Sinks.Many;

@Slf4j
public class EventWebFluxSubscriber extends EventSubscriber<Many<ServerSentEvent<String>>> {

  @Autowired
  // @Qualifier("sseManyFluxRegistry")
  Map<String, Collection<SseSubscription<Many<ServerSentEvent<String>>>>> sseRegistry;

  @Override
  public SseSubscription<Many<ServerSentEvent<String>>> subscribe(String subscriptionId) {
    return subscribe(subscriptionId, null, null);
  }

  @Override
  public SseSubscription<Many<ServerSentEvent<String>>> subscribe(
      String subscriptionId,
      Consumer<SseSubscription<Many<ServerSentEvent<String>>>> notifySubscriptionPublished,
      Consumer<SseSubscription<Many<ServerSentEvent<String>>>> notifySubscriptionRemoved) {
    if (subscriptionId == null)
      throw new InvalidSubscriptionException("The param susbscriptionId cannot be null.");

    log.info("webflux subscriber!!!!!");
    Many<ServerSentEvent<String>> sinkMany = Sinks.many().unicast().onBackpressureBuffer();
    SseSubscription<Many<ServerSentEvent<String>>> sseSubscription =
        new SseSubscription<>(sinkMany, notifySubscriptionPublished, notifySubscriptionRemoved);
    sseRegistry
        .computeIfAbsent(subscriptionId, key -> new ConcurrentLinkedQueue<>())
        .add(sseSubscription);

    EmitResult emitResult =
        sinkMany.tryEmitNext(
            ServerSentEvent.<String>builder().id(subscriptionId).event("INIT").build());
    if (emitResult.isSuccess()) {
      log.info("SseMany init succeeded for subscriptionId: " + subscriptionId);
    } else {
      log.error("SseMany init failed for subscriptionId: " + subscriptionId);
    }
    return sseSubscription;
  }
}
