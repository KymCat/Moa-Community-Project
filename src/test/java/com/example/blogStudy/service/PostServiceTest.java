package com.example.blogStudy.service;

import com.example.blogStudy.dto.create.PostCreate;
import com.example.blogStudy.dto.response.PostDetailResponse;
import com.example.blogStudy.dto.response.PostResponse;
import com.example.blogStudy.entity.Post;
import com.example.blogStudy.entity.User;
import com.example.blogStudy.exception.CustomException;
import com.example.blogStudy.exception.ErrorCode;
import com.example.blogStudy.repository.LikeRepository;
import com.example.blogStudy.repository.PostRepository;
import com.example.blogStudy.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PagedModel;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    // 1. 필드 (Mock, 상수)
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock LikeRepository likeRepository;

    @InjectMocks
    private PostService postService;

    private static final String USER_ID = "test1user";
    private static final String PASSWORD = "password1";
    private static final String NICKNAME = "name1";
    private static final long ID = 1;
    private static final String TITLE = "title";
    private static final String CONTENT = "content";

    // 2. 테스트용 객체 생성 메서드
    private Pageable defaultPageable() {
        return PageRequest.of(
                0, 5, Sort.by("createdAt").descending());
    }
    private User defaultUser() {
        return User.create(USER_ID, PASSWORD, NICKNAME);
    }
    private Post defaultPost() {
        return Post.create(TITLE, CONTENT, defaultUser());
    }


    // 3. 테스트 코드
    @Test
    @DisplayName("게시글 전체 조회 성공")
    void get_posts_success() {
        // given
        Pageable pageable = defaultPageable();
        Post post = defaultPost();
        Page<Post> posts = new PageImpl<>(List.of(post));

        given(postRepository.findAllWithUser(pageable)).willReturn(posts);

        // when
        PagedModel<PostResponse> result =  postService.getPosts(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0))
                .usingRecursiveComparison()
                .isEqualTo(PostResponse.from(post));
    }

    @Test
    @DisplayName("게시글 단일 조회 성공")
    void get_post_success() {
        // given
        Post post = defaultPost();
        int likeCount = 0;
        PostDetailResponse expected = PostDetailResponse.from(post, likeCount);

        given(postRepository.findById(ID)).willReturn(Optional.of(post));
        given(likeRepository.countByPostId(ID)).willReturn(likeCount);

        // when
        PostDetailResponse result = postService.getPost(ID);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("게시글 단일 조회 실패 - 존재하지 않는 ID")
    void get_post_fail_id_not_found() {
        // given
        given(postRepository.findById(ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPost(ID))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("게시글 작성 성공")
    void create_post_success() {
        // given
        PostCreate dto = new PostCreate(TITLE, CONTENT);
        User user = defaultUser();
        Post saved = defaultPost();
        PostResponse expected = PostResponse.from(saved);

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(postRepository.save(any(Post.class))).willReturn(saved);

        // when
        PostResponse result = postService.createPost(USER_ID, dto);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("게시글 작성 실패 - 존재하지 않는 userId")
    void create_post_fail_not_found_user_id() {
        // given
        PostCreate dto = new PostCreate(TITLE, CONTENT);

        given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.createPost(USER_ID, dto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }
//
//    @Test
//    void updatePost() {
//    }
//
//    @Test
//    void deletePost() {
//    }
}