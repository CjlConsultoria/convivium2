package com.convivium.module.support.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.module.support.dto.MessageResponse;
import com.convivium.module.support.dto.TicketResponse;
import com.convivium.module.support.entity.SupportMessage;
import com.convivium.module.support.entity.SupportTicket;
import com.convivium.module.support.repository.SupportMessageRepository;
import com.convivium.module.support.repository.SupportTicketRepository;
import com.convivium.module.user.entity.User;
import com.convivium.module.user.entity.UserCondominiumRole;
import com.convivium.module.user.repository.UserCondominiumRoleRepository;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportChatService {

    private final SupportTicketRepository supportTicketRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final UserRepository userRepository;
    private final UserCondominiumRoleRepository userCondominiumRoleRepository;
    private final Optional<SimpMessagingTemplate> messagingTemplate;

    // ---- Ticket Operations ----

    @Transactional
    public TicketResponse createTicket(Long userId, String subject) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado", "USER_NOT_FOUND"));

        SupportTicket ticket = SupportTicket.builder()
                .user(user)
                .subject(subject)
                .status("OPEN")
                .build();

        Long condoId = TenantContext.getCurrentTenantId();
        if (condoId != null) {
            var condoRepo = userCondominiumRoleRepository
                    .findByUserIdAndCondominiumId(userId, condoId);
            if (condoRepo.isPresent()) {
                ticket.setCondominium(condoRepo.get().getCondominium());
            }
        }

        ticket = supportTicketRepository.save(ticket);
        return toTicketResponse(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listTicketsByUser(Long userId) {
        return supportTicketRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toTicketResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long ticketId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("Chamado nao encontrado", "TICKET_NOT_FOUND"));
        return toTicketResponse(ticket);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> listAllTickets(String status, int page, int size) {
        Page<SupportTicket> ticketPage;
        if (status != null && !status.isBlank()) {
            ticketPage = supportTicketRepository.findByStatusOrderByCreatedAtDesc(status, PageRequest.of(page, size));
        } else {
            ticketPage = supportTicketRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        }
        return ticketPage.map(this::toTicketResponse);
    }

    @Transactional
    public TicketResponse updateTicketStatus(Long ticketId, String status) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("Chamado nao encontrado", "TICKET_NOT_FOUND"));
        ticket.setStatus(status);
        ticket = supportTicketRepository.save(ticket);
        return toTicketResponse(ticket);
    }

    // ---- Message Operations ----

    @Transactional
    public MessageResponse sendMessage(Long ticketId, Long senderId, String messageText, boolean isAdmin) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("Chamado nao encontrado", "TICKET_NOT_FOUND"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado", "USER_NOT_FOUND"));

        SupportMessage message = SupportMessage.builder()
                .ticket(ticket)
                .sender(sender)
                .message(messageText)
                .fromAdmin(isAdmin)
                .build();

        message = supportMessageRepository.save(message);

        // Se o ticket estava OPEN e admin responde, muda para IN_PROGRESS
        if (isAdmin && "OPEN".equals(ticket.getStatus())) {
            ticket.setStatus("IN_PROGRESS");
            supportTicketRepository.save(ticket);
        }

        MessageResponse response = toMessageResponse(message);

        // Publicar via WebSocket se disponivel
        messagingTemplate.ifPresent(template -> {
            template.convertAndSend("/topic/support/tickets/" + ticketId, response);
            if (!isAdmin) {
                template.convertAndSend("/topic/support/admin", response);
            }
        });

        return response;
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(Long ticketId) {
        return supportMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional
    public void markMessagesAsRead(Long ticketId, boolean markAdminMessages) {
        supportMessageRepository.markAsRead(ticketId, markAdminMessages);
    }

    // ---- Mappers ----

    private TicketResponse toTicketResponse(SupportTicket ticket) {
        String userName = ticket.getUser() != null ? ticket.getUser().getName() : "Desconhecido";
        String condoName = ticket.getCondominium() != null ? ticket.getCondominium().getName() : null;
        String unitLabel = null;
        String roleName = null;

        if (ticket.getUser() != null && ticket.getCondominium() != null) {
            Optional<UserCondominiumRole> ucr = userCondominiumRoleRepository
                    .findByUserIdAndCondominiumId(ticket.getUser().getId(), ticket.getCondominium().getId());
            if (ucr.isPresent()) {
                roleName = ucr.get().getRole() != null ? ucr.get().getRole().name() : null;
                unitLabel = ucr.get().getUnit() != null ? ucr.get().getUnit().getIdentifier() : null;
            }
        }

        long unreadCount = supportMessageRepository.countByTicketIdAndReadFalseAndFromAdminFalse(ticket.getId());

        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getStatus(),
                userName,
                condoName,
                unitLabel,
                roleName,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                unreadCount
        );
    }

    private MessageResponse toMessageResponse(SupportMessage msg) {
        return new MessageResponse(
                msg.getId(),
                msg.getTicket().getId(),
                msg.getSender().getId(),
                msg.getSender().getName(),
                msg.getMessage(),
                msg.getFromAdmin(),
                msg.getRead(),
                msg.getCreatedAt()
        );
    }
}
