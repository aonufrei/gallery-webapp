package com.aonufrei.gallerywebapp.service;

import com.aonufrei.gallerywebapp.dto.PictureInfoDto;
import com.aonufrei.gallerywebapp.dto.PictureOutDto;
import com.aonufrei.gallerywebapp.model.Picture;
import com.aonufrei.gallerywebapp.repo.PictureRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PictureService {

	private final Logger LOG = LoggerFactory.getLogger(PictureService.class);

	private final PictureRepository pictureRepository;

	public PictureService(PictureRepository pictureRepository) {
		this.pictureRepository = pictureRepository;
	}

	public String convertPictureTokenToPath(String token) {
		return token;
	}

	public String picturePathToToken(String path) {
		return path;
	}

	public byte[] getPictureFromPath(String path) throws IOException {
		try (InputStream bImage = new FileInputStream("D:\\" + path)) {
			return IOUtils.toByteArray(bImage);
		}
	}

	public List<PictureOutDto> getPictures(Integer pageSize, Integer pageNumber) {
		return pictureRepository.findAll(Pageable.ofSize(pageSize).withPage(pageNumber)).stream()
				.map(this::convertModelToOutDto)
				.collect(Collectors.toList());
	}

	private PictureOutDto convertModelToOutDto(Picture picture) {
		String pictureUrl = "api/v1/gallery/specific/?pic=" + convertPictureTokenToPath(picture.getInSystemFilename());
		return new PictureOutDto(picture.getName(), picture.getIsSharedToPublic(), pictureUrl, picture.getCreatedAt());
	}

	public void savePicture(Integer ownerId, PictureInfoDto pictureInfoDto, MultipartFile file) throws IOException {
		validatePictureInfo(pictureInfoDto);
		if (!isValidImage(file)) {
			LOG.error("Unsupported image format was provided");
			throw new RuntimeException("Unsupported image format provided");
		}
		String name = pictureInfoDto.getName();
		Boolean isPublic = pictureInfoDto.getIsSharedToPublic();
		if (isPublic == null) isPublic = false;

		if (pictureInfoDto.getName() == null) {
			name = UUID.randomUUID().toString();
		}
		String inSystemFilename = UUID.randomUUID().toString();

		File savedFile = new File("D:\\" + inSystemFilename);
		file.transferTo(savedFile);
		Picture pictureForDb = Picture.builder()
				.name(name)
				.isSharedToPublic(isPublic)
				.inSystemFilename(inSystemFilename)
				.ownerId(ownerId)
				.build();
		try {
			pictureRepository.save(pictureForDb);
		} catch (Exception e) {
			LOG.error("Exception occurred during picture saving to db", e);
			if (!savedFile.delete()) LOG.error("Image was not deleted");
			throw new RuntimeException("Failed to save your picture in our system");
		}
	}

	public static void validatePictureInfo(PictureInfoDto pictureInfoDto) {
		if (pictureInfoDto == null) {
			throw new RuntimeException("Picture information was not provided");
		}
	}

	public static boolean isValidImage(MultipartFile uploadedFile) throws IOException {
		try (InputStream input = uploadedFile.getInputStream()) {
			try {
				return ImageIO.read(input).toString() != null;
			} catch (Exception e) {
				return false;
			}
		}
	}

}
