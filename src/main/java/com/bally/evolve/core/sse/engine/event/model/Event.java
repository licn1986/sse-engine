package com.bally.evolve.core.sse.engine.event.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Event implements Serializable {
	private static final long serialVersionUID = -5443632528329352549L;

	@JsonProperty("eventId")
	private String eventId;

	@JsonProperty("subscriptionId")
	private String subscriptionId;

	@JsonProperty("name")
	private String name;

	@JsonProperty("payload")
	private String payload;

	public Event() {

	}

	public Event(String eventId, String name, String subscriptionId, String payload) {
		this.setEventId(eventId);
		this.setName(name);
		this.setSubscriptionId(subscriptionId);
		this.setPayload(payload);
	}

	public Event(String name, String subscriptionId, String payload) {
		long nextLong = new Random().nextLong();
		this.setEventId("" + (nextLong < 0 ? Math.abs(nextLong) : nextLong));
		this.setName(name);
		this.setSubscriptionId(subscriptionId);
		this.setPayload(payload);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		return Objects.equals(eventId, other.eventId) && Objects.equals(name, other.name)
				&& Objects.equals(subscriptionId, other.subscriptionId) && Objects.equals(payload, other.payload);
	}

	public String toString() {
		return "{\"eventId\": \"" + eventId + "\", \"name\": \"" + name + "\", \"subscriptionId\": \"" + subscriptionId + "\", \"payload\": \""
				+ payload + "\"}";
	}
}
