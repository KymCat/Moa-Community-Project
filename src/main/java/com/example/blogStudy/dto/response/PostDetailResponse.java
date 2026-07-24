package com.example.blogStudy.dto.response;

import com.example.blogStudy.entity.Post;
import com.example.blogStudy.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userId;
    private String name;
    private Role role;

    private long commentCount;
    private long likeCount;

    public static PostDetailResponse from(Post post, long commentCount, long likeCount) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getUser().getId(),
                post.getUser().getName(),
                post.getUser().getRole(),
                commentCount,
                likeCount
        );
    }
}
