package com.aonufrei.gallerywebapp.repo;

import com.aonufrei.gallerywebapp.model.Picture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PictureRepository extends JpaRepository<Picture, Integer> {

	void deleteByNameAndOwnerId(String name, Integer ownerId);

	Picture getPictureByNameAndOwnerId(String name, Integer ownerId);

}
