package com.recruitmentplatform.auth.security;

import com.recruitmentplatform.auth.entity.UserCredential;
import com.recruitmentplatform.auth.enums.AccountRole;
import com.recruitmentplatform.auth.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final AccountRole role;
    private final AccountStatus status;
    private final boolean emailVerified;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(UserCredential credential) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + credential.getRole().name()));
        return UserPrincipal.builder()
                .id(credential.getId())
                .email(credential.getEmail())
                .password(credential.getPasswordHash())
                .role(credential.getRole())
                .status(credential.getStatus())
                .emailVerified(credential.isEmailVerified())
                .authorities(authorities)
                .build();
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
        return status != AccountStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == AccountStatus.ACTIVE;
    }
}
