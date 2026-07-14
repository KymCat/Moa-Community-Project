package com.example.blogStudy.service;

import com.example.blogStudy.dto.create.CommentCreate;
import com.example.blogStudy.dto.response.CommentResponse;
import com.example.blogStudy.dto.update.CommentUpdate;
import com.example.blogStudy.entity.Comment;
import com.example.blogStudy.entity.Post;
import com.example.blogStudy.entity.User;
import com.example.blogStudy.exception.CustomException;
import com.example.blogStudy.exception.ErrorCode;
import com.example.blogStudy.repository.CommentRepository;
import com.example.blogStudy.repository.PostRepository;
import com.example.blogStudy.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;


    // id 해당 게시글 댓글 조회
    @Transactional(readOnly = true)
    public PagedModel<CommentResponse> getComments(Long id, int page) {
        Pageable pageable = PageRequest.of(
                page,
                3,
                Sort.by("createdAt").descending());

        // Page => PagedModel : Page 타입보다 안정적인 구조인 PagedModel 반환 권장
        return new PagedModel<>(
                commentRepository.findByPostId(id,pageable)
                        .map(CommentResponse::from));

    }


    // 댓글 작성
    @Transactional
    public CommentResponse createComment(String userId, Long postId, CommentCreate dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Comment comment = dto.toEntity(user, post);
        commentRepository.save(comment);
        return CommentResponse.from(comment);
    }


    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(String userId, Long id, CommentUpdate dto) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        comment.validateOwner(userId); // 권한 확인
        comment.update(dto);
        return CommentResponse.from(comment);
    }


    // 댓글 삭제
    @Transactional
    public void deleteComment(String userId, Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        comment.validateOwner(userId);
        commentRepository.delete(comment);
    }

    // 마이페이지 본인 댓글 조회
    @Transactional(readOnly = true)
    public PagedModel<CommentResponse> getMyComments(String userId, int page) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(
                        Sort.Order.desc("createAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<Comment> comments = commentRepository.findByUserId(userId, pageable);
        Page<CommentResponse> result = comments.map(CommentResponse::from);

        return new PagedModel<>(result);
    }
}
