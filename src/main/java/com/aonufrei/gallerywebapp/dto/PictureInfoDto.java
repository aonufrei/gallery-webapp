package com.aonufrei.gallerywebapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PictureInfoDto {

	private String name;

	private Boolean isSharedToPublic;

}
