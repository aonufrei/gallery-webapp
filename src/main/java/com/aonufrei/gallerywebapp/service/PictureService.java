package com.aonufrei.gallerywebapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.aonufrei.gallerywebapp.dto.PictureInfoDto;
import com.aonufrei.gallerywebapp.dto.PictureOutDto;
import com.aonufrei.gallerywebapp.dto.PictureUpdateInfoDto;
import com.aonufrei.gallerywebapp.model.Picture;
import com.aonufrei.gallerywebapp.repo.PictureRepository;
import org.apache.commons.io.IOUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PictureService {

	private final Logger LOG = LoggerFactory.getLogger(PictureService.class);

	private final PictureRepository pictureRepository;
	private final AccountService accountService;
	private final AmazonS3 s3Client;

	@Value("${s3.main-bucket-name}")
	private String bucketName;

	public PictureService(PictureRepository pictureRepository, AccountService accountService, AmazonS3 s3Client) {
		this.pictureRepository = pictureRepository;
		this.accountService = accountService;
		this.s3Client = s3Client;
	}

	public List<PictureOutDto> getAllPublicImages(Integer pageSize, Integer pageNumber) {
		return pictureRepository.getPicturesByIsSharedToPublic(true, Pageable.ofSize(pageSize).withPage(pageNumber))
				.stream()
				.map(this::convertModelToOutDto)
				.collect(Collectors.toList());
	}

	public List<PictureOutDto> getPicturesForUser(Integer currentUserId, Integer userId, Integer pageSize, Integer pageNumber) {
		Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
		Page<Picture> result = currentUserId != null && currentUserId.equals(userId)
				? pictureRepository.getPicturesByOwnerIdOrderByModifiedAt(userId, pageable)
				: pictureRepository.getPicturesByOwnerIdAndIsSharedToPublicOrderByModifiedAt(userId, true, pageable);
		return result.stream()
				.map(this::convertModelToOutDto)
				.collect(Collectors.toList());
	}

	public PictureOutDto getPictureByOwnerAndName(Integer currentUser, Integer userId, String name) {
		PictureOutDto picture = this.getPicture(name, userId);
		if (picture != null && (picture.getIsPublic() || Objects.equals(currentUser, userId))) {
			return picture;
		}
		throw new RuntimeException("Picture was not found");
	}

	public String convertPictureTokenToPath(String token) {
		return token;
	}

	public byte[] getPictureFromPath(String path) throws IOException {
		try (InputStream bImage = getPictureFromS3(path)) {
			return IOUtils.toByteArray(bImage);
		}
	}

	public InputStream getPictureFromS3(String s3PictureName) {
		S3Object s3Picture = s3Client.getObject(s3PictureName, bucketName);
		if (s3Picture == null) {
			LOG.error("Picture " + s3PictureName + " was not found in s3");
			throw new RuntimeException("Picture was not found in s3");
		}
		return s3Picture.getObjectContent();
	}

	public List<PictureOutDto> getPictures(Integer pageSize, Integer pageNumber) {
		return pictureRepository.findAll(Pageable.ofSize(pageSize).withPage(pageNumber)).stream()
				.map(this::convertModelToOutDto)
				.collect(Collectors.toList());
	}

	private PictureOutDto convertModelToOutDto(Picture picture) {
		String pictureUrl = "api/v1/gallery/specific?pic=" + createS3PictureName(picture.getName(), picture.getOwner().getUsername());
		return new PictureOutDto(picture.getName(), picture.getIsSharedToPublic(), pictureUrl, picture.getCreatedAt());
	}

	public PictureOutDto getPicture(String name, Integer ownerId) {
		Picture pictureFromDb = pictureRepository.getPictureByNameAndOwnerId(name, ownerId);
		if (pictureFromDb == null) {
			LOG.error(String.format("Picture of name %s of user %d was not found", name, ownerId));
			throw new RuntimeException("Picture was not found");
		}
		return convertModelToOutDto(pictureFromDb);
	}
	public PictureOutDto savePicture(Integer ownerId, PictureInfoDto pictureInfoDto, MultipartFile file) throws IOException {
		validatePictureInfo(pictureInfoDto);
		String ownerUsername = accountService.getUsernameById(ownerId);
		if (!isValidImageFormat(file)) {
			LOG.error("Unsupported image format was provided");
			throw new RuntimeException("Unsupported image format provided");
		}
		String name = pictureInfoDto.getName();
		Boolean isPublic = pictureInfoDto.getIsSharedToPublic();
		if (isPublic == null) isPublic = false;

		if (pictureInfoDto.getName() == null) {
			name = UUID.randomUUID().toString();
		}
		String s3Filename = createS3PictureName(ownerUsername, pictureInfoDto.getName());
		saveFileToS3(s3Filename, file);
		Picture pictureForDb = Picture.builder()
				.name(name)
				.isSharedToPublic(isPublic)
				.ownerId(ownerId)
				.build();
		try {
			pictureRepository.save(pictureForDb);
		} catch (Exception e) {
			LOG.error("Exception occurred during picture saving to db", e);
			s3Client.deleteObject(s3Filename, bucketName);
			throw new RuntimeException("Failed to save your picture in our system");
		}
		return getPicture(name, ownerId);
	}

	@Transactional
	public PictureOutDto changeVisibility(Integer ownerId, String pictureName, boolean isPublic) throws IOException {
		Picture targetPicture = pictureRepository.getPictureByNameAndOwnerId(pictureName, ownerId);
		targetPicture.setIsSharedToPublic(isPublic);
		pictureRepository.save(targetPicture);
		return convertModelToOutDto(targetPicture);
	}

	public String createS3PictureName(String ownerUsername, String pictureName) {
		return String.format("%s/%s", ownerUsername, pictureName);
	}

	public void saveFileToS3(String key, MultipartFile multipartFile) throws IOException {
		ObjectMetadata data = new ObjectMetadata();
		data.setContentType(multipartFile.getContentType());
		data.setContentLength(multipartFile.getSize());
		s3Client.putObject(bucketName, key, multipartFile.getInputStream(), data);
	}

	public void deletePicture(Integer id) {
		pictureRepository.deleteById(id);
	}

	public void deletePicture(String name, Integer ownerId) {
		Picture targetPicture = pictureRepository.getPictureByNameAndOwnerId(name, ownerId);
		if (targetPicture == null) return;
		try {
			pictureRepository.deleteById(targetPicture.getId());
		} catch (Exception e) {
			LOG.error("Error during deleting picture with id: " + targetPicture.getId(), e);
			return;
		}
		String ownerUsername = accountService.getUsernameById(ownerId);
		DeleteObjectRequest deletePictureRequest =
				new DeleteObjectRequest(bucketName, createS3PictureName(ownerUsername, name));
		s3Client.deleteObject(deletePictureRequest);
	}

	public static void validatePictureInfo(PictureInfoDto pictureInfoDto) {
		if (pictureInfoDto == null) {
			throw new RuntimeException("Picture information was not provided");
		}
	}

	public static boolean isValidImageFormat(MultipartFile uploadedFile) throws IOException {
		try (InputStream input = uploadedFile.getInputStream()) {
			try {
				return ImageIO.read(input).toString() != null;
			} catch (Exception e) {
				return false;
			}
		}
	}

}
