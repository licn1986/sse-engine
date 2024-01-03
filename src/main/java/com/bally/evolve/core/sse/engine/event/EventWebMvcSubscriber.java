package com.bally.evolve.core.sse.engine.event;

import com.bally.evolve.core.sse.engine.SseSubscription;
import com.bally.evolve.core.sse.engine.exception.InvalidSubscriptionException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventWebMvcSubscriber extends EventSubscriber<SseEmitter> {
  @Autowired
  // @Qualifier("sseEmitterRegistry")
  Map<String, Collection<SseSubscription<SseEmitter>>> sseRegistry;

  @Override
  public SseSubscription<SseEmitter> subscribe(String subscriptionId)
      throws InvalidSubscriptionException {
    return subscribe(subscriptionId, null, null);
  }

  @Override
  public SseSubscription<SseEmitter> subscribe(
      String subscriptionId,
      Consumer<SseSubscription<SseEmitter>> notifySubscriptionPublished,
      Consumer<SseSubscription<SseEmitter>> notifySubscriptionRemoved) {
    if (subscriptionId == null)
      throw new InvalidSubscriptionException("The param susbscriptionId cannot be null.");
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    SseSubscription<SseEmitter> sseSubscription =
        new SseSubscription<>(emitter, notifySubscriptionPublished, notifySubscriptionRemoved);
    sseRegistry
        .computeIfAbsent(subscriptionId, key -> new ConcurrentLinkedQueue<>())
        .add(sseSubscription);

    try {
      emitter.send(SseEmitter.event().id(subscriptionId).name("INIT"));
      log.info("SseEmitter init succeeded for subscriptionId: " + subscriptionId);
    } catch (IOException e) {
      log.error(
          "SseEmitter init exception for subscriptionId: "
              + subscriptionId
              + ", error: "
              + e.getMessage());
    }

    emitter.onTimeout(emitter::complete);
    emitter.onCompletion(() -> this.removeEmitter(subscriptionId, sseSubscription));
    emitter.onError(
        error -> {
          log.info("SseEmitter error for subscriptionId: " + subscriptionId + ", error: " + error);
          this.removeEmitter(subscriptionId, sseSubscription);
        });

    return sseSubscription;
  }

  protected void removeEmitter(
      final String subscriptionId, final SseSubscription<SseEmitter> sseSubscription) {
    Collection<SseSubscription<SseEmitter>> listSseSubscription = sseRegistry.get(subscriptionId);
    if (listSseSubscription != null) {
      listSseSubscription.remove(sseSubscription);
      log.info("Removed emitter for subscriptionId: " + subscriptionId);
    }
  }
}
