package unit;

import com.app.boardcraftback.domain.entity.user.RoleType;
import com.app.boardcraftback.domain.entity.user.Users;
import com.app.boardcraftback.repository.UsersRepository;
import com.app.boardcraftback.support.error.FieldValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Map.of;

@Service
@RequiredArgsConstructor
@Transactional
public class UsersServiceImpl implements UsersService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 신규 사용자를 등록한다.
     * <p>
     * - 이메일은 {@code null}이면 빈 문자열로 간주하고, 앞뒤 공백 제거 후 소문자로 정규화한다.<br>
     * - 비밀번호는 {@link org.springframework.security.crypto.password.PasswordEncoder}로 해싱 저장한다.<br>
     * - 기본 권한 {@link com.app.boardcraftback.domain.entity.user.RoleType#USER}를 부여한다.<br>
     * - 본 메서드는 쓰기 트랜잭션에서 실행되며, 성공 시 커밋된다.
     * </p>
     *
     * @param rawEmail    가입 이메일 (nullable, 공백 제거 후 소문자 정규화)
     * @param rawPassword 평문 비밀번호 (해싱되어 저장)
     * @param nickname    닉네임 (고유해야 함)
     * @param terms       서비스 약관 동의 여부
     * @return 저장된 {@link Users} 엔티티
     *
     * @throws IllegalArgumentException  약관에 동의하지 않은 경우
     * @throws FieldValidationException
     *         입력 필드가 비즈니스 규칙을 위반한 경우.
     *         <ul>
     *           <li>{@code email}: 이미 사용 중인 이메일</li>
     *           <li>{@code nickname}: 이미 사용 중인 닉네임</li>
     *         </ul>
     *
     * @implNote
     * 이메일/닉네임의 고유성 위반은 DB 유니크 제약에서 발생하기 전에 사전 검증으로
     * {@code FieldValidationException}을 던진다. 컨트롤러 단의 {@code @Valid} 실패와
     * 동일한 응답 포맷(400 + validation map)으로 처리하기 위함이다.
     */
    @Override
    public Users registerUser(String rawEmail, String rawPassword, String nickname, boolean terms) {
        if (!terms) throw new IllegalArgumentException("terms not accepted");

        final String email = rawEmail == null ? "" : rawEmail.trim().toLowerCase();

        // 이메일 고유 무결성 검증 -> IllegalStateException
        assertEmailAvailable(email);
        // 닉네임 고유 무결성 검증 -> IllegalStateException
        assertNicknameAvailable(nickname);

        String hashed = passwordEncoder.encode(rawPassword);

        Users user = Users.builder()
                .email(email)
                .passwordHash(hashed)
                .nickname(nickname)
                .enabled(true)
                .build();
        user.getRoles().add(RoleType.USER);

        return usersRepository.save(user);
    }
    /**
     * 이메일이 사용 가능하지 않으면 {@code FieldValidationException}을 던진다.
     *
     * @param email 정규화된 이메일(비-null)
     * @throws com.app.boardcraftback.support.error.FieldValidationException email 중복 시
     */
    private void assertEmailAvailable(String email) {
        if (usersRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new FieldValidationException(of("email", "이미 사용 중인 이메일입니다."));
        }
    }
    /**
     * 닉네임이 사용 가능하지 않으면 {@code FieldValidationException}을 던진다.
     *
     * @param nickname 닉네임(비-null)
     * @throws com.app.boardcraftback.support.error.FieldValidationException nickname 중복 시
     */
    private void assertNicknameAvailable(String nickname) {
        if (usersRepository.existsByNickname(nickname)) {
            throw new FieldValidationException(of("nickname", "이미 사용 중인 닉네임입니다."));
        }
    }
}
