package com.example.blogStudy.controller;

import com.example.blogStudy.dto.create.UserCreate;
import com.example.blogStudy.dto.response.UserResponse;
import com.example.blogStudy.dto.update.NameUpdate;
import com.example.blogStudy.dto.update.PasswordUpdate;
import com.example.blogStudy.security.CustomUserDetails;
import com.example.blogStudy.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 유저 전체 조회
    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        return userService.getUsers();
    }

    // 해당 id 유저 조회
    @GetMapping("/users/{id}")
    public UserResponse getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    // 로그인한 본인 정보 조회
    @GetMapping("/users/me")
    public UserResponse getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.getMe(userDetails.getUserId());
    }

    // 유저 계정 생성
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreate dto) {
        UserResponse created = userService.createUser(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    // 현재 유저 비밀번호 수정
    @PatchMapping("/users/me/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody PasswordUpdate dto,
                                               @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        String id = userDetails.getUsername();
        userService.updatePassword(id, dto);

        return ResponseEntity.noContent().build();
    }

    // 현재 유저 닉네임 수정
    @PatchMapping("/users/me/name")
    public ResponseEntity<Void> updateName(@Valid @RequestBody NameUpdate dto,
                                           @AuthenticationPrincipal CustomUserDetails userDetails)
    {

        String id = userDetails.getUserId();
        userService.updateName(id, dto);

        return ResponseEntity.noContent().build();
    }

    // 유저 계정 탈퇴
    @DeleteMapping("/users/me")
    public ResponseEntity<UserResponse> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {

        String id = userDetails.getUserId();
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}
