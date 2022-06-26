package com.aonufrei.gallerywebapp.controller.ui;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthUIController {

	@GetMapping("login-page")
	public String showLoginPage(Model model) {
		model.addAttribute("logged", SecurityContextHolder.getContext().getAuthentication() != null);
		return "login";
	}

	@GetMapping("login-error")
	public String loginError(Model model) {
		model.addAttribute("loginError", true);
		return "login";
	}


}
