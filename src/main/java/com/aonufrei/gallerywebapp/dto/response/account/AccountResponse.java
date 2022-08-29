package com.aonufrei.gallerywebapp.dto.response.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class AccountResponse implements IsAccountResponse {

	private String message;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String reason;

	@JsonProperty("is_error")
	private boolean isError;

	public AccountResponse(String message, boolean isError) {
		this.message = message;
		this.isError = isError;
	}

	public AccountResponse(String message, String reason, boolean isError) {
		this.message = message;
		this.reason = reason;
		this.isError = isError;
	}

}
