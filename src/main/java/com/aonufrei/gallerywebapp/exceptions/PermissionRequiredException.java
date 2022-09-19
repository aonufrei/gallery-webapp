package com.aonufrei.gallerywebapp.exceptions;

public class PermissionRequiredException extends RuntimeException {

	public PermissionRequiredException(String message) {
		super(message);
	}

	public PermissionRequiredException(String message, Throwable cause) {
		super(message, cause);
	}
}
