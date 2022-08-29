package com.aonufrei.gallerywebapp.service;


import com.aonufrei.gallerywebapp.dto.AccountInDto;
import com.aonufrei.gallerywebapp.exceptions.AccountNotFoundException;
import com.aonufrei.gallerywebapp.exceptions.AuthorizationError;
import com.aonufrei.gallerywebapp.exceptions.InvalidAccountIdFormatException;
import com.aonufrei.gallerywebapp.model.Account;
import com.aonufrei.gallerywebapp.repo.AccountRepository;
import com.aonufrei.gallerywebapp.utils.GeneralUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

	private final Logger LOG = LoggerFactory.getLogger(AccountService.class);
	private final AccountRepository accountRepository;
	private final PasswordEncoder passwordEncoder;

	private final IsTokenService<AccountInDto> tokenService;

	public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder, IsTokenService<AccountInDto> tokenService) {
		this.accountRepository = accountRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
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

	public String getAuthToken(String username, String password) {
		if (username == null || password == null) {
			throw new AuthorizationError("Forbidden");
		}
		Account accountByUsername = getAccountByUsername(username);
		if (accountByUsername == null) {
			LOG.error("Account was not found in the database");
			throw new AuthorizationError("Forbidden");
		}
		String encodedPassword = passwordEncoder.encode(password);
		if (accountByUsername.getPassword().equals(encodedPassword)) {
			throw new AuthorizationError("Forbidden");
		}

		return tokenService.encode(username, password);
	}

	public Integer validateAccountId(String accountId) {
		if (NumberUtils.isCreatable(accountId)) {
			Integer intAccountId = NumberUtils.createInteger(accountId);
			return validateAccountId(intAccountId);
		}
		throw new InvalidAccountIdFormatException(String.format("Account id of wrong format was provided [%s]", accountId));
	}

	public Integer validateAccountId(Integer accountId) {
		if (accountId != null && accountRepository.existsById(accountId)) {
			return accountId;
		}
		throw new AccountNotFoundException(String.format("Account with id [%d] was now found", accountId));
	}

	public String getUsernameById(Integer id) {
		Account account = accountRepository.getById(id);
		if (account == null) {
			throw new AccountNotFoundException(String.format("Account with id [%d] was now found", id));
		}
		return account.getUsername();
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
