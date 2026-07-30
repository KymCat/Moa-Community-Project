package com.example.blogStudy.event;

import com.example.blogStudy.service.WebPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CommentNotificationEventListener {
    private final WebPushService webPushService;

    @Async("webPushExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CommentNotificationCreatedEvent event) {
        webPushService.sendCommentNotification(event);
    }
}
