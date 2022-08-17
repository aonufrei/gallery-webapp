package com.aonufrei.gallerywebapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.aonufrei")
public class GalleryWebappApplication {

	public static void main(String[] args) {
		SpringApplication.run(GalleryWebappApplication.class, args);
	}

}
