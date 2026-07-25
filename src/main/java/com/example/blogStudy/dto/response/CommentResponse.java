package com.example.blogStudy.dto.response;

import com.example.blogStudy.entity.Comment;
import com.example.blogStudy.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
    private String user_id;
    private String name;
    private Role role;
    private Long post_id;

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
          comment.getId(),
          comment.getContent(),
          comment.getCreatedAt(),
          comment.getUpdatedAt(),
          comment.getUser().getId(),
          comment.getUser().getName(),
          comment.getUser().getRole(),
          comment.getPost().getId()
        );
    }
}
