package com.convivium.module.support.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.module.support.dto.MessageResponse;
import com.convivium.module.support.dto.TicketResponse;
import com.convivium.module.support.entity.SupportMessage;
import com.convivium.module.support.entity.SupportTicket;
import com.convivium.module.user.entity.User;
import com.convivium.module.user.repository.UserCondominiumRoleRepository;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.module.support.repository.SupportMessageRepository;
import com.convivium.module.support.repository.SupportTicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportChatServiceTest {

    @Mock
    private SupportTicketRepository supportTicketRepository;

    @Mock
    private SupportMessageRepository supportMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCondominiumRoleRepository userCondominiumRoleRepository;

    private SupportChatService supportChatService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        supportChatService = new SupportChatService(
                supportTicketRepository,
                supportMessageRepository,
                userRepository,
                userCondominiumRoleRepository,
                Optional.empty()
        );
    }

    private User createUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }

    private SupportTicket createTicket(Long id, User user, String subject, String status) {
        SupportTicket ticket = SupportTicket.builder()
                .id(id)
                .user(user)
                .subject(subject)
                .status(status)
                .build();
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        return ticket;
    }

    // ---- createTicket ----

    @Test
    void createTicket_createsAndReturns() {
        User user = createUser(1L, "Joao");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        SupportTicket saved = createTicket(1L, user, "Ajuda", "OPEN");
        when(supportTicketRepository.save(any())).thenReturn(saved);
        when(supportMessageRepository.countByTicketIdAndReadFalseAndFromAdminFalse(1L)).thenReturn(0L);

        TicketResponse result = supportChatService.createTicket(1L, "Ajuda");

        assertThat(result.subject()).isEqualTo("Ajuda");
        assertThat(result.status()).isEqualTo("OPEN");
        assertThat(result.userName()).isEqualTo("Joao");
        verify(supportTicketRepository).save(any(SupportTicket.class));
    }

    @Test
    void createTicket_userNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supportChatService.createTicket(99L, "Ajuda"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Usuario nao encontrado");
    }

    // ---- listTicketsByUser ----

    @Test
    void listTicketsByUser_returnsList() {
        User user = createUser(1L, "Joao");
        SupportTicket ticket = createTicket(1L, user, "Problema", "OPEN");

        when(supportTicketRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(ticket));
        when(supportMessageRepository.countByTicketIdAndReadFalseAndFromAdminFalse(1L)).thenReturn(2L);

        List<TicketResponse> result = supportChatService.listTicketsByUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).unreadCount()).isEqualTo(2);
    }

    // ---- getTicket ----

    @Test
    void getTicket_returnsTicket() {
        User user = createUser(1L, "Joao");
        SupportTicket ticket = createTicket(1L, user, "Problema", "OPEN");

        when(supportTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(supportMessageRepository.countByTicketIdAndReadFalseAndFromAdminFalse(1L)).thenReturn(0L);

        TicketResponse result = supportChatService.getTicket(1L);

        assertThat(result.subject()).isEqualTo("Problema");
    }

    @Test
    void getTicket_notFound_throws() {
        when(supportTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supportChatService.getTicket(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Chamado nao encontrado");
    }

    // ---- listAllTickets ----

    @Test
    void listAllTickets_withStatus_filtersCorrectly() {
        User user = createUser(1L, "Joao");
        SupportTicket ticket = createTicket(1L, user, "Test", "OPEN");
        Page<SupportTicket> page = new PageImpl<>(List.of(ticket));

        when(supportTicketRepository.findByStatusOrderByCreatedAtDesc("OPEN", PageRequest.of(0, 20)))
                .thenReturn(page);
        when(supportMessageRepository.countByTicketIdAndReadFalseAndFromAdminFalse(1L)).thenReturn(0L);

        Page<TicketResponse> result = supportChatService.listAllTickets("OPEN", 0, 20);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void listAllTickets_withoutStatus_returnsAll() {
        User user = createUser(1L, "Joao");
        SupportTicket ticket = createTicket(1L, user, "Test", "OPEN");
        Page<SupportTicket> page = new PageImpl<>(List.of(ticket));

        when(supportTicketRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 20)))
                .thenReturn(page);
        when(supportMessageRepository.countByTicketIdAndReadFalseAndFromAdminFalse(1L)).thenReturn(0L);

        Page<TicketResponse> result = supportChatService.listAllTickets(null, 0, 20);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ---- updateTicketStatus ----

    @Test
    void updateTicketStatus_updatesAndReturns() {
        User user = createUser(1L, "Joao");
        SupportTicket ticket = createTicket(1L, user, "Test", "OPEN");

        when(supportTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(supportTicketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(supportMessageRepository.countByTicketIdAndReadFalseAndFromAdminFalse(1L)).thenReturn(0L);

        TicketResponse result = supportChatService.updateTicketStatus(1L, "RESOLVED");

        assertThat(result.status()).isEqualTo("RESOLVED");
    }

    @Test
    void updateTicketStatus_notFound_throws() {
        when(supportTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supportChatService.updateTicketStatus(99L, "RESOLVED"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Chamado nao encontrado");
    }

    // ---- sendMessage ----

    @Test
    void sendMessage_createsMessageAndReturns() {
        User user = createUser(1L, "Joao");
        SupportTicket ticket = createTicket(1L, user, "Test", "IN_PROGRESS");

        when(supportTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        SupportMessage saved = SupportMessage.builder()
                .id(1L).ticket(ticket).sender(user).message("Ola").fromAdmin(false).read(false).build();
        saved.setCreatedAt(Instant.now());
        when(supportMessageRepository.save(any())).thenReturn(saved);

        MessageResponse result = supportChatService.sendMessage(1L, 1L, "Ola", false);

        assertThat(result.message()).isEqualTo("Ola");
        assertThat(result.fromAdmin()).isFalse();
    }

    @Test
    void sendMessage_adminResponseChangesOpenToInProgress() {
        User admin = createUser(2L, "Admin");
        User user = createUser(1L, "Joao");
        SupportTicket ticket = createTicket(1L, user, "Test", "OPEN");

        when(supportTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));

        SupportMessage saved = SupportMessage.builder()
                .id(1L).ticket(ticket).sender(admin).message("Resposta").fromAdmin(true).read(false).build();
        saved.setCreatedAt(Instant.now());
        when(supportMessageRepository.save(any())).thenReturn(saved);

        supportChatService.sendMessage(1L, 2L, "Resposta", true);

        assertThat(ticket.getStatus()).isEqualTo("IN_PROGRESS");
        verify(supportTicketRepository).save(ticket);
    }

    @Test
    void sendMessage_adminResponseOnNonOpenTicketDoesNotChangeStatus() {
        User admin = createUser(2L, "Admin");
        User user = createUser(1L, "Joao");
        SupportTicket ticket = createTicket(1L, user, "Test", "IN_PROGRESS");

        when(supportTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));

        SupportMessage saved = SupportMessage.builder()
                .id(1L).ticket(ticket).sender(admin).message("Resp").fromAdmin(true).read(false).build();
        saved.setCreatedAt(Instant.now());
        when(supportMessageRepository.save(any())).thenReturn(saved);

        supportChatService.sendMessage(1L, 2L, "Resp", true);

        assertThat(ticket.getStatus()).isEqualTo("IN_PROGRESS");
        verify(supportTicketRepository, never()).save(ticket);
    }

    @Test
    void sendMessage_ticketNotFound_throws() {
        when(supportTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supportChatService.sendMessage(99L, 1L, "Msg", false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Chamado nao encontrado");
    }

    @Test
    void sendMessage_senderNotFound_throws() {
        User user = createUser(1L, "Joao");
        SupportTicket ticket = createTicket(1L, user, "Test", "OPEN");

        when(supportTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supportChatService.sendMessage(1L, 99L, "Msg", false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Usuario nao encontrado");
    }

    // ---- getMessages ----

    @Test
    void getMessages_returnsList() {
        User user = createUser(1L, "Joao");
        SupportTicket ticket = createTicket(1L, user, "Test", "OPEN");
        SupportMessage msg = SupportMessage.builder()
                .id(1L).ticket(ticket).sender(user).message("Ola").fromAdmin(false).read(false).build();
        msg.setCreatedAt(Instant.now());

        when(supportMessageRepository.findByTicketIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(msg));

        List<MessageResponse> result = supportChatService.getMessages(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).message()).isEqualTo("Ola");
    }

    // ---- markMessagesAsRead ----

    @Test
    void markMessagesAsRead_callsRepository() {
        supportChatService.markMessagesAsRead(1L, true);

        verify(supportMessageRepository).markAsRead(1L, true);
    }
}
