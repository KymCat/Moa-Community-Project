package com.example.blogStudy.controller;

import com.example.blogStudy.dto.create.PostCreate;
import com.example.blogStudy.dto.request.PostSearchType;
import com.example.blogStudy.dto.response.ApiResponse;
import com.example.blogStudy.dto.response.PostDetailResponse;
import com.example.blogStudy.dto.response.PostResponse;
import com.example.blogStudy.dto.update.PostUpdate;
import com.example.blogStudy.entity.PostListMode;
import com.example.blogStudy.entity.Role;
import com.example.blogStudy.security.CustomUserDetails;
import com.example.blogStudy.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Validated
@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 전체 조회
    @GetMapping("/posts")
    public ApiResponse<PagedModel<PostResponse>> getPosts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "ALL") PostListMode mode)
    {
        PagedModel<PostResponse> pages = postService.getPosts(page, mode);

        return ApiResponse.success(pages);
    }

    // 게시글 단일 조회
    @GetMapping("/posts/{id}")
    public ApiResponse<PostDetailResponse> getPost(@PathVariable Long id) {
        PostDetailResponse page = postService.getPost(id);

        return ApiResponse.success(page);
    }

    // 게시글 작성
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostCreate dto)
    {

        String userId = userDetails.getUserId();
        PostResponse created = postService.createPost(userId, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    // 게시글 수정
    @PatchMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody PostUpdate dto)
    {
        String userId = userDetails.getUserId();
        Role userRole = userDetails.getUserRole();
        PostResponse updated = postService.updatePost(userId, userRole, id, dto);

        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id)
    {
        String userId = userDetails.getUserId();
        Role userRole = userDetails.getUserRole();
        postService.deletePost(userId, userRole, id);

        return ResponseEntity.noContent().build();
    }

    // 게시글 검색
    @GetMapping("/posts/search")
    public ApiResponse<PagedModel<PostResponse>> searchPosts(
            @RequestParam PostSearchType type,
            @RequestParam @NotBlank @Size(max = 100) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {

        PagedModel<PostResponse> pages = postService.searchPosts(type, keyword, page);

        return ApiResponse.success(pages);
    }

    // 마이페이지 본인 게시글 조회
    @GetMapping("/posts/me")
    public ApiResponse<PagedModel<PostResponse>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") @Min(0) int page)
    {
        String userId = userDetails.getUsername();
        PagedModel<PostResponse> pages = postService.getMyPosts(userId, page);

        return ApiResponse.success(pages);
    }
}
