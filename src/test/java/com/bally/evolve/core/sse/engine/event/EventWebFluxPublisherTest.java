package com.bally.evolve.core.sse.engine.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import com.bally.evolve.core.sse.engine.SseSubscription;
import com.bally.evolve.core.sse.engine.config.SseEngineBeanConfig;
import com.bally.evolve.core.sse.engine.config.SseEngineWebFluxBeanConfig;
import com.bally.evolve.core.sse.engine.event.model.Event;
import com.bally.evolve.core.sse.engine.exception.InvalidSubscriptionException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.publisher.Sinks.Many;
import reactor.core.publisher.Sinks.ManySpec;
import reactor.core.publisher.Sinks.UnicastSpec;

@SpringBootTest(classes = {SseEngineBeanConfig.class, SseEngineWebFluxBeanConfig.class})
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
class EventWebFluxPublisherTest {

  @Autowired EventDispatcher eventDispatcher;

  @Autowired EventWebFluxSubscriber eventSubscriber;

  @Autowired EventPublisher eventPublisher;

  @BeforeEach
  void setup() {
    Awaitility.await().untilTrue(isEventQueueClearedUp());
  }

  private AtomicBoolean isEventQueueClearedUp() {
    eventPublisher.eventQueue.clear();
    return new AtomicBoolean(eventPublisher.eventQueue.isEmpty());
  }

  @Test
  void testPublishEvents_sseManySinkSubscription()
      throws InvalidSubscriptionException, IOException {
    // Setup
    String subscriptionId = "SUBID#1";
    Event event = new Event("test-event", subscriptionId, "test payload");
    EventWebFluxPublisherTest eventPublisherTestSpy = spy(this);
    eventDispatcher.dispatchEvent(
        event, eventPublisherTestSpy::notifyEventSent, eventPublisherTestSpy::notifyEventExpired);
    assertThat(eventPublisher.eventQueue.element().getEventObject()).isEqualTo(event);
    ManySpec manySpec = Mockito.mock(ManySpec.class);
    UnicastSpec unicastSpec = Mockito.mock(UnicastSpec.class);
    Many<Object> many = Mockito.mock(Many.class);
    EmitResult emitResult = Mockito.mock(EmitResult.class);

    try (MockedStatic<Sinks> mockStatic = Mockito.mockStatic(Sinks.class)) {
      mockStatic.when(() -> Sinks.many()).thenReturn(manySpec);
      Mockito.when(manySpec.unicast()).thenReturn(unicastSpec);
      Mockito.when(unicastSpec.onBackpressureBuffer()).thenReturn(many);
      Mockito.when(many.tryEmitNext(ArgumentMatchers.any())).thenReturn(emitResult);
      Mockito.when(emitResult.isSuccess()).thenReturn(true);

      SseSubscription<Many<ServerSentEvent<String>>> sseSubscription =
          eventSubscriber.subscribe(
              subscriptionId,
              eventPublisherTestSpy::notifyFluxSubscriptionPublished,
              eventPublisherTestSpy::notifyFluxSubscriptionRemoved);
      assertThat(sseSubscription.getCurrentEvent()).isNull();

      // Execute
      eventPublisher.publishEvents();
      Awaitility.await().untilAsserted(() -> assertThat(eventPublisher.eventQueue).isEmpty());

      // Assert
      // assertThat(eventPublisher.eventQueue);
      assertThat(sseSubscription.getCurrentEvent()).isNotNull();
      Mockito.verify(sseSubscription.getSubscription(), times(2))
          .tryEmitNext(ArgumentMatchers.any());
      Mockito.verify(eventPublisherTestSpy, times(1)).notifyEventSent(event);
      Mockito.verify(eventPublisherTestSpy, times(0)).notifyEventExpired(event);
      Mockito.verify(eventPublisherTestSpy, times(1))
          .notifyFluxSubscriptionPublished(sseSubscription);
      Mockito.verify(eventPublisherTestSpy, times(0))
          .notifyFluxSubscriptionRemoved(sseSubscription);
    }
  }

  void notifyEventSent(Event event) {
    log.info("Notification of the event sent. " + event.toString());
  }

  void notifyEventExpired(Event event) {
    log.info("Notification of the event expired. " + event.toString());
  }

  void notifyFluxSubscriptionPublished(
      SseSubscription<Many<ServerSentEvent<String>>> sseSubscription) {
    log.info(
        "Notification of the subscription publishing. manySse: {"
            + sseSubscription.getSubscription()
            + "}, event: "
            + sseSubscription.getCurrentEvent());
  }

  void notifyFluxSubscriptionRemoved(
      SseSubscription<Many<ServerSentEvent<String>>> sseSubscription) {
    log.info(
        "Notification of the subscription removal. manySse: {"
            + sseSubscription.getSubscription()
            + "}");
  }
}
