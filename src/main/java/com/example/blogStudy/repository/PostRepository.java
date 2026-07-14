package com.example.blogStudy.repository;

import com.example.blogStudy.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostSearchRepository {

    // 게시글 전체 조회 N+1 fetch join 해결 방법
    @Query(
            value = "SELECT p FROM Post p JOIN FETCH p.user",
            countQuery = "SELECT COUNT(p) FROM Post p"
    )
    Page<Post> findAllWithUser(Pageable pageable);

    // 게시글 단건 조회 fetch join
    @EntityGraph(attributePaths = "user")
    Optional<Post> findById(Long id);

    // userId가 작성한 게시글 검색
    @Query(
            value = "SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id = :userId",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId"
    )
    Page<Post> findByUserId(@Param("userId") String userId, Pageable pageable);
}
