package com.convivium.module.user.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.common.exception.ResourceNotFoundException;
import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.entity.Unit;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.condominium.repository.UnitRepository;
import com.convivium.module.user.dto.UserCreateRequest;
import com.convivium.module.user.dto.UserResponse;
import com.convivium.module.user.dto.UserUpdateRequest;
import com.convivium.module.user.entity.Role;
import com.convivium.module.user.entity.User;
import com.convivium.module.user.entity.UserCondominiumRole;
import com.convivium.module.user.repository.UserCondominiumRoleRepository;
import com.convivium.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCondominiumRoleRepository userCondominiumRoleRepository;

    @Mock
    private CondominiumRepository condominiumRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void listUsers_returnsPage() {
        Long condoId = 1L;
        User user = createUser(1L);
        Condominium condo = Condominium.builder().id(condoId).name("Condo").build();
        Unit unit = Unit.builder().id(10L).identifier("101").condominiumId(condoId).build();
        UserCondominiumRole ucr = UserCondominiumRole.builder()
                .user(user)
                .condominium(condo)
                .role(Role.MORADOR)
                .unit(unit)
                .status("ACTIVE")
                .build();

        Page<UserCondominiumRole> page = new PageImpl<>(List.of(ucr));
        when(userCondominiumRoleRepository.findByCondominiumId(eq(condoId), any(Pageable.class)))
                .thenReturn(page);

        Page<UserResponse> result = userService.listUsers(condoId, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("user@test.com", result.getContent().get(0).email());
    }

    @Test
    void createUser_throwsWhenEmailExists() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        UserCreateRequest request = new UserCreateRequest(
                "User", "user@test.com", "senha123", null, null, "MORADOR", null);

        assertThrows(BusinessException.class, () ->
                userService.createUser(1L, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_throwsWhenCpfExists() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(userRepository.existsByCpf("12345678901")).thenReturn(true);

        UserCreateRequest request = new UserCreateRequest(
                "User", "user@test.com", "senha123", "123.456.789-01", null, "MORADOR", null);

        assertThrows(BusinessException.class, () ->
                userService.createUser(1L, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_throwsWhenCondominiumNotFound() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(condominiumRepository.findById(1L)).thenReturn(Optional.empty());

        UserCreateRequest request = new UserCreateRequest(
                "User", "user@test.com", "senha123", null, null, "MORADOR", null);

        assertThrows(ResourceNotFoundException.class, () ->
                userService.createUser(1L, request));
    }

    @Test
    void createUser_success() {
        Long condoId = 1L;
        Condominium condo = Condominium.builder().id(condoId).name("Condo").build();
        User savedUser = createUser(1L);
        UserCondominiumRole savedUcr = UserCondominiumRole.builder()
                .user(savedUser)
                .condominium(condo)
                .role(Role.MORADOR)
                .status("ACTIVE")
                .build();

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(condominiumRepository.findById(condoId)).thenReturn(Optional.of(condo));
        when(passwordEncoder.encode("senha123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userCondominiumRoleRepository.save(any(UserCondominiumRole.class)))
                .thenAnswer(i -> i.getArgument(0));

        UserCreateRequest request = new UserCreateRequest(
                "User", "user@test.com", "senha123", null, null, "MORADOR", null);

        UserResponse response = userService.createUser(condoId, request);

        assertNotNull(response);
        assertEquals("user@test.com", response.email());
        assertEquals("MORADOR", response.role());
        verify(userRepository).save(any(User.class));
        verify(userCondominiumRoleRepository).save(any(UserCondominiumRole.class));
    }

    @Test
    void getUser_throwsWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.getUser(1L, 999L));
    }

    @Test
    void getUser_throwsWhenUserNotInCondominium() {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userCondominiumRoleRepository.findByUserIdAndCondominiumId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                userService.getUser(1L, 1L));
    }

    @Test
    void getUser_success() {
        User user = createUser(1L);
        Condominium condo = Condominium.builder().id(1L).build();
        Unit unit = Unit.builder().id(10L).identifier("101").build();
        UserCondominiumRole ucr = UserCondominiumRole.builder()
                .user(user)
                .condominium(condo)
                .role(Role.MORADOR)
                .unit(unit)
                .status("ACTIVE")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userCondominiumRoleRepository.findByUserIdAndCondominiumId(1L, 1L))
                .thenReturn(Optional.of(ucr));

        UserResponse response = userService.getUser(1L, 1L);

        assertNotNull(response);
        assertEquals("user@test.com", response.email());
        assertEquals("101", response.unitIdentifier());
    }

    @Test
    void updateUser_success() {
        User user = createUser(1L);
        Condominium condo = Condominium.builder().id(1L).build();
        UserCondominiumRole ucr = UserCondominiumRole.builder()
                .user(user)
                .condominium(condo)
                .role(Role.MORADOR)
                .status("ACTIVE")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userCondominiumRoleRepository.findByUserIdAndCondominiumId(1L, 1L))
                .thenReturn(Optional.of(ucr));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest("New Name", "11999999999", null, null, null);

        UserResponse response = userService.updateUser(1L, 1L, request);

        assertNotNull(response);
        verify(userRepository).save(argThat(u -> "New Name".equals(u.getName())));
    }

    @Test
    void deleteUser_success() {
        UserCondominiumRole ucr = UserCondominiumRole.builder().id(1L).build();
        when(userCondominiumRoleRepository.findByUserIdAndCondominiumId(1L, 1L))
                .thenReturn(Optional.of(ucr));

        userService.deleteUser(1L, 1L);

        verify(userCondominiumRoleRepository).delete(ucr);
    }

    @Test
    void deleteUser_throwsWhenUserNotInCondominium() {
        when(userCondominiumRoleRepository.findByUserIdAndCondominiumId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                userService.deleteUser(1L, 1L));
    }

    @Test
    void getPendingApprovals_returnsList() {
        User user = createUser(1L);
        Condominium condo = Condominium.builder().id(1L).build();
        UserCondominiumRole ucr = UserCondominiumRole.builder()
                .user(user)
                .condominium(condo)
                .role(Role.MORADOR)
                .status("PENDING_APPROVAL")
                .build();

        when(userCondominiumRoleRepository.findByCondominiumIdAndStatus(1L, "PENDING_APPROVAL"))
                .thenReturn(List.of(ucr));

        List<UserResponse> result = userService.getPendingApprovals(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void approveUser_success() {
        User user = createUser(1L);
        Condominium condo = Condominium.builder().id(1L).build();
        UserCondominiumRole ucr = UserCondominiumRole.builder()
                .user(user)
                .condominium(condo)
                .role(Role.MORADOR)
                .status("PENDING_APPROVAL")
                .build();

        when(userCondominiumRoleRepository.findByUserIdAndCondominiumId(1L, 1L))
                .thenReturn(Optional.of(ucr));
        when(userCondominiumRoleRepository.save(any(UserCondominiumRole.class)))
                .thenAnswer(i -> i.getArgument(0));

        userService.approveUser(1L, 1L, 2L, null);

        verify(userCondominiumRoleRepository).save(argThat(u ->
                "ACTIVE".equals(u.getStatus()) && u.getApprovedBy() != null));
    }

    @Test
    void approveUser_throwsWhenNotPending() {
        UserCondominiumRole ucr = UserCondominiumRole.builder()
                .status("ACTIVE")
                .build();
        when(userCondominiumRoleRepository.findByUserIdAndCondominiumId(1L, 1L))
                .thenReturn(Optional.of(ucr));

        assertThrows(BusinessException.class, () ->
                userService.approveUser(1L, 1L, 2L, null));
    }

    @Test
    void rejectUser_success() {
        UserCondominiumRole ucr = UserCondominiumRole.builder()
                .id(1L)
                .status("PENDING_APPROVAL")
                .build();
        when(userCondominiumRoleRepository.findByUserIdAndCondominiumId(1L, 1L))
                .thenReturn(Optional.of(ucr));

        userService.rejectUser(1L, 1L);

        verify(userCondominiumRoleRepository).delete(ucr);
    }

    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUuid(UUID.randomUUID());
        user.setEmail("user@test.com");
        user.setName("User");
        user.setPasswordHash("hash");
        user.setActive(true);
        return user;
    }
}
