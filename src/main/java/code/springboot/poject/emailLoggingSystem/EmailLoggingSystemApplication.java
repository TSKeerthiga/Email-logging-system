package code.springboot.poject.emailLoggingSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "code.springboot.poject.emailLoggingSystem.repository")
@EntityScan(basePackages = "code.springboot.poject.emailLoggingSystem.entity")
//@EnableScheduling
public class EmailLoggingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailLoggingSystemApplication.class, args);
	}

}
