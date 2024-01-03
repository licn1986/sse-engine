package com.bally.evolve.core.sse.engine.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import com.bally.evolve.core.sse.engine.SseSubscription;
import com.bally.evolve.core.sse.engine.config.SseEngineBeanConfig;
import com.bally.evolve.core.sse.engine.config.SseEngineWebMvcBeanConfig;
import com.bally.evolve.core.sse.engine.event.model.Event;
import com.bally.evolve.core.sse.engine.exception.InvalidSubscriptionException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {SseEngineBeanConfig.class, SseEngineWebMvcBeanConfig.class})
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
class EventWebMvcPublisherTest {
  @Autowired EventDispatcher eventDispatcher;

  @Autowired EventWebMvcSubscriber eventSubscriber;

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
  void testPublishEvents_sseEmitterSubscription() throws InvalidSubscriptionException, IOException {
    // Setup
    String subscriptionId = "SUBID#1";
    Event event = new Event("test-event", subscriptionId, "test payload");
    EventWebMvcPublisherTest eventPublisherTestSpy = spy(this);
    eventDispatcher.dispatchEvent(
        event, eventPublisherTestSpy::notifyEventSent, eventPublisherTestSpy::notifyEventExpired);
    assertThat(eventPublisher.eventQueue.element().getEventObject()).isEqualTo(event);

    try (MockedConstruction<SseEmitter> mockConstruction =
        Mockito.mockConstruction(SseEmitter.class)) {
      SseSubscription<SseEmitter> sseSubscription =
          eventSubscriber.subscribe(
              subscriptionId,
              eventPublisherTestSpy::notifyEmitterSubscriptionPublished,
              eventPublisherTestSpy::notifyEmitterSubscriptionRemoved);
      assertThat(sseSubscription.getCurrentEvent()).isNull();

      // Execute
      eventPublisher.publishEvents();

      // Assert
      assertThat(eventPublisher.eventQueue).isEmpty();
      assertThat(sseSubscription.getCurrentEvent()).isNotNull();
      Mockito.verify(sseSubscription.getSubscription(), times(2))
          .send(ArgumentMatchers.any(SseEventBuilder.class));
      Mockito.verify(eventPublisherTestSpy, times(1)).notifyEventSent(event);
      Mockito.verify(eventPublisherTestSpy, times(0)).notifyEventExpired(event);
      Mockito.verify(eventPublisherTestSpy, times(1))
          .notifyEmitterSubscriptionPublished(sseSubscription);
      Mockito.verify(eventPublisherTestSpy, times(0))
          .notifyEmitterSubscriptionRemoved(sseSubscription);
    }
  }

  void notifyEventSent(Event event) {
    log.info("Notification of the event sent. " + event.toString());
  }

  void notifyEventExpired(Event event) {
    log.info("Notification of the event expired. " + event.toString());
  }

  void notifyEmitterSubscriptionPublished(SseSubscription<SseEmitter> sseSubscription) {
    log.info(
        "Notification of the subscription publishing. manySse: {"
            + sseSubscription.getSubscription()
            + "}, event: "
            + sseSubscription.getCurrentEvent());
  }

  void notifyEmitterSubscriptionRemoved(SseSubscription<SseEmitter> sseSubscription) {
    log.info(
        "Notification of the subscription removal. manySse: {"
            + sseSubscription.getSubscription()
            + "}");
  }
}
