package com.app.boardcraftback.security;

import com.app.boardcraftback.domain.entity.user.RoleType;
import com.app.boardcraftback.domain.entity.user.Users;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter @ToString
public class AppUserDetails implements UserDetails {
    private final String userId;   // UUID 문자열(CHAR(36))
    private final String email;    // 로그인용
    private final String password; // bcrypt
    private final boolean enabled;
    private final Set<RoleType> roles;

    public AppUserDetails(Users u) {
        this.userId = u.getId();
        this.email = u.getEmail();
        this.password = u.getPasswordHash();
        this.enabled = u.isEnabled();
        this.roles = u.getRoles();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .collect(Collectors.toSet());
    }
}