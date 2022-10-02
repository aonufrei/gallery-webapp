package com.aonufrei.gallerywebapp.security.data;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

	private final String secret;
	public JwtService(@Value("${jwt.secret}") String secret) {
		this.secret = secret;
	}

	public String encode(String subject, Map<String, Object> claims, Instant issued, Instant expiration) {
		return Jwts.builder()
				.setSubject(subject)
				.setClaims(claims)
				.setIssuedAt(Date.from(issued))
				.setExpiration(Date.from(expiration))
				.signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
				.compact();
	}

	public Jws<Claims> decode(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
				.build()
				.parseClaimsJws(token);
	}
}
