package com.aonufrei.gallerywebapp.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountInDto {

	private String username;

	private String password;

}
