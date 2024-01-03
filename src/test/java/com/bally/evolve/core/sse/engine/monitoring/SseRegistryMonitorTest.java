package com.bally.evolve.core.sse.engine.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import com.bally.evolve.core.sse.engine.SseSubscription;
import com.bally.evolve.core.sse.engine.config.SseEngineBeanConfig;
import com.bally.evolve.core.sse.engine.config.SseEngineWebFluxBeanConfig;
import com.bally.evolve.core.sse.engine.event.EventWebFluxSubscriber;
import com.bally.evolve.core.sse.engine.exception.InvalidSubscriptionException;

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.annotation.DirtiesContext;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks.Many;

@SpringBootTest(classes = {SseEngineBeanConfig.class, SseEngineWebFluxBeanConfig.class})
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
class SseRegistryMonitorTest {

  @Autowired EventWebFluxSubscriber eventSubscriber;

  @Autowired SseRegistryMonitor sseRegistryMonitor;

  @Test
  void testCleanupEventFluxRegistry() throws InvalidSubscriptionException {
    // Setup
    String subscriptionId = "SUBID#1";
    SseSubscription<Many<ServerSentEvent<String>>> sseSubscription =
        eventSubscriber.subscribe(subscriptionId);
    assertThat(sseRegistryMonitor.sseRegistry.get(subscriptionId)).contains(sseSubscription);

    sseSubscription.getSubscription().tryEmitComplete();
    Awaitility.await()
        .pollDelay(1, TimeUnit.SECONDS)
        .untilAsserted(
            () ->
                Assertions.assertThat(sseRegistryMonitor.sseRegistry.get(subscriptionId))
                    .contains(sseSubscription));

    // Execute
    sseRegistryMonitor.cleanupEventFluxRegistry();

    // Assert
    assertThat(sseRegistryMonitor.sseRegistry).isEmpty();
  }

  @Test
  void testCleanupEventFluxRegistryWithCallback() throws InvalidSubscriptionException {
    // Setup
    SseRegistryMonitorTest registryMonitorTestSpy = spy(this);
    String subscriptionId = "SUBID#1";
    SseSubscription<Many<ServerSentEvent<String>>> sseSubscription =
        eventSubscriber.subscribe(
            subscriptionId,
            registryMonitorTestSpy::notifySubscriptionPublished,
            registryMonitorTestSpy::notifySubscriptionRemoved);
    assertThat(sseRegistryMonitor.sseRegistry.get(subscriptionId)).contains(sseSubscription);

    sseSubscription.getSubscription().tryEmitComplete();
    Awaitility.await()
        .pollDelay(1, TimeUnit.SECONDS)
        .untilAsserted(
            () ->
                Assertions.assertThat(sseRegistryMonitor.sseRegistry.get(subscriptionId))
                    .contains(sseSubscription));

    // Execute
    sseRegistryMonitor.cleanupEventFluxRegistry();

    // Assert
    assertThat(sseRegistryMonitor.sseRegistry).isEmpty();
    Mockito.verify(registryMonitorTestSpy, times(0)).notifySubscriptionPublished(sseSubscription);
    Mockito.verify(registryMonitorTestSpy, times(1)).notifySubscriptionRemoved(sseSubscription);
  }

  void notifySubscriptionPublished(SseSubscription<Many<ServerSentEvent<String>>> sseSubscription) {
    log.info(
        "Notification of the subscription publishing. manySse: {"
            + sseSubscription.getSubscription()
            + "}, event: "
            + sseSubscription.getCurrentEvent());
  }

  void notifySubscriptionRemoved(SseSubscription<Many<ServerSentEvent<String>>> sseSubscription) {
    log.info(
        "Notification of the subscription removal. manySse: {"
            + sseSubscription.getSubscription()
            + "}");
  }
}
