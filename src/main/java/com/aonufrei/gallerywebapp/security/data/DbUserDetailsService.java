package com.aonufrei.gallerywebapp.security.data;

import com.aonufrei.gallerywebapp.model.Account;
import com.aonufrei.gallerywebapp.service.AccountService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DbUserDetailsService implements UserDetailsService {

	private final AccountService accountService;

	public DbUserDetailsService(AccountService accountService) {
		this.accountService = accountService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Account accountFromDb = accountService.getAccountByUsername(username);
		if (accountFromDb == null) {
			throw new UsernameNotFoundException("User was not found");
		}
		return new AccountUserDetails(accountFromDb);
	}
}
