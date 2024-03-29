package com.aonufrei.gallerywebapp.controller.rest;

import com.aonufrei.gallerywebapp.dto.AccountInDto;
import com.aonufrei.gallerywebapp.dto.response.account.AccountResponse;
import com.aonufrei.gallerywebapp.dto.response.account.AccountTokenResponse;
import com.aonufrei.gallerywebapp.dto.response.account.IsAccountResponse;
import com.aonufrei.gallerywebapp.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Accounts", description = "Account API")
@RequestMapping("api/v1/auth")
public class AccountRestController {

	private final Logger log = LoggerFactory.getLogger(AccountRestController.class);

	private final AccountService accountService;

	public AccountRestController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping("register")
	@Operation(summary = "Register account", description = "Is used to create user account")
	public ResponseEntity<IsAccountResponse> registerAccount(@RequestBody AccountInDto accountInDto) {
		try {
			accountService.createAccount(accountInDto);
			return ResponseEntity.ok(new AccountResponse("Account created successfully", false));
		} catch (Throwable t) {
			log.error("Account registration error", t);
			return ResponseEntity.badRequest().body(new AccountResponse("Failed to create account", t.getMessage(), true));
		}
	}

	@PostMapping("login")
	@Operation(summary = "Login into the account", description = "Is used to login. Returns authorization token")
	public ResponseEntity<IsAccountResponse> getAuthKey(@RequestBody AccountInDto accountInDto) {
		try {
			String token = accountService.getAuthToken(accountInDto.getUsername(), accountInDto.getPassword());
			return ResponseEntity.ok(new AccountTokenResponse(token));
		} catch (Throwable t) {
			log.error("Auth key creation error", t);
			return ResponseEntity.badRequest().body(new AccountTokenResponse(null, t.getMessage(), true));
		}
	}
}
