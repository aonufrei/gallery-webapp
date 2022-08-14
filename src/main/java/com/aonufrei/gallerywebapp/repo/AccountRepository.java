package com.aonufrei.gallerywebapp.repo;

import com.aonufrei.gallerywebapp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Integer> {

	Account getAccountByUsername(String username);

}
