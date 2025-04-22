package com.ideas2it.emailLoggingSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.ideas2it.emailLoggingSystem.repository")
@EntityScan(basePackages = "com.ideas2it.emailLoggingSystem.entity")
@EnableScheduling
public class EmailLoggingSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(EmailLoggingSystemApplication.class, args);
	}
}



