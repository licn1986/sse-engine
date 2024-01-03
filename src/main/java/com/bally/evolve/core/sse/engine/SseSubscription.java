package com.bally.evolve.core.sse.engine;

import java.util.function.Consumer;
import com.bally.evolve.core.sse.engine.event.model.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SseSubscription<T> {

	private T subscription;
	private Event currentEvent;
	private Consumer<SseSubscription<T>> subscriptionPublished;
	private Consumer<SseSubscription<T>> subscriptionRemoved;

	public SseSubscription(T subscription, Consumer<SseSubscription<T>> subscriptionPublished,
			Consumer<SseSubscription<T>> subscriptionRemoved) {
		this.setSubscription(subscription);
		this.setSubscriptionPublished(subscriptionPublished);
		this.setSubscriptionRemoved(subscriptionRemoved);
	}
}
