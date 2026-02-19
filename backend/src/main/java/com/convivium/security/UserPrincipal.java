package com.convivium.security;

import com.convivium.module.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String uuid;
    private final String email;
    private final String password;
    private final String name;
    private final Long condominiumId;
    private final boolean isPlatformAdmin;
    private final boolean active;
    private final Collection<GrantedAuthority> authorities;

    public static UserPrincipal fromUser(User user, Long condominiumId,
                                         List<String> roles, List<String> permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (roles != null) {
            roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .forEach(authorities::add);
        }

        if (permissions != null) {
            permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        return UserPrincipal.builder()
                .id(user.getId())
                .uuid(user.getUuid() != null ? user.getUuid().toString() : null)
                .email(user.getEmail())
                .password(user.getPasswordHash() != null ? user.getPasswordHash() : "{noop}__no_password__")
                .name(user.getName())
                .condominiumId(condominiumId)
                .isPlatformAdmin(user.isPlatformAdmin())
                .active(user.isActive())
                .authorities(authorities)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
