package com.example.blogStudy.controller;

import com.example.blogStudy.dto.create.PostCreate;
import com.example.blogStudy.dto.request.PostSearchType;
import com.example.blogStudy.dto.response.PostDetailResponse;
import com.example.blogStudy.dto.response.PostResponse;
import com.example.blogStudy.dto.update.PostUpdate;
import com.example.blogStudy.security.CustomUserDetails;
import com.example.blogStudy.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public PagedModel<PostResponse> getPosts(
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable)
    {
        return postService.getPosts(pageable);
    }

    // 게시글 단일 조회
    @GetMapping("/posts/{id}")
    public PostDetailResponse getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }

    // 게시글 작성
    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createPost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @Valid @RequestBody PostCreate dto) {

        String userId = userDetails.getUserId();
        PostResponse created = postService.createPost(userId, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    // 게시글 수정
    @PatchMapping("/posts/{id}")
    public ResponseEntity<PostResponse> updatePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody PostUpdate dto) {
        String userId = userDetails.getUserId();
        PostResponse updated = postService.updatePost(userId, id, dto);

        return ResponseEntity.ok(updated);
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @PathVariable Long id) {
        String userId = userDetails.getUserId();
        postService.deletePost(userId, id);

        return ResponseEntity.noContent().build();
    }

    // 게시글 검색
    @GetMapping("/posts/search")
    public PagedModel<PostResponse> searchPosts(
            @RequestParam PostSearchType type,
            @RequestParam @NotBlank @Size(max = 100) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {

        return postService.searchPosts(type, keyword, page);
    }

    // 마이페이지 본인 게시글 조회
    @GetMapping("/posts/me")
    public PagedModel<PostResponse> getMyPosts(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @RequestParam(defaultValue = "0") @Min(0) int page)
    {
        String userId = userDetails.getUsername();
        return postService.getMyPosts(userId, page);
    }

}
