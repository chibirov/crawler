package com.wiprodigital.trial.exception;


public class InvalidUrlException extends RuntimeException {

	public InvalidUrlException(String message, Exception e) {
		super(message, e);
	}
}