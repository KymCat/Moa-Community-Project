package com.example.blogStudy.service;

import com.example.blogStudy.dto.create.UserCreate;
import com.example.blogStudy.dto.response.UserResponse;
import com.example.blogStudy.dto.update.NameUpdate;
import com.example.blogStudy.dto.update.PasswordUpdate;
import com.example.blogStudy.entity.User;
import com.example.blogStudy.exception.CustomException;
import com.example.blogStudy.exception.ErrorCode;
import com.example.blogStudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 유저 전체 조회
    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    // 해당 id 유저 조회
    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }

    // 로그인한 본인 정보 조회
    public UserResponse getMe(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }

    // 유저 계정 생성
    @Transactional
    public UserResponse createUser(UserCreate dto) {
        String bcryptPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toEntity(bcryptPassword);

        try {
            User saved = userRepository.saveAndFlush(user); // try 문에서 flush 발생시키기
            return UserResponse.from(saved);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent duplicate create userId = {}", dto.getId());
            throw new CustomException(ErrorCode.DUPLICATE_USER_ID);
        }
    }

    // 현재 유저 비밀번호 수정
    @Transactional
    public void updatePassword(String id, PasswordUpdate dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String currentPassword = dto.getCurrentPassword();
        String newPassword = dto.getNewPassword();

        // 기존 패스워드, 새 패스워드 비교
        if (currentPassword.equals(newPassword))
            throw new CustomException(ErrorCode.SAME_AS_CURRENT_VALUE);

        // 기존 패스워드 일치 여부
        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            throw new CustomException(ErrorCode.INVALID_PASSWORD);

        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    // 현재 유저 닉네임 수정
    @Transactional
    public void updateName(String id, NameUpdate dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newName = dto.getName();

        // 기존 닉네임, 새 닉네임 비교
        if (newName.equals(user.getName()))
            throw new CustomException(ErrorCode.SAME_AS_CURRENT_VALUE);

        user.updateName(newName);
    }

    // 유저 계정 삭제
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }
}
