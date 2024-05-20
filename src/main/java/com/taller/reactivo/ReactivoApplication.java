package com.taller.reactivo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories
public class ReactivoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactivoApplication.class, args);
	}

}
