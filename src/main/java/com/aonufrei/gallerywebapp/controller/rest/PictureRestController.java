package com.aonufrei.gallerywebapp.controller.rest;

import com.aonufrei.gallerywebapp.dto.PictureInfoDto;
import com.aonufrei.gallerywebapp.dto.PictureOutDto;
import com.aonufrei.gallerywebapp.dto.ResponseDto;
import com.aonufrei.gallerywebapp.service.PictureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/v1/gallery")
public class PictureRestController {

	private final Logger LOG = LoggerFactory.getLogger(PictureRestController.class);

	private final PictureService pictureService;

	private final HttpServletRequest request;

	public PictureRestController(PictureService pictureService, HttpServletRequest request) {
		this.pictureService = pictureService;
		this.request = request;
	}

	@GetMapping("specific")
	private ResponseEntity<byte[]> provideUserPicture(@RequestParam("pic") String token) {
		LOG.info("Token provided: " + token);
		try {
			String imagePath = pictureService.convertPictureTokenToPath(token);
			return ResponseEntity.ok(pictureService.getPictureFromPath(imagePath));
		} catch (Throwable e) {
			LOG.error("Cannot provide user a picture", e);
			return ResponseEntity.badRequest().build();
		}
	}

}
