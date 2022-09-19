package com.aonufrei.gallerywebapp.repo;

import com.aonufrei.gallerywebapp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Integer> {

	Account getAccountByUsername(String username);

	@Query("select a from accounts a where a.id in (:ids)")
	List<Account> getAccountsByIds(List<Integer> ids);

	@Query("select case when count(a) > 0 then true else false end " +
			" from accounts a where a.id = :id and a.username = :username")
	boolean isSameAccount(Integer id, String username);

}
