package com.ideas2it.emailLoggingSystem.repository;

import com.ideas2it.emailLoggingSystem.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    /**
     * Fetches all email logs from the database, ordered by the email log's ID in ascending order.
     *
     * @return a list of all email logs ordered by their ID
     */
    @Query("SELECT e FROM EmailLog e ORDER BY e.id ASC")
    List<EmailLog> findAllEmailLogsSorted();

    /**
     * Fetches all email logs along with their associated attachments in a single query.
     * This query uses a LEFT JOIN FETCH to eagerly load attachments for each email log.
     *
     * @return a list of all email logs with their associated attachments
     */
    @Query("SELECT e FROM EmailLog e LEFT JOIN FETCH e.attachments")
    List<EmailLog> findAllWithAttachments();
}