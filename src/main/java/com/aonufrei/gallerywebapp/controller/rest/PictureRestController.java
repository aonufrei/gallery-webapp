package com.aonufrei.gallerywebapp.controller.rest;

import com.aonufrei.gallerywebapp.dto.PictureInfoDto;
import com.aonufrei.gallerywebapp.dto.PictureOutDto;
import com.aonufrei.gallerywebapp.exceptions.GeneralRequestError;
import com.aonufrei.gallerywebapp.service.PictureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/v1/picture")
public class PictureRestController {

	private final Logger log = LoggerFactory.getLogger(PictureRestController.class);

	private final PictureService pictureService;

	public PictureRestController(PictureService pictureService) {
		this.pictureService = pictureService;
	}

	@GetMapping("specific")
	private ResponseEntity<byte[]> provideUserPicture(@RequestParam("pic") String token) {
		log.info("Token provided: " + token);
		try {
			String imagePath = pictureService.convertPictureTokenToPath(token);
			return ResponseEntity.ok(pictureService.getPictureFromPath(imagePath));
		} catch (Throwable e) {
			log.error("Cannot provide user a picture", e);
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("feeds")
	public ResponseEntity<List<PictureOutDto>> getAllPublicImage(@RequestParam("page_size") Integer size,
	                                                             @RequestParam("page_number") Integer number) {
		List<PictureOutDto> allPublicImages = pictureService.getAllPublicImages(size, number);
		return ResponseEntity.ok(allPublicImages);
	}

	@GetMapping("{user}")
	private ResponseEntity<List<PictureOutDto>> getAllPicturesForUser(@PathVariable("user") Integer userId,
	                                                                  @RequestParam("page_size") Integer size,
	                                                                  @RequestParam("page_number") Integer number) {
		List<PictureOutDto> picturesForUser = pictureService.getPicturesForUser(null, userId, size, number);
		return ResponseEntity.ok(picturesForUser);
	}

	@GetMapping("{user}/{name}")
	private ResponseEntity<PictureOutDto> getUserPicture(@PathVariable("user") Integer userId,
	                                                     @PathVariable("name") String pictureName) {
		// private image will be shown to owner only
		Integer ownerId = null;
		PictureOutDto result = pictureService.getPictureByOwnerAndName(ownerId, userId, pictureName);
		return ResponseEntity.ok(result);
	}

	@PostMapping
	private ResponseEntity<PictureOutDto> createPicture(@RequestParam(value = "name", required = false) String pictureName,
	                                                    @RequestParam(value = "is_public", required = false) Boolean isPublic,
	                                                    @RequestParam("pic") MultipartFile pictureFile) {
		try {
			PictureOutDto pictureOutDto = pictureService.savePicture(null, new PictureInfoDto(pictureName, isPublic), pictureFile);
			return ResponseEntity.ok(pictureOutDto);
		} catch (Throwable t) {
			log.error("Unable to create picture", t);
			return ResponseEntity.badRequest().body(null);
		}
	}

	@PutMapping
	private ResponseEntity<PictureOutDto> updatePicture(@RequestParam(value = "name", required = false) String pictureName,
	                                                    @RequestParam(value = "is_public", required = true) Boolean isPublic) {
		PictureOutDto result;
		Integer ownerId = null;
		try {
			result = pictureService.changeVisibility(ownerId, pictureName, isPublic);
		} catch (IOException e) {
			log.error("Unable to change picture visibility", e);
			throw new GeneralRequestError("Unable to change picture visibility", e);
		}
		return ResponseEntity.ok(result);
	}

	@DeleteMapping
	private ResponseEntity<?> deletePicture(@RequestParam("name") String name) {

		Integer ownerId = null;
		pictureService.deletePicture(name, ownerId);
		return ResponseEntity.ok().build();
	}

}
