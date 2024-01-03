package com.bally.evolve.core.sse.engine.exception;

public class InvalidSubscriptionException extends RuntimeException {

  private static final long serialVersionUID = -8821449167987509160L;

  public InvalidSubscriptionException(String message) {
    super(message);
  }
}
