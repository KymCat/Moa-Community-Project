package com.example.blogStudy.service;

import com.example.blogStudy.entity.PushSubscription;
import com.example.blogStudy.event.CommentNotificationCreatedEvent;
import com.example.blogStudy.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushService {

    private static final int MAX_CONTENT_LENGTH = 120;

    private final PushService pushService;
    private final ObjectMapper objectMapper;
    private final PushSubscriptionRepository pushSubscriptionRepository;

    public void sendCommentNotification(
            CommentNotificationCreatedEvent event
    ) {
        List<PushSubscription> subscriptions =
                pushSubscriptionRepository.findAllByUserId(
                        event.receiverId()
                );

        if (subscriptions.isEmpty()) {
            return;
        }

        String payload = createPayload(event);

        if (payload == null) {
            return;
        }

        for (PushSubscription subscription : subscriptions) {
            send(subscription, payload);
        }
    }

    private String createPayload(
            CommentNotificationCreatedEvent event
    ) {
        String content = summarize(event.commentContent());

        WebPushPayload payload = new WebPushPayload(
                "새 댓글이 달렸습니다.",
                event.commenterName() + ": " + content,
                "/posts/" + event.postId(),
                event.notificationId()
        );

        try {
            return objectMapper.writeValueAsString(payload);

        } catch (JacksonException exception) {
            log.error(
                    "Web Push payload 생성 실패, notificationId={}",
                    event.notificationId(),
                    exception
            );

            return null;
        }
    }

    private void send(
            PushSubscription subscription,
            String payload
    ) {
        try {
            Notification notification = new Notification(
                    subscription.getEndpoint(),
                    subscription.getP256dh(),
                    subscription.getAuth(),
                    payload
            );

            HttpResponse response = pushService.send(
                    notification,
                    Encoding.AES128GCM
            );

            int statusCode =
                    response.getStatusLine().getStatusCode();

            handleResponse(subscription, statusCode);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            log.warn(
                    "Web Push 전송 Thread 중단, subscriptionId={}",
                    subscription.getId()
            );

        } catch (Exception exception) {
            log.error(
                    "Web Push 전송 실패, subscriptionId={}",
                    subscription.getId(),
                    exception
            );
        }
    }

    private void handleResponse(
            PushSubscription subscription,
            int statusCode
    ) {
        if (statusCode >= 200 && statusCode < 300) {
            log.debug(
                    "Web Push 전송 성공, subscriptionId={}",
                    subscription.getId()
            );
            return;
        }

        if (statusCode == 404 || statusCode == 410) {
            pushSubscriptionRepository.deleteById(
                    subscription.getId()
            );

            log.info(
                    "만료된 Push 구독 삭제, subscriptionId={}, status={}",
                    subscription.getId(),
                    statusCode
            );
            return;
        }

        log.warn(
                "Web Push Service 응답 실패, subscriptionId={}, status={}",
                subscription.getId(),
                statusCode
        );
    }

    private String summarize(String content) {
        if (content == null || content.isBlank()) {
            return "댓글 내용을 확인해 주세요.";
        }

        String normalized =
                content.replaceAll("\\s+", " ").trim();

        int codePointCount =
                normalized.codePointCount(0, normalized.length());

        if (codePointCount <= MAX_CONTENT_LENGTH) {
            return normalized;
        }

        int endIndex = normalized.offsetByCodePoints(
                0,
                MAX_CONTENT_LENGTH
        );

        return normalized.substring(0, endIndex) + "...";
    }

    private record WebPushPayload(
            String title,
            String body,
            String url,
            Long notificationId
    ) {
    }
}