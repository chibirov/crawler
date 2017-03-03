package com.wiprodigital.trial.exception;

public class UnknowHostException extends RuntimeException {
	
	public UnknowHostException(String message, Exception e) {
		super(message, e);
	}
}
