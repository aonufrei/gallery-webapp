package com.aonufrei.gallerywebapp.controller.rest;

import com.aonufrei.gallerywebapp.dto.PictureInfoDto;
import com.aonufrei.gallerywebapp.dto.PictureOutDto;
import com.aonufrei.gallerywebapp.exceptions.GeneralRequestError;
import com.aonufrei.gallerywebapp.exceptions.PictureNotFoundException;
import com.aonufrei.gallerywebapp.exceptions.PermissionRequiredException;
import com.aonufrei.gallerywebapp.security.data.AccountUserDetails;
import com.aonufrei.gallerywebapp.service.AccountService;
import com.aonufrei.gallerywebapp.service.PictureService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/v1/picture")
@SecurityRequirement(name = "app-security")
public class PictureRestController {

	private final Logger LOG = LoggerFactory.getLogger(PictureRestController.class);

	private final PictureService pictureService;
	private final AccountService accountService;

	public PictureRestController(PictureService pictureService, AccountService accountService) {
		this.pictureService = pictureService;
		this.accountService = accountService;
	}
	@GetMapping("feeds")
	public ResponseEntity<List<PictureOutDto>> getAllPublicImage(@RequestParam("page_size") Integer size,
	                                                             @RequestParam("page_number") Integer number) {
		List<PictureOutDto> allPublicImages = pictureService.getAllPublicImages(size, number);
		return ResponseEntity.ok(allPublicImages);
	}

	@GetMapping("{username}")
	private ResponseEntity<List<PictureOutDto>> getAllPicturesForUser(@PathVariable("username") String username,
	                                                                  @RequestParam("page_size") Integer size,
	                                                                  @RequestParam("page_number") Integer number) {
		Integer currentUserId = accountService.getAuthorizedAccountId();
		List<PictureOutDto> picturesForUser = pictureService.getPicturesForUser(currentUserId, username, size, number);
		return ResponseEntity.ok(picturesForUser);
	}

	@GetMapping("{username}/{token}")
	private ResponseEntity<PictureOutDto> getUserPictureInfo(@PathVariable("username") String username,
	                                                     @PathVariable("token") String token) {
		// private image will be shown to owner only
		Integer currentUserId = accountService.getAuthorizedAccountId();
		PictureOutDto result;
		try {
			result = pictureService.getPictureInfoByUsernameAndToken(currentUserId, username, token);
		} catch (Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t;
		}
		return ResponseEntity.ok(result);
	}

	@GetMapping(value = "{username}/{token}/pic", produces = MediaType.IMAGE_JPEG_VALUE)
	private ResponseEntity<byte[]> getUserPicture(@PathVariable("username") String username,
														 @PathVariable("token") String token) {
		// private image will be shown to owner only
		Integer currentUserId = accountService.getAuthorizedAccountId();
		try {
			byte[] pic = pictureService.getPictureBytesByUsernameAndToken(currentUserId, username, token);
			return ResponseEntity.ok(pic);
		} catch (PictureNotFoundException e) {
			LOG.error(e.getMessage(), e);
			throw e;
		}
	}

	@PostMapping
	private ResponseEntity<PictureOutDto> createPicture(@RequestParam(value = "name", required = false) String pictureName,
														@RequestParam(value = "is_public", required = false) Boolean isPublic,
														@RequestParam(value = "pic") MultipartFile pictureFile) {
		AccountUserDetails authorizedAccount = accountService.getAuthorizedAccount();
		if (authorizedAccount == null || authorizedAccount.getId() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		try {
			PictureOutDto pictureOutDto = pictureService.savePicture(authorizedAccount.getId(), new PictureInfoDto(pictureName, isPublic), pictureFile);
			return ResponseEntity.ok(pictureOutDto);
		} catch (Throwable t) {
			LOG.error("Unable to create picture", t);
			return ResponseEntity.badRequest().body(null);
		}
	}

	@PutMapping("{username}/{token}")
	private ResponseEntity<PictureOutDto> updatePictureInfo(@PathVariable("username") String username,
															@PathVariable("token") String token,
															@RequestBody PictureInfoDto pictureInfoDto) {
		Integer authorizedAccountId = accountService.getAuthorizedAccountId();
		if (!accountService.isSameAccount(authorizedAccountId, username)) {
			throw new PermissionRequiredException("User only has access to his pictures");
		}
		PictureOutDto result;
		try {
			result = pictureService.updatePictureInformation(username, token, pictureInfoDto);
		} catch (Throwable t) {
			LOG.error("Error occurred when updating picture information", t);
			throw new GeneralRequestError("Error occurred when updating picture information. Please, try again later", t);
		}
		return ResponseEntity.ok(result);
	}

	@DeleteMapping("{username}/{token}")
	private ResponseEntity<Boolean> deletePicture(@PathVariable("username") String username, @PathVariable("token") String token) {
		Integer authorizedAccountId = accountService.getAuthorizedAccountId();
		if (!accountService.isSameAccount(authorizedAccountId, username)) {
			throw new PermissionRequiredException("User only has access to picture of themselves");
		}
		return ResponseEntity.ok(pictureService.deletePicture(username, token));
	}

}
