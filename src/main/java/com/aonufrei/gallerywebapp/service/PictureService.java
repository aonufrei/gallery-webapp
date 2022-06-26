package com.aonufrei.gallerywebapp.service;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class PictureService {

	private final Logger LOG = LoggerFactory.getLogger(PictureService.class);

	public String convertPictureTokenToPath(String token) {
		return token;
	}

	public byte[] getPictureFromPath(String path) throws IOException {
		try (InputStream bImage = new FileInputStream("D:\\" + path)) {
			return IOUtils.toByteArray(bImage);
		}
	}

	public void savePicture(MultipartFile file) throws IOException {
		if (!isValidImage(file)) {
			LOG.error("Unsupported image format was provided");
			throw new RuntimeException("Unsupported image format provided");
		}
		File savedFile = new File("D:\\" + file.getOriginalFilename());
		file.transferTo(savedFile);
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
