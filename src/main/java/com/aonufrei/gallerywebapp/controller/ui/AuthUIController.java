package com.aonufrei.gallerywebapp.controller.ui;

import com.aonufrei.gallerywebapp.dto.AccountInDto;
import com.aonufrei.gallerywebapp.service.AccountService;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Hidden
@Controller
public class AuthUIController {

	private final AccountService accountService;

	private final Logger LOG = LoggerFactory.getLogger(AuthUIController.class);
	public AuthUIController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PreAuthorize("isAnonymous()")
	@GetMapping("login-page")
	public String showLoginPage(Model model) {
		model.addAttribute("logged", SecurityContextHolder.getContext().getAuthentication() != null);
		return "login";
	}

	@PreAuthorize("isAnonymous()")
	@GetMapping("login-error")
	public String loginError(Model model) {
		model.addAttribute("loginError", true);
		return "login";
	}

	@PreAuthorize("isAnonymous()")
	@GetMapping("register")
	public String showRegisterPage() {
		return "register";
	}

	@PreAuthorize("isAnonymous()")
	@PostMapping("register")
	public String createAccount(@RequestParam("username") String username, @RequestParam("password") String password, Model model) {
		try {
			accountService.createAccount(new AccountInDto(username, password));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			model.addAttribute("errorMessage", e.getMessage());
			return "register";
		}
		return "gallery";
	}

}
