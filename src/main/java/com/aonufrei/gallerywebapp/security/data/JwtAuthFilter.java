package com.aonufrei.gallerywebapp.security.data;

import com.aonufrei.gallerywebapp.model.Account;
import com.aonufrei.gallerywebapp.service.AccountService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private final AccountService accountService;
	private final JwtService jwtService;

	public JwtAuthFilter(AccountService accountService, JwtService jwtService) {
		this.accountService = accountService;
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		processJwt(request);
		filterChain.doFilter(request, response);
	}

	private void processJwt(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (StringUtils.isBlank(authorization)) {
			return;
		}
		if (authorization.length() < 8) {
			return;
		}
		String token = authorization.substring(7);
		String username = getUsernameFromToken(token);
		if (username == null) {
			return;
		}
		Account accountByUsername;
		try {
			accountByUsername = accountService.getAccountByUsername(username);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			return;
		}
		AccountUserDetails accountUserDetails = new AccountUserDetails(accountByUsername);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(accountUserDetails, null, accountUserDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
		auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	}

	private String getUsernameFromToken(String token) {
		if (token == null) {
			return null;
		}
		Jws<Claims> decode;
		try {
			decode = jwtService.decode(token);
		} catch (Throwable ignore) {
			return null;
		}
		return decode.getBody().get("username", String.class);
	}

}
