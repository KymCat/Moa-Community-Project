package com.example.blogStudy.service;

import com.example.blogStudy.dto.create.PostCreate;
import com.example.blogStudy.dto.request.PostSearchType;
import com.example.blogStudy.dto.response.PostDetailResponse;
import com.example.blogStudy.dto.response.PostResponse;
import com.example.blogStudy.dto.update.PostUpdate;
import com.example.blogStudy.entity.Post;
import com.example.blogStudy.entity.PostListMode;
import com.example.blogStudy.entity.Role;
import com.example.blogStudy.entity.User;
import com.example.blogStudy.exception.CustomException;
import com.example.blogStudy.exception.ErrorCode;
import com.example.blogStudy.repository.CommentRepository;
import com.example.blogStudy.repository.LikeRepository;
import com.example.blogStudy.repository.PostRepository;
import com.example.blogStudy.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;


    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public PagedModel<PostDetailResponse> getPosts(int page, PostListMode mode) {
        Pageable pageable;
        Page<Post> pages;

        if (mode == PostListMode.RECOMMEND) {
            pageable = PageRequest.of(page, 5);
            pages = postRepository.findRecommendPost(pageable);
        }
        else {
            pageable = PageRequest.of(page, 5,
                    Sort.by(Sort.Order.desc("createdAt")));
            pages = postRepository.findAllWithUser(pageable);
        }

        Page<PostDetailResponse> result =
                pages.map(post -> {
                    long commentCount = commentRepository.countByPostId(post.getId());
                    long likeCount = likeRepository.countByPostId(post.getId());

                    return PostDetailResponse.from(post, commentCount, likeCount);
                });

        return new PagedModel<>(result);
    }

    // 게시글 단일 조회
    @Transactional(readOnly = true)
    public PostDetailResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 좋아요 갯수 가져오기
        long countCount = commentRepository.countByPostId(id);
        long likeCount = likeRepository.countByPostId(id);

        return PostDetailResponse.from(post, countCount, likeCount);
    }

    // 게시글 작성
    @Transactional
    public PostResponse createPost(String userId, PostCreate dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post saved = postRepository.save(dto.toEntity(user));    // 실제 서비스에는 진짜 session 이 들어감
        return  PostResponse.from(saved);
    }

    // 게시글 수정
    @Transactional
    public PostResponse updatePost(String userId, Role userRole, Long id, PostUpdate dto) {
        validatePostUpdate(dto);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        String authorUserId = post.getUser().getId();
        checkPostOwner(userId, userRole, authorUserId);

        post.update(dto);
        return PostResponse.from(post);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(String userId, Role userRole, Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        String authorUserId = post.getUser().getId();
        checkPostOwner(userId, userRole, authorUserId);

        postRepository.delete(post);
    }

    /*
        필드 전체 Null
        필드 한쪽 공백,빈 문자열 검증 함수
     */
    private void validatePostUpdate(PostUpdate dto) {
        if (dto.getTitle() == null && dto.getContent() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (dto.getTitle() != null && dto.getTitle().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (dto.getContent() != null && dto.getContent().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // 게시글 검색
    @Transactional(readOnly = true)
    public PagedModel<PostResponse> searchPosts(
            PostSearchType type,
            String keyword,
            int page
    ) {

        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<Post> posts = postRepository.searchPosts(type, keyword, pageable);
        Page<PostResponse> result = posts.map(PostResponse::from);

        return new PagedModel<>(result);
    }

    // 마이페이지 본인 게시글 조회
    @Transactional(readOnly = true)
    public PagedModel<PostResponse> getMyPosts(String userId, int page) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<Post> posts = postRepository.findByUserId(userId, pageable);
        Page<PostResponse> result = posts.map(PostResponse::from);

        return new PagedModel<>(result);
    }

    private void checkPostOwner(
            String userId,
            Role userRole,
            String authorUserId)
    {
        boolean isAdmin = userRole == Role.ADMIN;
        boolean isOwner = userId.equals(authorUserId);

        if (!isAdmin && !isOwner) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }
    }
}
