package com.example.blogStudy.controller;

import com.example.blogStudy.dto.create.UserCreate;
import com.example.blogStudy.dto.response.ApiResponse;
import com.example.blogStudy.dto.response.UserResponse;
import com.example.blogStudy.dto.update.NameUpdate;
import com.example.blogStudy.dto.update.PasswordUpdate;
import com.example.blogStudy.security.CustomUserDetails;
import com.example.blogStudy.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 유저 전체 조회
    @GetMapping("/users")
    public ApiResponse<List<UserResponse>> getUsers() {
        List<UserResponse> users = userService.getUsers();

        return ApiResponse.success(users);
    }

    // 해당 id 유저 조회
    @GetMapping("/users/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable String id) {
        UserResponse user = userService.getUserById(id);

        return ApiResponse.success(user);
    }

    // 로그인한 본인 정보 조회
    @GetMapping("/users/me")
    public ApiResponse<UserResponse> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        UserResponse me =  userService.getMe(userDetails.getUserId());

        return ApiResponse.success(me);
    }

    // 유저 계정 생성
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreate dto)
    {
        UserResponse created = userService.createUser(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    // 현재 유저 비밀번호 수정
    @PatchMapping("/users/me/password")
    public ResponseEntity<Void> updatePassword(
            @Valid @RequestBody PasswordUpdate dto,
            @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        String id = userDetails.getUserId();
        userService.updatePassword(id, dto);

        return ResponseEntity.noContent().build();
    }

    // 현재 유저 닉네임 수정
    @PatchMapping("/users/me/name")
    public ResponseEntity<Void> updateName(
            @Valid @RequestBody NameUpdate dto,
            @AuthenticationPrincipal CustomUserDetails userDetails)
    {

        String id = userDetails.getUserId();
        userService.updateName(id, dto);

        return ResponseEntity.noContent().build();
    }

    // 유저 계정 탈퇴
    @DeleteMapping("/users/me")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails)
    {

        String id = userDetails.getUserId();
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    // ============= ADMIN API =============
    @PatchMapping("/admin/users/{id}/name")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateNameByAdmin(
            @PathVariable String id,
            @Valid @RequestBody NameUpdate dto)
    {
        userService.updateNameByAdmin(id, dto);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserByAdmin(@PathVariable String id) {
        userService.deleteUserByAdmin(id);

        return ResponseEntity.noContent().build();
    }
}
