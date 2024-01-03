package com.bally.evolve.core.sse.engine.event;

import com.bally.evolve.core.sse.engine.event.model.DelayedEvent;
import com.bally.evolve.core.sse.engine.event.model.Event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class EventPublisher {

  @Autowired
  @Qualifier("eventQueue")
  DelayQueue<DelayedEvent<Event>> eventQueue;

  public void publishEvents() {
    DelayedEvent<Event> currentEvent = null;
    DelayedEvent<Event> lastEvent = null;
    while ((currentEvent = eventQueue.peek()) != null && currentEvent != lastEvent) {
      Event eventObject = currentEvent.getEventObject();
      if (this.publish(eventObject) > 0) {
        boolean isRemoved = eventQueue.remove(currentEvent);
        if (isRemoved) {
          Consumer<Event> notifyEventSent = currentEvent.getNotifyEventSent();
          if (notifyEventSent != null) {
            CompletableFuture.runAsync(
                () -> {
                  notifyEventSent.accept(eventObject);
                });
            log.trace(
                "Removed published/sent event ('"
                    + currentEvent
                    + "') successfully from the event queue.");
          }
        } else {
          log.error(
              "Removing published/sent event ('"
                  + currentEvent
                  + "') from the event queue failed.");
        }
      }
      lastEvent = currentEvent;
    }
  }

  protected abstract int publish(Event event);
}
