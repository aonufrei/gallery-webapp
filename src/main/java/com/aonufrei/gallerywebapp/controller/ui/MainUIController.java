package com.aonufrei.gallerywebapp.controller.ui;

import com.aonufrei.gallerywebapp.dto.PictureInfoDto;
import com.aonufrei.gallerywebapp.service.AccountService;
import com.aonufrei.gallerywebapp.service.PictureService;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Hidden
@Controller
public class MainUIController {

	private static final Logger LOG = LoggerFactory.getLogger(MainUIController.class);

	private final PictureService pictureService;
	private final AccountService accountService;

	public MainUIController(PictureService pictureService, AccountService accountService) {
		this.pictureService = pictureService;
		this.accountService = accountService;
	}

	@GetMapping(value = {"", "home"})
	private String homepage(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean isAuthenticated = !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();

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

	@GetMapping("add-picture")
	private String showAddPicturePage() {
		return "add-picture";
	}

	@PostMapping("add-picture")
	private String processAddPicture(@RequestParam("name") String name,
									 @RequestParam("owner_id") String ownerId,
									 @RequestParam(value = "is_public", required = false) Boolean isPublic,
									 @RequestParam("file") MultipartFile file) {
		try {
			Integer accountId = accountService.validateAccountId(ownerId);
			pictureService.savePicture(accountId, new PictureInfoDto(name, isPublic != null && isPublic), file);
		} catch (Exception e) {
			LOG.error("Error occurred when adding picture", e);
		}
		return "redirect:/home";
	}

}
