package com.example.blogStudy.repository;

import com.example.blogStudy.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    int countByPostId(Long postId);

    boolean existsByUserIdAndPostId(String id, Long postId);
}
