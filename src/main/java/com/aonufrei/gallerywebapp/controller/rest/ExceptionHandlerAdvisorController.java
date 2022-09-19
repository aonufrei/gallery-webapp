package com.aonufrei.gallerywebapp.controller.rest;

import com.aonufrei.gallerywebapp.dto.response.ErrorResponse;
import com.aonufrei.gallerywebapp.exceptions.GeneralRequestError;
import com.aonufrei.gallerywebapp.exceptions.PermissionRequiredException;
import com.aonufrei.gallerywebapp.exceptions.PictureNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerAdvisorController extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = GeneralRequestError.class)
	protected ResponseEntity<ErrorResponse> handleGeneralError(GeneralRequestError generalRequestError) {
		return ResponseEntity.badRequest().body(new ErrorResponse(generalRequestError.getMessage(), true));
	}

	@ExceptionHandler(value = PictureNotFoundException.class)
	protected ResponseEntity<ErrorResponse> handlePictureNotFoundError(PictureNotFoundException pictureNotFoundError) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(pictureNotFoundError.getMessage(), true));
	}

	@ExceptionHandler(value = PermissionRequiredException.class)
	protected ResponseEntity<ErrorResponse> handlePictureNotFoundError(PermissionRequiredException pictureNotFoundError) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(pictureNotFoundError.getMessage(), true));
	}

}
