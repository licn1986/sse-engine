package com.bally.evolve.core.sse.engine.controller;

import com.bally.evolve.core.sse.engine.SseSubscription;
import com.bally.evolve.core.sse.engine.event.EventDispatcher;
import com.bally.evolve.core.sse.engine.event.EventWebMvcSubscriber;
import com.bally.evolve.core.sse.engine.event.model.Event;
import com.bally.evolve.core.sse.engine.exception.InvalidSubscriptionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks.Many;

// @RestController
@RequestMapping(path = "/sse-engine")
@CrossOrigin(
    origins = "*",
    methods = {
      RequestMethod.GET,
      RequestMethod.POST,
      RequestMethod.PUT,
      RequestMethod.DELETE,
      RequestMethod.PATCH,
      RequestMethod.OPTIONS
    },
    allowedHeaders = "X-Requested-With, content-type, Authorization")
@Slf4j
public class SseEngineController {

  @Autowired EventDispatcher eventDispatcher;

  @Autowired EventWebMvcSubscriber eventSubscriber;

  @GetMapping(value = "/stream-emitter-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamWebMvcEvents(@RequestParam String subscriptionId)
      throws InvalidSubscriptionException {
    // 4 Standard Parts of message: Id, event, retry, data
    log.info("mvc stream....");
    return eventSubscriber
        .subscribe(
            subscriptionId,
            this::notifyEmitterSubscriptionPublished,
            this::notifyEmitterSubscriptionRemoved)
        .getSubscription();
  }

  //  @GetMapping(value = "/stream-flux-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  //  public Flux<ServerSentEvent<String>> streamWebFluxEvents(@RequestParam String subscriptionId)
  //      throws InvalidSubscriptionException {
  //    // 4 Standard Parts of message: Id, event, retry, data
  //
  //    return eventSubscriber
  //        .subscribe(
  //            subscriptionId,
  //            this::notifyFluxSubscriptionPublished,
  //            this::notifyFluxSubscriptionRemoved)
  //        .getSubscription()
  //        .asFlux();
  //  }

  @PostMapping(value = "/dispatch-event")
  public void dispatchEvent(
      @RequestParam String subscriptionId,
      @RequestParam String name,
      @RequestParam String eventPayload) {
    Event event = new Event(name, subscriptionId, eventPayload);
    eventDispatcher.dispatchEvent(event, this::notifyEventSent, this::notifyEventExpired);
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
