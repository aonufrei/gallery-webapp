package com.aonufrei.gallerywebapp.controller.ui;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainUIController {

	@GetMapping(value = {"", "home"})
	private String homepage(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean isAuthenticated = !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
		System.out.println(isAuthenticated);

		model.addAttribute("logged", isAuthenticated);
		return "index";
	}

	@GetMapping("gallery")
	private String redirectToUserPage(Model model) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails) principal).getUsername();
		model.addAttribute("user", username);
		return "gallery";
	}

}
