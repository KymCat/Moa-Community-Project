package com.example.blogStudy.service;

import com.example.blogStudy.dto.response.NotificationResponse;
import com.example.blogStudy.dto.response.UnreadNotificationCountResponse;
import com.example.blogStudy.entity.Comment;
import com.example.blogStudy.entity.Notification;
import com.example.blogStudy.entity.User;
import com.example.blogStudy.exception.CustomException;
import com.example.blogStudy.exception.ErrorCode;
import com.example.blogStudy.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public PagedModel<NotificationResponse> getNotifications(
            String userId, int page)
    {
        Pageable pageable = PageRequest.of(
                page,
                5,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<Notification> pages =
                notificationRepository.findAllByReceiverId(userId, pageable);

        return new PagedModel<>(pages
                .map(NotificationResponse::from));
    }

    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse getUnreadCount(String userId) {
        long count = notificationRepository
                .countByReceiverIdAndReadAtIsNull(userId);

        return new UnreadNotificationCountResponse(count);
    }

    @Transactional
    public void markAsRead(Long id, String userId) {
        Notification notification = notificationRepository
                .findByIdAndReceiverId(id, userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsRead(userId, Instant.now());
    }

    @Transactional
    public void createCommentNotification(Comment comment) {
        User receiver = comment.getPost().getUser();
        User commenter = comment.getUser();

        // 자신이 작성한 게시글에 직접 댓글을 단 경우 알림을 만들지 않는다
        if (receiver.getId().equals(commenter.getId())) {
            return;
        }

        Notification notification =
                Notification.create(receiver, comment);

        notificationRepository.save(notification);
    }
}
