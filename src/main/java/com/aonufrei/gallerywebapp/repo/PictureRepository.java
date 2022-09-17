package com.aonufrei.gallerywebapp.repo;

import com.aonufrei.gallerywebapp.model.Picture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PictureRepository extends JpaRepository<Picture, Integer> {

	void deleteByNameAndOwnerId(String name, Integer ownerId);

	Picture getPictureByNameAndOwnerId(String name, Integer ownerId);

	Page<Picture> getPicturesByIsSharedToPublic(boolean isSharedToPublic, Pageable pageable);

	Page<Picture> getPicturesByOwnerIdOrderByModifiedAt(Integer ownerId, Pageable pageable);

	Page<Picture> getPicturesByOwnerIdAndIsSharedToPublicOrderByModifiedAt(Integer ownerId, boolean isSharedToPublic, Pageable pageable);

	boolean existsPictureByToken(String token);

	Picture getPictureByToken(String token);
}
