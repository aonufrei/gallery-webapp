package com.aonufrei.gallerywebapp.repo;

import com.aonufrei.gallerywebapp.model.Picture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PictureRepository extends JpaRepository<Picture, Integer> {

	void deleteByNameAndOwnerId(String name, Integer ownerId);

	Picture getPictureByNameAndOwnerId(String name, Integer ownerId);

	Page<Picture> getPicturesByIsSharedToPublic(boolean isSharedToPublic, Pageable pageable);

	Page<Picture> getPicturesByOwnerIdOrderByModifiedAt(Integer ownerId, Pageable pageable);

	Page<Picture> getPicturesByOwnerIdAndIsSharedToPublicOrderByModifiedAt(Integer ownerId, boolean isSharedToPublic, Pageable pageable);

	boolean existsPictureByToken(String token);

	Picture getPictureByToken(String token);

	@Query("select case when count(p)> 0 then true else false end" +
			" from pictures p where p.token = :token and p.owner.username = :username and p.isSharedToPublic = true")
	boolean isPicturePublic(String username, String token);

	@Query("select p from pictures p " +
			"where p.token = :token and p.owner.username = :username")
	Picture getPictureByOwnerNameAndToken(String username, String token);

	@Query("select case when count(p)> 0 then true else false end " +
			" from pictures p where p.token = :token and p.owner.username = :username")
	boolean doesUserHasImage(String username, String token);

	@Query("delete from pictures p where p.token = :token and p.owner.username = :username")
	void deleteByOwnerNameAndToken(String username, String token);
}
