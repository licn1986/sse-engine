package com.bally.evolve.core.sse.engine.monitoring;

import com.bally.evolve.core.sse.engine.SseSubscription;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks.Many;

@Component
@Slf4j
public class SseRegistryMonitor {
  @Autowired
  // @Qualifier("sseManyFluxRegistry")
  Map<String, Collection<SseSubscription<Many<ServerSentEvent<String>>>>> sseRegistry;

  public void cleanupEventFluxRegistry() {
    sseRegistry.keySet().stream()
        .forEach(
            subscriptionId -> {
              Collection<SseSubscription<Many<ServerSentEvent<String>>>> listSseSubscription =
                  sseRegistry.get(subscriptionId);
              listSseSubscription.stream()
                  .forEach(
                      sseSubscription -> {
                        Many<ServerSentEvent<String>> subscription =
                            sseSubscription.getSubscription();
                        if (subscription.currentSubscriberCount() <= 0) {
                          sseRegistry.remove(subscriptionId);
                          Consumer<SseSubscription<Many<ServerSentEvent<String>>>>
                              subscriptionRemoved = sseSubscription.getSubscriptionRemoved();
                          if (subscriptionRemoved != null) {
                            CompletableFuture.runAsync(
                                () -> {
                                  subscriptionRemoved.accept(sseSubscription);
                                });
                          }
                          log.info(
                              subscription.stepName()
                                  + " susbcriber count is zero and is removed from registry.");
                        } else {
                          log.info(
                              subscription.stepName()
                                  + " susbcriber count: "
                                  + subscription.currentSubscriberCount());
                        }
                      });
            });
  }
}
