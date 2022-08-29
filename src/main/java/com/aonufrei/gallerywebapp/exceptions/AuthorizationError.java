package com.aonufrei.gallerywebapp.exceptions;

public class AuthorizationError extends RuntimeException {

	public AuthorizationError() {
	}

	public AuthorizationError(String message) {
		super(message);
	}

	public AuthorizationError(String message, Throwable cause) {
		super(message, cause);
	}

}
