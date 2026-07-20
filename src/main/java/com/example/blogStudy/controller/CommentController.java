package com.example.blogStudy.controller;

import com.example.blogStudy.dto.create.CommentCreate;
import com.example.blogStudy.dto.response.ApiResponse;
import com.example.blogStudy.dto.response.CommentResponse;
import com.example.blogStudy.dto.update.CommentUpdate;
import com.example.blogStudy.entity.Role;
import com.example.blogStudy.security.CustomUserDetails;
import com.example.blogStudy.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
public class CommentController {

    private final CommentService commentService;

    // id 해당 게시글 댓글 조회
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<PagedModel<CommentResponse>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page)
    {
        PagedModel<CommentResponse> comments = commentService.getComments(postId, page);

        return ApiResponse.success(comments);
    }


    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreate dto)
    {

        String userId = userDetails.getUserId();
        CommentResponse created = commentService.createComment(userId, postId, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }


    // 댓글 수정
    @PatchMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CommentUpdate dto)
    {
        String userId = userDetails.getUserId();
        Role userRole = userDetails.getUserRole();
        CommentResponse updated = commentService.updateComment(userId, userRole, id, dto);

        return ResponseEntity.ok(ApiResponse.success(updated));
    }


    // 댓글 삭제
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id)
    {

        String userId = userDetails.getUserId();
        Role userRole = userDetails.getUserRole();
        commentService.deleteComment(userId, userRole, id);

        return ResponseEntity.noContent().build();
    }

    // 마이페이지 본인 댓글 조회
    @GetMapping("/comments/me")
    public ApiResponse<PagedModel<CommentResponse>> getMyComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") @Min(0) int page)
    {
        String userId = userDetails.getUsername();
        PagedModel<CommentResponse> comments = commentService.getMyComments(userId, page);

        return ApiResponse.success(comments);
    }
}
