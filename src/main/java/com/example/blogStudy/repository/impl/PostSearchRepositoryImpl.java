package com.example.blogStudy.repository.impl;

import com.example.blogStudy.dto.request.PostSearchType;
import com.example.blogStudy.entity.Post;
import com.example.blogStudy.entity.QPost;
import com.example.blogStudy.entity.QUser;
import com.example.blogStudy.repository.PostSearchRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@AllArgsConstructor
public class PostSearchRepositoryImpl implements PostSearchRepository {
    private final JPAQueryFactory queryFactory;

    private final QPost post = QPost.post;
    private final QUser user = QUser.user;

    // 게시글 검색
    @Override
    public Page<Post> searchPosts(
            PostSearchType type,
            String keyword,
            Pageable pageable)
    {
        BooleanExpression condition = searchCondition(type, keyword);

        // queryFactory를 이용한 조회 및 Pagination 처리
        List<Post> content = queryFactory
                .selectFrom(post)
                .join(user, post.user).fetchJoin()
                .where(condition)
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.count())
                .from(post)
                .join(user, post.user)
                .where(condition)
                .fetchOne();

        long count = total != null ? total : 0L;
        return new PageImpl<>(content, pageable, count);
    }

    // where 조건문
    private BooleanExpression searchCondition(PostSearchType type, String keyword) {
        String pattern = "%" + escapeLikeKeyword(keyword) + "%";

        return switch (type) {
            case TITLE -> post.title.like(pattern, '!');
            case CONTENT -> post.content.like(pattern, '!');
            case AUTHOR -> user.name.like(pattern, '!');
        };

    }

    private String escapeLikeKeyword(String keyword) {
        return keyword
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }


}
