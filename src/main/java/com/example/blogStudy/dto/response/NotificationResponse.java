package com.example.blogStudy.dto.response;

import com.example.blogStudy.entity.Comment;
import com.example.blogStudy.entity.Notification;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long postId,
        Long commentId,
        String commenterName,
        String commentContent,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse from(Notification notification) {
        Comment comment = notification.getComment();

        return new NotificationResponse(
                notification.getId(),
                comment.getPost().getId(),
                comment.getId(),
                comment.getUser().getName(),
                comment.getContent(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
