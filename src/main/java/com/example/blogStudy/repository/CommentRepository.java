package com.example.blogStudy.repository;

import com.example.blogStudy.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

        @Query(
                value = "SELECT c FROM Comment c JOIN FETCH c.user",
                countQuery = "SELECT COUNT(c) FROM Comment c"
        )
        Page<Comment> findAllWithUser(Pageable pageable);

        @Query(
                value = "SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId",
                countQuery = """
                        SELECT COUNT(c) 
                        FROM Comment c
                        WHERE c.post.id = :postId                                                              
                        """
        )
        Page<Comment> findByPostId(@Param("postId") Long postId, Pageable pageable);

        @Query(
                value = "SELECT c FROM Comment c JOIN FETCH c.post WHERE c.user.id = :userId",
                countQuery = """
                        SELECT COUNT(c)
                        FROM Comment c
                        WHERE c.user.id = :userId
                        """
        )
        Page<Comment> findByUserId(@Param("userId") String userId, Pageable pageable);
}
