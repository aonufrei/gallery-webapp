package com.aonufrei.gallerywebapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureUpdateInfoDto {

	private String name;

	@JsonProperty("is_shared")
	private Boolean isShared;
}
