package com.sigorta.claim_service;

import org.springframework.boot.SpringApplication;

public class TestClaimServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(ClaimServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
