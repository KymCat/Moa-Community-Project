package com.example.blogStudy.repository;

import com.example.blogStudy.dto.request.PostSearchType;
import com.example.blogStudy.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostSearchRepository {
    public Page<Post> searchPosts(
            PostSearchType type,
            String keyword,
            Pageable pageable
    );
}
