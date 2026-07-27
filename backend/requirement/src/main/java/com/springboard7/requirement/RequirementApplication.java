package com.springboard7.requirement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class RequirementApplication {

	public static void main(String[] args) {
		SpringApplication.run(RequirementApplication.class, args);
	}

}
