package com.app.boardcraftback.unit;


import com.app.boardcraftback.domain.entity.user.RoleType;
import com.app.boardcraftback.domain.entity.user.User;
import com.app.boardcraftback.repository.UserRepository;
import com.app.boardcraftback.security.service.AppUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("REQ-001_UNIT")  // 요구사항 트레이스
class REQ001_login_UNIT {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AppUserDetailsService userDetailsService;

    private User user(String email, boolean enabled, Set<RoleType> roles) {
        return User.builder()
                .email(email)
                .passwordHash("{bcrypt}$2a$10$dummyhash") // 형식만 유지
                .nickname("johndoe")
                .enabled(enabled)
                .roles(roles)
                .build();
    }

    @Test
    @Tag("T-UNIT-007")
    @DisplayName("[T-UNIT-007][REQ-001] 정상 로그인: UserDetails 반환 및 권한 매핑 확인")
    void loadUser_success_returnsUserDetails_withAuthorities() {
        // given
        var found = user("user@mail.com", true, Set.of(RoleType.USER, RoleType.MOD));
        when(userRepository.findByEmailIgnoreCase("user@mail.com"))
                .thenReturn(Optional.of(found));

        // when
        var ud = userDetailsService.loadUserByUsername("user@mail.com");

        // then
        assertThat(ud.getUsername()).isEqualTo("user@mail.com");
        assertThat(ud.getPassword()).startsWith("{bcrypt}");
        assertThat(ud.isEnabled()).isTrue();

        // 권한 매핑 (ROLE_ 접두사 가정)
        assertThat(ud.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_MOD");
    }

    @Test
    @Tag("T-UNIT-008")
    @DisplayName("[T-UNIT-008][REQ-001] 미존재 이메일 → UsernameNotFoundException")
    void loadUser_notFound_throwsException() {
        when(userRepository.findByEmailIgnoreCase("nouser@mail.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nouser@mail.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("nouser@mail.com");
    }

    @Test
    @Tag("T-UNIT-009")
    @DisplayName("[T-UNIT-009][REQ-001] 이메일 정규화: 공백/대소문자 → trim + lowerCase")
    void loadUser_emailNormalization_trimAndLowercase() {
        var found = user("user@mail.com", true, Set.of(RoleType.USER));
        when(userRepository.findByEmailIgnoreCase("user@mail.com"))
                .thenReturn(Optional.of(found));

        var ud = userDetailsService.loadUserByUsername("   USER@mail.COM   ");

        assertThat(ud.getUsername()).isEqualTo("user@mail.com");
        verify(userRepository).findByEmailIgnoreCase("user@mail.com");
    }

    @Test
    @Tag("T-UNIT-010")
    @DisplayName("[T-UNIT-010][REQ-001] rawEmail == null → 빈문자열로 정규화되어 탐색, 결과 없으면 예외")
    void loadUser_nullEmail_becomesEmpty_andThrows() {
        when(userRepository.findByEmailIgnoreCase("")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("email: ");
        verify(userRepository).findByEmailIgnoreCase("");
    }

    @Test
    @Tag("T-UNIT-011")
    @DisplayName("[T-UNIT-011][REQ-001] 비활성 사용자(enabled=false) → UserDetails 반환되나 isEnabled=false")
    void loadUser_disabledUser_returnsUserDetails_withEnabledFalse() {
        var found = user("sleep@mail.com", false, Set.of(RoleType.USER));
        when(userRepository.findByEmailIgnoreCase("sleep@mail.com"))
                .thenReturn(Optional.of(found));

        var ud = userDetailsService.loadUserByUsername("sleep@mail.com");

        assertThat(ud.isEnabled()).isFalse();
        // 권한은 매핑되지만, 인증 필터 단계에서 isEnabled=false면 로그인 실패 흐름이 맞다.
        assertThat(ud.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER");
    }
}