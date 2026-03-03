package com.convivium.module.complaint.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.common.exception.ResourceNotFoundException;
import com.convivium.module.complaint.dto.ComplaintCreateRequest;
import com.convivium.module.complaint.dto.ComplaintListResponse;
import com.convivium.module.complaint.dto.ComplaintResponseCreateRequest;
import com.convivium.module.complaint.entity.Complaint;
import com.convivium.module.complaint.entity.ComplaintCategory;
import com.convivium.module.complaint.entity.ComplaintResponse;
import com.convivium.module.complaint.entity.ComplaintStatus;
import com.convivium.module.complaint.repository.ComplaintRepository;
import com.convivium.module.complaint.repository.ComplaintResponseRepository;
import com.convivium.module.condominium.repository.UnitRepository;
import com.convivium.module.user.entity.User;
import com.convivium.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;
    @Mock
    private ComplaintResponseRepository complaintResponseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private ComplaintService complaintService;

    @Test
    void listComplaints_returnsPage() {
        Complaint c = createComplaint(1L);
        when(complaintRepository.findByCondominiumId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(c)));
        Page<ComplaintListResponse> result = complaintService.listComplaints(1L, null, Pageable.unpaged());
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getComplaint_throwsWhenNotFound() {
        when(complaintRepository.findByIdAndCondominiumId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> complaintService.getComplaint(1L, 1L));
    }

    @Test
    void getComplaint_success() {
        Complaint c = createComplaint(1L);
        when(complaintRepository.findByIdAndCondominiumId(1L, 1L)).thenReturn(Optional.of(c));
        when(complaintResponseRepository.findByComplaintIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
        var r = complaintService.getComplaint(1L, 1L);
        assertNotNull(r);
        assertEquals("Reclamacao", r.title());
    }

    @Test
    void createComplaint_throwsWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        ComplaintCreateRequest req = new ComplaintCreateRequest("NOISE", "Titulo", "Desc", false, null, null);
        assertThrows(ResourceNotFoundException.class, () -> complaintService.createComplaint(1L, 1L, req));
    }

    @Test
    void createComplaint_throwsWhenInvalidCategory() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L)));
        ComplaintCreateRequest req = new ComplaintCreateRequest("INVALID", "Titulo", "Desc", false, null, null);
        assertThrows(BusinessException.class, () -> complaintService.createComplaint(1L, 1L, req));
    }

    @Test
    void createComplaint_success() {
        User user = createUser(1L);
        Complaint saved = createComplaint(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(complaintRepository.save(any())).thenReturn(saved);
        ComplaintCreateRequest req = new ComplaintCreateRequest("NOISE", "Titulo", "Descricao", false, null, null);
        ComplaintListResponse r = complaintService.createComplaint(1L, 1L, req);
        assertNotNull(r);
        verify(complaintRepository).save(any(Complaint.class));
    }

    @Test
    void addResponse_success() {
        Complaint c = createComplaint(1L);
        User responder = createUser(1L);
        ComplaintResponse cr = new ComplaintResponse();
        cr.setId(1L);
        cr.setMessage("Resposta");
        cr.setInternal(false);
        cr.setResponder(responder);
        cr.setComplaint(c);
        when(complaintRepository.findByIdAndCondominiumId(1L, 1L)).thenReturn(Optional.of(c));
        when(userRepository.findById(1L)).thenReturn(Optional.of(responder));
        when(complaintResponseRepository.save(any())).thenReturn(cr);
        when(complaintRepository.save(any())).thenReturn(c);
        ComplaintResponseCreateRequest req = new ComplaintResponseCreateRequest("Resposta", false);
        var r = complaintService.addResponse(1L, 1L, 1L, req);
        assertNotNull(r);
        assertEquals("Resposta", r.message());
    }

    @Test
    void updateStatus_throwsWhenInvalidTransition() {
        Complaint c = createComplaint(1L);
        c.setStatus(ComplaintStatus.CLOSED);
        when(complaintRepository.findByIdAndCondominiumId(1L, 1L)).thenReturn(Optional.of(c));
        assertThrows(BusinessException.class, () -> complaintService.updateStatus(1L, 1L, "OPEN"));
    }

    @Test
    void updateStatus_success() {
        Complaint c = createComplaint(1L);
        c.setStatus(ComplaintStatus.IN_REVIEW);
        when(complaintRepository.findByIdAndCondominiumId(1L, 1L)).thenReturn(Optional.of(c));
        when(complaintRepository.save(any())).thenReturn(c);
        complaintService.updateStatus(1L, 1L, "RESOLVED");
        verify(complaintRepository).save(argThat(x -> ComplaintStatus.RESOLVED == x.getStatus()));
    }

    @Test
    void getMyComplaints_returnsPage() {
        Complaint c = createComplaint(1L);
        when(complaintRepository.findByCondominiumIdAndComplainantId(eq(1L), eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(c)));
        Page<ComplaintListResponse> result = complaintService.getMyComplaints(1L, 1L, null, Pageable.unpaged());
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    private Complaint createComplaint(Long id) {
        Complaint c = new Complaint();
        c.setId(id);
        c.setCondominiumId(1L);
        c.setComplainant(createUser(1L));
        c.setCategory(ComplaintCategory.NOISE);
        c.setTitle("Reclamacao");
        c.setDescription("Desc");
        c.setStatus(ComplaintStatus.OPEN);
        c.setPriority("MEDIUM");
        c.setAnonymous(false);
        c.setResponses(new java.util.ArrayList<>());
        c.setAttachments(new java.util.ArrayList<>());
        return c;
    }

    private User createUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setName("User");
        u.setEmail("user@test.com");
        u.setCondominiumRoles(new java.util.ArrayList<>());
        return u;
    }
}
