package code.springboot.poject.emailLoggingSystem.repository;

import code.springboot.poject.emailLoggingSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
