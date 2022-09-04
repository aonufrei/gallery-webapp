package com.aonufrei.gallerywebapp.exceptions;

public class GeneralRequestError extends RuntimeException {

	public GeneralRequestError(String message) {
		super(message);
	}

	public GeneralRequestError(String message, Throwable cause) {
		super(message, cause);
	}

	public GeneralRequestError(Throwable cause) {
		super(cause);
	}

}
