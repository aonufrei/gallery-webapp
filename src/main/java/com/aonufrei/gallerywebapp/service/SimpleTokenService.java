package com.aonufrei.gallerywebapp.service;

import com.aonufrei.gallerywebapp.dto.AccountInDto;
import org.springframework.stereotype.Service;

@Service
public class SimpleTokenService implements IsTokenService<AccountInDto> {

	@Override
	public String encode(String username, String password) {
		return String.format("%s:%s", username, password);
	}

	@Override
	public AccountInDto decode(String token) {
		String[] parts = token.split(":");
		if (parts.length != 2) {
			return null;
		}

		return new AccountInDto(parts[0], parts[1]);
	}

}
