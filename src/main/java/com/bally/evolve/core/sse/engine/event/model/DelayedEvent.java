package com.bally.evolve.core.sse.engine.event.model;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Getter;

@Getter
public class DelayedEvent<T> implements Delayed {

	private T eventObject;
	private Long expiryTime;

	private Consumer<T> notifyEventSent;

	private Consumer<T> notifyEventExpired;

	// Constructor of DelayObject
	public DelayedEvent(T eventObject, long delayTime, Consumer<T> notifyEventSent, Consumer<T> notifyEventExpired) {
		this.eventObject = eventObject;
		this.expiryTime = System.currentTimeMillis() + delayTime;
		this.notifyEventSent = notifyEventSent;
		this.notifyEventExpired = notifyEventExpired;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		long diff = expiryTime - System.currentTimeMillis();
		return unit.convert(diff, TimeUnit.MILLISECONDS);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(Delayed obj) {
		return this.getExpiryTime().compareTo(((DelayedEvent<T>) obj).getExpiryTime());
	}

	@Override
	public String toString() {
		return "\n{" + "eventObject: " + eventObject + ", expiryTime: " + expiryTime + "}";
	}
}
