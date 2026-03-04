package com.convivium.module.support.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.module.support.dto.MessageCreateRequest;
import com.convivium.module.support.dto.MessageResponse;
import com.convivium.module.support.dto.TicketCreateRequest;
import com.convivium.module.support.dto.TicketResponse;
import com.convivium.module.support.service.SupportChatService;
import com.convivium.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support/tickets")
@RequiredArgsConstructor
public class SupportChatController {

    private final SupportChatService supportChatService;

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TicketCreateRequest request) {
        TicketResponse ticket = supportChatService.createTicket(principal.getId(), request.subject());
        return ResponseEntity.ok(ApiResponse.ok(ticket));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketResponse>>> listMyTickets(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<TicketResponse> tickets = supportChatService.listTicketsByUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(tickets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(@PathVariable Long id) {
        TicketResponse ticket = supportChatService.getTicket(id);
        return ResponseEntity.ok(ApiResponse.ok(ticket));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        // Marca mensagens do admin como lidas para o usuario
        supportChatService.markMessagesAsRead(id, true);
        List<MessageResponse> messages = supportChatService.getMessages(id);
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MessageCreateRequest request) {
        MessageResponse message = supportChatService.sendMessage(id, principal.getId(), request.message(), false);
        return ResponseEntity.ok(ApiResponse.ok(message));
    }
}
