package com.app.boardcraftback.security.userDetail;

import com.app.boardcraftback.domain.entity.user.RoleType;
import com.app.boardcraftback.domain.entity.user.User;
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
    public record AppUser(String userId, String nickname, String email, Set<RoleType> roles) {
    }

    // 클라이언트 측에서 사용할 사용자 정보 묶음
    private final AppUser appUser;
    private final String password;
    private final boolean enabled;

    public AppUserDetails(User u) {
        this.appUser = new AppUser(
                u.getId(),
                u.getNickname(),
                u.getEmail(),
                u.getRoles()
        );
        this.password = u.getPasswordHash();
        this.enabled = u.isEnabled();
    }

    @Override
    public String getUsername() {
        return appUser.email();
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
        return appUser.roles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .collect(Collectors.toSet());
    }
}