package com.aonufrei.gallerywebapp.service;


import com.aonufrei.gallerywebapp.dto.AccountInDto;
import com.aonufrei.gallerywebapp.model.Account;
import com.aonufrei.gallerywebapp.repo.AccountRepository;
import com.aonufrei.gallerywebapp.utils.GeneralUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

	private final AccountRepository accountRepository;
	private final PasswordEncoder passwordEncoder;

	public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
		this.accountRepository = accountRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public Integer createAccount(AccountInDto accountInDto) {
		validateAccount(accountInDto);
		String encodedPassword = passwordEncoder.encode(accountInDto.getPassword());
		Account accountForDb = Account.builder()
				.username(accountInDto.getUsername())
				.password(encodedPassword)
				.build();
		Account savedAccount = accountRepository.save(accountForDb);
		return savedAccount.getId();
	}

	public Account getAccountByUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new RuntimeException("Username cannot be blank");
		}
		Account accountByUsername = accountRepository.getAccountByUsername(username);
		if (accountByUsername == null) {
			throw new RuntimeException("Account was not found");
		}
		return accountByUsername;
	}

	public void validateAccount(AccountInDto accountInDto) {
		GeneralUtils.checkAndThrow(accountInDto.getUsername() == null
				|| accountInDto.getUsername().isBlank(), "Username is required");
		GeneralUtils.checkAndThrow(accountInDto.getPassword() == null, "Password is required");
		GeneralUtils.checkAndThrow(accountInDto.getPassword().length() < 6, "Password is too short");
		GeneralUtils.checkAndThrow(accountInDto.getPassword().length() > 12, "Password is too long");
	}

}
