package com.aonufrei.gallerywebapp.dto.response.account;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccountTokenResponse implements IsAccountResponse {

	private String token;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String message;

	@JsonProperty("is_error")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean isError;


	public AccountTokenResponse(String token) {
		this.token = token;
	}

	public AccountTokenResponse(String token, String message, Boolean isError) {
		this.token = token;
		this.isError = isError;
		this.message = message;
	}

}
