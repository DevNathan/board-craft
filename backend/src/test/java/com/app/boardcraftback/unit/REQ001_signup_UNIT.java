package com.app.boardcraftback.unit;

import com.app.boardcraftback.domain.entity.user.RoleType;
import com.app.boardcraftback.domain.entity.user.User;
import com.app.boardcraftback.repository.UserRepository;
import com.app.boardcraftback.service.UserServiceImpl;
import com.app.boardcraftback.support.error.FieldValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Trace:
 * - REQ-001 인증/인가 — 회원가입·로그인
 * - T-UNIT-001 ~ T-UNIT-006
 */
@ExtendWith(MockitoExtension.class)
@Tag("REQ-001") // 요구사항 트레이스
class REQ001_signup_UNIT {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        lenient().when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        lenient().when(userRepository.existsByNickname(anyString())).thenReturn(false);
    }

    @Test
    @Tag("T-UNIT-001")
    @DisplayName("[T-UNIT-001][REQ-001] 약관 미동의 시 IllegalArgumentException")
    void registerUser_termsNotAccepted() {
        assertThatThrownBy(() -> userService.registerUser("a@b.com", "pw", "nick", false)).isInstanceOf(FieldValidationException.class).satisfies(ex -> {
            var e = (FieldValidationException) ex;
            assertThat(e.getErrors()).containsEntry("terms", "약관 동의가 필요합니다.");
        });

        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }

    @Test
    @Tag("T-UNIT-002")
    @DisplayName("[T-UNIT-002][REQ-001] 이메일 중복 시 FieldValidationException(email)")
    void registerUser_duplicateEmail() {
        when(userRepository.findByEmailIgnoreCase("dup@email.com")).thenReturn(Optional.of(User.builder().build()));

        assertThatThrownBy(() -> userService.registerUser("dup@email.com", "pw", "nick", true)).isInstanceOf(FieldValidationException.class).satisfies(ex -> {
            var e = (FieldValidationException) ex;
            assertThat(e.getErrors()).containsEntry("email", "이미 사용 중인 이메일입니다.");
        });

        verify(userRepository, never()).existsByNickname(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @Tag("T-UNIT-003")
    @DisplayName("[T-UNIT-003][REQ-001] 닉네임 중복 시 FieldValidationException(nickname)")
    void registerUser_duplicateNickname() {
        when(userRepository.existsByNickname("NICK")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser("ok@email.com", "pw", "NICK", true)).isInstanceOf(FieldValidationException.class).satisfies(ex -> {
            var e = (FieldValidationException) ex;
            assertThat(e.getErrors()).containsEntry("nickname", "이미 사용 중인 닉네임입니다.");
        });

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest(name = "[T-UNIT-004][REQ-001] 이메일 정규화: 입력[{0}] → 저장[{1}]")
    @Tag("T-UNIT-004")
    @CsvSource({"'  USER@Mail.COM  ', user@mail.com", "'USER@MAIL.COM', user@mail.com", "'user@mail.com', user@mail.com"})
    void registerUser_emailNormalization(String input, String expected) {
        when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "ENC(" + inv.getArgument(0) + ")");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.registerUser(input, "pw", "nick", true);

        var captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo(expected);
    }

    @Test
    @Tag("T-UNIT-005")
    @DisplayName("[T-UNIT-005][REQ-001] 정상 등록: 패스워드 해싱, USER 롤 부여, save 호출")
    void registerUser_success() {
        when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "ENC(" + inv.getArgument(0) + ")");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.registerUser("a@b.com", "plainPW", "nick", true);

        var captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        var saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("a@b.com");
        assertThat(saved.getPasswordHash()).isEqualTo("ENC(plainPW)");
        assertThat(saved.getNickname()).isEqualTo("nick");
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.getRoles()).contains(RoleType.USER);

        assertThat(result.getEmail()).isEqualTo("a@b.com");
    }

    @Test
    @Tag("T-UNIT-006")
    @DisplayName("[T-UNIT-006][REQ-001] rawEmail == null → 빈문자열로 정규화되어도 흐름 유지")
    void registerUser_nullEmailBecomesEmptyString() {
        when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "ENC(" + inv.getArgument(0) + ")");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.registerUser(null, "pw", "nick", true);

        var captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("");
    }
}
