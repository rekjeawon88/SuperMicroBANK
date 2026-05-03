package com.bank.user.service;

import com.bank.user.User;
import com.bank.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.create("test@bank.com", "password123", "홍길동");
    }

    // =========================================================
    // signUp
    // =========================================================
    @Nested
    @DisplayName("signUp()")
    class SignUp {

        @Test
        @DisplayName("정상 - 올바른 정보로 회원가입 시 저장된 유저를 반환한다")
        void signUp_success() {
            // given
            given(userRepository.findByEmail("test@bank.com")).willReturn(Optional.empty());
            given(userRepository.save(any(User.class))).willReturn(mockUser);

            // when
            User result = userService.signUp("test@bank.com", "password123", "홍길동");

            // then
            assertThat(result.getEmail()).isEqualTo("test@bank.com");
            assertThat(result.getName()).isEqualTo("홍길동");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("예외 - 이메일이 null이면 IllegalArgumentException 발생")
        void signUp_emailNull_throwsException() {
            assertThatThrownBy(() -> userService.signUp(null, "password123", "홍길동"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 이메일이 공백이면 IllegalArgumentException 발생")
        void signUp_emailBlank_throwsException() {
            assertThatThrownBy(() -> userService.signUp("  ", "password123", "홍길동"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 비밀번호가 null이면 IllegalArgumentException 발생")
        void signUp_passwordNull_throwsException() {
            assertThatThrownBy(() -> userService.signUp("test@bank.com", null, "홍길동"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비밀번호");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 이름이 null이면 IllegalArgumentException 발생")
        void signUp_nameNull_throwsException() {
            assertThatThrownBy(() -> userService.signUp("test@bank.com", "password123", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이름");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 이미 사용 중인 이메일이면 IllegalArgumentException 발생")
        void signUp_duplicateEmail_throwsException() {
            given(userRepository.findByEmail("test@bank.com")).willReturn(Optional.of(mockUser));

            assertThatThrownBy(() -> userService.signUp("test@bank.com", "password123", "홍길동"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 사용 중인 이메일");

            verify(userRepository, never()).save(any());
        }
    }

    // =========================================================
    // login
    // =========================================================
    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("정상 - 올바른 이메일과 비밀번호로 로그인 성공")
        void login_success() {
            given(userRepository.findByEmail("test@bank.com")).willReturn(Optional.of(mockUser));

            User result = userService.login("test@bank.com", "password123");

            assertThat(result.getEmail()).isEqualTo("test@bank.com");
        }

        @Test
        @DisplayName("예외 - 이메일이 null이면 IllegalArgumentException 발생")
        void login_emailNull_throwsException() {
            assertThatThrownBy(() -> userService.login(null, "password123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("필수");
        }

        @Test
        @DisplayName("예외 - 비밀번호가 공백이면 IllegalArgumentException 발생")
        void login_passwordBlank_throwsException() {
            assertThatThrownBy(() -> userService.login("test@bank.com", "  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("필수");
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 이메일로 로그인 시 IllegalArgumentException 발생")
        void login_userNotFound_throwsException() {
            given(userRepository.findByEmail("notfound@bank.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login("notfound@bank.com", "password123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("예외 - 비밀번호 불일치 시 IllegalArgumentException 발생")
        void login_wrongPassword_throwsException() {
            given(userRepository.findByEmail("test@bank.com")).willReturn(Optional.of(mockUser));

            assertThatThrownBy(() -> userService.login("test@bank.com", "wrongPassword"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비밀번호가 올바르지 않습니다");
        }
    }
}
