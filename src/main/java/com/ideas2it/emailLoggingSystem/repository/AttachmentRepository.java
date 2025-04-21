package com.ideas2it.emailLoggingSystem.repository;

import com.ideas2it.emailLoggingSystem.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}

