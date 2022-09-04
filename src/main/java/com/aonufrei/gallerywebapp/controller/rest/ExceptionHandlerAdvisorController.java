package com.aonufrei.gallerywebapp.controller.rest;

import com.aonufrei.gallerywebapp.dto.response.ErrorResponse;
import com.aonufrei.gallerywebapp.exceptions.GeneralRequestError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerAdvisorController extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = GeneralRequestError.class)
	protected ResponseEntity<ErrorResponse> handleGeneralError(GeneralRequestError generalRequestError) {
		return ResponseEntity.badRequest().body(new ErrorResponse(generalRequestError.getMessage()));
	}

}
