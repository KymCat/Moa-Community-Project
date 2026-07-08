package com.example.blogStudy.service;

import com.example.blogStudy.entity.Like;
import com.example.blogStudy.entity.Post;
import com.example.blogStudy.entity.User;
import com.example.blogStudy.exception.CustomException;
import com.example.blogStudy.exception.ErrorCode;
import com.example.blogStudy.repository.LikeRepository;
import com.example.blogStudy.repository.PostRepository;
import com.example.blogStudy.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;


    // 해당 게시글 좋아요 여부
    public Boolean likes(String userId, Long postId) {
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }

    // 게시글 좋아요
    @Transactional
    public void likePost(String userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post =  postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 중복 처리
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new CustomException(ErrorCode.DUPLICATE_LIKE);
        }

        try {
            Like like = Like.create(user, post);
            likeRepository.save(like);

        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent duplicate like request. userId={}, postId={}", userId, postId);
            throw new CustomException(ErrorCode.DUPLICATE_LIKE);
        }
    }

}
