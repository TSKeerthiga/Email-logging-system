package com.ideas2it.emailLoggingSystem.repository;

import com.ideas2it.emailLoggingSystem.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    @Query("SELECT e FROM EmailLog e ORDER BY e.id ASC")
    List<EmailLog> findAllEmailLogsSorted();

    @Query("SELECT e FROM EmailLog e LEFT JOIN FETCH e.attachments")
    List<EmailLog> findAllWithAttachments();
}