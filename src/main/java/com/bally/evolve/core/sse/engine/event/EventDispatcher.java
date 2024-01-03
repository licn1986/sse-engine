package com.bally.evolve.core.sse.engine.event;

import java.util.concurrent.DelayQueue;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.bally.evolve.core.sse.engine.SseEngineProperties;
import com.bally.evolve.core.sse.engine.event.model.DelayedEvent;
import com.bally.evolve.core.sse.engine.event.model.Event;

@Component
public class EventDispatcher {

	@Autowired
	@Qualifier("sseEngineProps")
	SseEngineProperties sseEngineProps;
	@Autowired
	@Qualifier("eventQueue")
	DelayQueue<DelayedEvent<Event>> eventQueue;

	public void dispatchEvent(Event event) {
		this.dispatchEvent(event, null, null);
	}

	public void dispatchEvent(Event event, Consumer<Event> notifyEventSent, Consumer<Event> notifyEventExpired) {
		eventQueue.put(new DelayedEvent<>(event, sseEngineProps.getEventTimeout(), notifyEventSent, notifyEventExpired));
	}

}
