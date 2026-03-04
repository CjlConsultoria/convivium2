package com.convivium.module.support.repository;

import com.convivium.module.support.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    List<SupportMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    long countByTicketIdAndReadFalseAndFromAdminTrue(Long ticketId);

    long countByTicketIdAndReadFalseAndFromAdminFalse(Long ticketId);

    @Modifying
    @Query("UPDATE SupportMessage m SET m.read = true, m.readAt = CURRENT_TIMESTAMP " +
           "WHERE m.ticket.id = :ticketId AND m.fromAdmin = :fromAdmin AND m.read = false")
    int markAsRead(Long ticketId, boolean fromAdmin);
}
