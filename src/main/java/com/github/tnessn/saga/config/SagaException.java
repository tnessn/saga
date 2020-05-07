package com.github.tnessn.saga.config;

public class SagaException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SagaException(String message) {
		super(message);
	}

	public SagaException(Throwable cause) {
		super(cause);
	}

	public SagaException(String cause, Throwable throwable) {
		super(cause, throwable);
	}
}