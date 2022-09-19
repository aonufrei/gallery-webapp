package com.aonufrei.gallerywebapp.exceptions;

public class PictureNotFoundException extends RuntimeException {

	public PictureNotFoundException(String message) {
		super(message);
	}

	public PictureNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
