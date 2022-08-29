package com.aonufrei.gallerywebapp.service;

public interface IsTokenService<T> {

	String encode(String username, String password);

	T decode(String token);

}
