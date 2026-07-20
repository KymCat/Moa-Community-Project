package com.example.blogStudy.entity;

import com.example.blogStudy.dto.update.CommentUpdate;
import com.example.blogStudy.entity.base.BaseTimeEntity;
import com.example.blogStudy.exception.CustomException;
import com.example.blogStudy.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comment")
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Lob
    @Column(nullable = false, length = 6000)
    private String content;


    // FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Builder
    public Comment(String content, User user, Post post) {
        this.content = content;
        this.user = user;
        this.post = post;
    }

    public void update(CommentUpdate dto) {
        this.content = dto.getContent();
    }

    public void validateOwner(String userId, Role userRole) {
        boolean isAdmin = userRole == Role.ADMIN;
        boolean isOwner = this.user.getId().equals(userId);

        if (!isAdmin && !isOwner)
            throw new CustomException(ErrorCode.COMMENT_FORBIDDEN);
    }
}
