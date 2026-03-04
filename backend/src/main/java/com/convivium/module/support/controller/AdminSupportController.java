package com.convivium.module.support.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.module.support.dto.MessageCreateRequest;
import com.convivium.module.support.dto.MessageResponse;
import com.convivium.module.support.dto.TicketResponse;
import com.convivium.module.support.service.SupportChatService;
import com.convivium.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/support/tickets")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
@RequiredArgsConstructor
public class AdminSupportController {

    private final SupportChatService supportChatService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> listTickets(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TicketResponse> result = supportChatService.listAllTickets(status, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(@PathVariable Long id) {
        TicketResponse ticket = supportChatService.getTicket(id);
        return ResponseEntity.ok(ApiResponse.ok(ticket));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(@PathVariable Long id) {
        // Marca mensagens de usuarios como lidas para o admin
        supportChatService.markMessagesAsRead(id, false);
        List<MessageResponse> messages = supportChatService.getMessages(id);
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MessageCreateRequest request) {
        MessageResponse message = supportChatService.sendMessage(id, principal.getId(), request.message(), true);
        return ResponseEntity.ok(ApiResponse.ok(message));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TicketResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        TicketResponse ticket = supportChatService.updateTicketStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(ticket));
    }
}
