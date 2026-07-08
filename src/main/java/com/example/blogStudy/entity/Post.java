package com.example.blogStudy.entity;

import com.example.blogStudy.dto.update.PostUpdate;
import com.example.blogStudy.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post")
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(nullable = false, length = 6000)
    private String content;


    // FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static Post create(String title, String content, User user) {
        Post post = new Post();
        post.title = title;
        post.content = content;
        post.user = user;
        return post;
    }

    public void update(PostUpdate dto) {
        if (dto.getContent() != null) {
            this.content = dto.getContent();
        }

        if (dto.getTitle() != null) {
            this.title = dto.getTitle();
        }
    }
}

