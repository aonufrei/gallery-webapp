package com.aonufrei.gallerywebapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.aonufrei.gallerywebapp.dto.PictureInfoDto;
import com.aonufrei.gallerywebapp.dto.PictureOutDto;
import com.aonufrei.gallerywebapp.exceptions.GeneralRequestError;
import com.aonufrei.gallerywebapp.exceptions.PictureNotFoundException;
import com.aonufrei.gallerywebapp.model.Picture;
import com.aonufrei.gallerywebapp.repo.PictureRepository;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PictureService {

	private final Logger LOG = LoggerFactory.getLogger(PictureService.class);

	private final PictureRepository pictureRepository;
	private final AccountService accountService;
	private final AmazonS3 s3Client;

	@Value("${s3.main-bucket-name}")
	private String bucketName;

	@Value("${host.url}")
	private String hostPrefix;

	public PictureService(PictureRepository pictureRepository, AccountService accountService, AmazonS3 s3Client) {
		this.pictureRepository = pictureRepository;
		this.accountService = accountService;
		this.s3Client = s3Client;
	}

	public byte[] getPictureByToken(String username, String token) throws IOException {
		if (!pictureRepository.existsPictureByToken(token)) {
			throw new PictureNotFoundException("Picture with specified token cannot be found");
		}
		return getS3PictureByPath(token);
	}

	public List<PictureOutDto> getAllPublicImages(Integer pageSize, Integer pageNumber) {
		Pageable page;
		try {
			page = Pageable.ofSize(pageSize).withPage(pageNumber);
		} catch (Exception e) {
			throw new GeneralRequestError(e.getMessage());
		}
		return convertModelsToOutDto(pictureRepository.getPicturesByIsSharedToPublic(true, page).toList());
	}

	public List<PictureOutDto> getPicturesForUser(Integer currentUserId, String username, Integer pageSize, Integer pageNumber) {
		Pageable pageable;
		try {
			pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
		} catch (Exception e) {
			throw new GeneralRequestError(e.getMessage());
		}
		Integer ownerId = accountService.getIdByUsername(username);
		Page<Picture> result = currentUserId != null && currentUserId.equals(ownerId)
				? pictureRepository.getPicturesByOwnerIdOrderByModifiedAt(ownerId, pageable)
				: pictureRepository.getPicturesByOwnerIdAndIsSharedToPublicOrderByModifiedAt(ownerId, true, pageable);
		return convertModelsToOutDto(result.toList());
	}

	public PictureOutDto getPictureInfoByUsernameAndToken(Integer currentUser, String username, String token) {
		PictureOutDto pictureInfo = getPictureInfoByUsernameAndToken(username, token);
		if (pictureInfo.getIsPublic()) {
			return pictureInfo;
		}
		if (accountService.isSameAccount(currentUser, username)) {
			return pictureInfo;
		}
		throw new PictureNotFoundException(genPictureNotFoundMessage(username, token));
	}

	public byte[] getPictureBytesByUsernameAndToken(Integer currentUser, String username, String token) {
		if (!pictureRepository.doesUserHasImage(username, token)
				|| (!pictureRepository.isPicturePublic(username, token)
				&& !accountService.isSameAccount(currentUser, username))) {
			throw new PictureNotFoundException(genPictureNotFoundMessage(username, token));
		}
		try {
			String path = generateS3Path(username, token);
			return getS3PictureByPath(path);
		} catch (IOException e) {
			LOG.error("Error when getting picture from s3", e);
			throw new PictureNotFoundException(genPictureNotFoundMessage(username, token));
		}
	}

	public static String genPictureNotFoundMessage(String ownerUsername, String pictureToken) {
		return String.format("Picture [%s] was not found for user [%s]", pictureToken, ownerUsername);
	}

	public byte[] getS3PictureByPath(String path) throws IOException {
		try (InputStream bImage = getPictureFromS3(path)) {
			return IOUtils.toByteArray(bImage);
		}
	}

	public InputStream getPictureFromS3(String s3PicturePath) {
		S3Object s3Picture = s3Client.getObject(bucketName, s3PicturePath);
		if (s3Picture == null) {
			LOG.error("Picture [" + s3PicturePath + "] was not found in s3");
			throw new RuntimeException("Picture was not found in s3");
		}
		return s3Picture.getObjectContent();
	}

	public List<PictureOutDto> getPictures(Integer pageSize, Integer pageNumber) {
		Pageable pageable;
		try {
			pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
		} catch (Exception e) {
			throw new GeneralRequestError(e.getMessage());
		}
		return pictureRepository.findAll(pageable).stream()
				.map(this::convertModelToOutDto)
				.collect(Collectors.toList());
	}

	private PictureOutDto convertModelToOutDto(Picture picture) {
		String ownerUsername = picture.getOwner() != null
				? picture.getOwner().getUsername()
				: accountService.getUsernameById(picture.getOwnerId());
		String pictureUrl = String.format("%sapi/v1/picture/%s/%s/pic", hostPrefix, ownerUsername, picture.getToken());
		return new PictureOutDto(picture.getName(), picture.getIsSharedToPublic(), pictureUrl, picture.getCreatedAt());
	}

	private List<PictureOutDto> convertModelsToOutDto(List<Picture> pictures) {
		List<Integer> pictureIds = pictures.stream().map(Picture::getOwnerId).distinct().collect(Collectors.toList());
		Map<Integer, String> idToUsernameMap = accountService.getIdToUsernameMap(pictureIds);
		return pictures.stream().filter(p -> idToUsernameMap.containsKey(p.getOwnerId()))
				.map(p -> new PictureOutDto(
						p.getName(),
						p.getIsSharedToPublic(),
						String.format("%sapi/v1/picture/%s/%s/pic", hostPrefix, idToUsernameMap.get(p.getOwnerId()), p.getToken()),
						p.getCreatedAt()
				))
				.collect(Collectors.toList());
	}

	public PictureOutDto getPictureInfoByUsernameAndToken(String username, String token) {
		if (!pictureRepository.doesUserHasImage(username, token)) {
			throw new PictureNotFoundException(String.format("Picture [%s/%s] was not found", username, token));
		}
		return convertModelToOutDto(pictureRepository.getPictureByOwnerNameAndToken(username, token));
	}

	public PictureOutDto getPictureForUser(String name, Integer ownerId) {
		Picture pictureFromDb = pictureRepository.getPictureByNameAndOwnerId(name, ownerId);
		if (pictureFromDb == null) {
			LOG.error(String.format("Picture of name %s of user %d was not found", name, ownerId));
			throw new RuntimeException("Picture was not found");
		}
		if (pictureFromDb.getOwner() == null) {
			pictureFromDb.setOwner(accountService.getAccountById(ownerId));
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

		String token = generateNewPictureToken();
		saveFileToS3(generateS3Path(ownerUsername, token), file);
		Picture pictureForDb = Picture.builder()
				.name(name)
				.isSharedToPublic(isPublic)
				.ownerId(ownerId)
				.token(token)
				.build();
		try {
			pictureRepository.save(pictureForDb);
		} catch (Exception e) {
			LOG.error("Exception occurred during picture saving to db", e);
			s3Client.deleteObject(token, bucketName);
			throw new RuntimeException("Failed to save your picture in our system");
		}
		return getPictureForUser(name, ownerId);
	}

	private String generateS3Path(String ownerUsername, String token) {
		return String.format("%s/%s", ownerUsername, token);
	}

	private String generateNewPictureToken() {
		while (true) {
			String token = RandomStringUtils.random(8, true, true);
			if (!pictureRepository.existsPictureByToken(token)) {
				return token;
			}
		}
	}

	@Transactional
	public PictureOutDto updatePictureInformation(String username, String token, PictureInfoDto pictureInfoDto) {
		Picture pictureToUpdate = pictureRepository.getPictureByOwnerNameAndToken(username, token);
		if (pictureInfoDto.getName() != null) {
			pictureToUpdate.setName(pictureInfoDto.getName());
		}
		if (pictureInfoDto.getIsSharedToPublic() != null) {
			pictureToUpdate.setIsSharedToPublic(pictureInfoDto.getIsSharedToPublic());
		}
		pictureRepository.save(pictureToUpdate);
		return convertModelToOutDto(pictureToUpdate);
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

	public boolean deletePicture(String ownerUsername, String pictureToken) {
		if (!pictureRepository.doesUserHasImage(ownerUsername, pictureToken)) {
			return false;
		}
		pictureRepository.deleteByOwnerNameAndToken(ownerUsername, pictureToken);
		String s3PicturePath = generateS3Path(ownerUsername, pictureToken);
		try {
			DeleteObjectRequest deletePictureRequest = new DeleteObjectRequest(bucketName, s3PicturePath);
			s3Client.deleteObject(deletePictureRequest);
		} catch (Throwable t) {
			LOG.error(String.format("Cannot delete picture from s3 [ %s ]", s3PicturePath), t);
		}
		return true;
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
