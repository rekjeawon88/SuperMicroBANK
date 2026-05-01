package com.bank.user.service;

import com.bank.user.User;
import com.bank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public long getUserCount() {
        return userRepository.count();
    }

    @Transactional
    public User signUp(String email, String password, String name) {
        try {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("이메일은 필수입니다.");
            }
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("비밀번호는 필수입니다.");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("이름은 필수입니다.");
            }
            if (userRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }

            User newUser = User.create(email, password, name);
            return userRepository.save(newUser);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw illegalArgumentException;
        } catch (Exception exception) {
            throw new IllegalStateException("회원가입 처리 중 오류가 발생했습니다.", exception);
        }
    }

    public User login(String email, String password) {
        try {
            if (email == null || email.isBlank() || password == null || password.isBlank()) {
                throw new IllegalArgumentException("이메일과 비밀번호는 필수입니다.");
            }

            User foundUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            if (!foundUser.getPassword().equals(password)) {
                throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
            }

            return foundUser;
        } catch (IllegalArgumentException illegalArgumentException) {
            throw illegalArgumentException;
        } catch (Exception exception) {
            throw new IllegalStateException("로그인 처리 중 오류가 발생했습니다.", exception);
        }
    }
}
