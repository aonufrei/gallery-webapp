package com.aonufrei.gallerywebapp.exceptions;

public class AccountNotFoundException extends RuntimeException {

	public AccountNotFoundException() {
	}

	public AccountNotFoundException(String message) {
		super(message);
	}

	public AccountNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccountNotFoundException(Throwable cause) {
		super(cause);
	}

}
