package com.aonufrei.gallerywebapp.exceptions;

public class InvalidAccountIdFormatException extends RuntimeException {

	public InvalidAccountIdFormatException() {
	}

	public InvalidAccountIdFormatException(String message) {
		super(message);
	}

	public InvalidAccountIdFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidAccountIdFormatException(Throwable cause) {
		super(cause);
	}

}
