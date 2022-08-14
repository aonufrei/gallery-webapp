package com.aonufrei.gallerywebapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PictureOutDto {

	private String name;

	private Boolean isPublic;

	private String pictureUrl;

	private LocalDateTime uploadedOn;

}
