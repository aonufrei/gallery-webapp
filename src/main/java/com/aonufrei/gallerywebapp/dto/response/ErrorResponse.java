package com.aonufrei.gallerywebapp.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {

	private String message;

	@JsonProperty("is_error")
	private boolean isError;

	public ErrorResponse(String message) {
		this.message = message;
	}
}
