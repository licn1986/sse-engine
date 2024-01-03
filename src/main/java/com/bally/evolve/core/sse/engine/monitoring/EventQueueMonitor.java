package com.bally.evolve.core.sse.engine.monitoring;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.bally.evolve.core.sse.engine.event.model.DelayedEvent;
import com.bally.evolve.core.sse.engine.event.model.Event;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EventQueueMonitor {

	@Autowired
	@Qualifier("eventQueue")
	DelayQueue<DelayedEvent<Event>> eventQueue;

	public void cleanupDelayedEvents() {
		DelayedEvent<Event> expiredEvent = null;
		while ((expiredEvent = eventQueue.poll()) != null) {
			Event eventObject = expiredEvent.getEventObject();
			Consumer<Event> notifyEventExpired = expiredEvent.getNotifyEventExpired();
			if (notifyEventExpired != null) {
				CompletableFuture.runAsync(() -> {
					notifyEventExpired.accept(eventObject);
				});
			}
			log.info("Cleaned up expired event: " + expiredEvent);
		}
	}

}
