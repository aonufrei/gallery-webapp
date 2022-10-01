package com.aonufrei.gallerywebapp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.aonufrei")
@OpenAPIDefinition(info = @Info(title = "Gallery Application API", description = "Gallery application that uses AWS technologies (s3, rds, ec2)."))
@SecurityScheme(name = "app-security", type = SecuritySchemeType.HTTP, scheme = "bearer", in = SecuritySchemeIn.HEADER)
public class GalleryWebappApplication {

	public static void main(String[] args) {
		SpringApplication.run(GalleryWebappApplication.class, args);
	}

}
