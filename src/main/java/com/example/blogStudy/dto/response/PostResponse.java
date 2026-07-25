package com.example.blogStudy.dto.response;

import com.example.blogStudy.entity.Post;
import com.example.blogStudy.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
    private String userId;
    private String name;
    private Role role;

    // Entity -> DTO
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getUser().getId(), // 엔티티 에서 필요한 정보만
                post.getUser().getName(),
                post.getUser().getRole()
        );
    }

}

// 게시글 단건 및 리스트 조회 API
// User 객체는 넣지 않고 필요한 값만 꺼냄