package com.postech.workshop_service;

import org.springframework.boot.SpringApplication;

public class TestWorkshopServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(WorkshopServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
