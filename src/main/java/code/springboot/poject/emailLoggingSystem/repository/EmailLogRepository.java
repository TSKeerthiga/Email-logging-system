package code.springboot.poject.emailLoggingSystem.repository;

import code.springboot.poject.emailLoggingSystem.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    List<EmailLog> findByUserId(Long userId);
}