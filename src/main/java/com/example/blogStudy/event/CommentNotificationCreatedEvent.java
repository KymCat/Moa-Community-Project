package com.example.blogStudy.event;

public record CommentNotificationCreatedEvent(
        Long notificationId,
        String receiverId,
        Long postId,
        String commenterName,
        String commentContent
) { }
