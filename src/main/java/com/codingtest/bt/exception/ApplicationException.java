package com.codingtest.bt.exception;

public class ApplicationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ApplicationException(String message, Exception e) {
		super(message, e);
	}

	public ApplicationException(String message) {
		super(message);
	}

}
