package com.example.blogStudy.controller;

import com.example.blogStudy.dto.response.ApiResponse;
import com.example.blogStudy.dto.response.NotificationResponse;
import com.example.blogStudy.dto.response.UnreadNotificationCountResponse;
import com.example.blogStudy.security.CustomUserDetails;
import com.example.blogStudy.service.NotificationService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    // 읽지 않은 모든 알림 가져오기
    @GetMapping
    public ApiResponse<PagedModel<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") @Min(0) int page)
    {
        String userId = userDetails.getUserId();
        PagedModel<NotificationResponse> result =
                notificationService.getNotifications(userId, page);

        return ApiResponse.success(result);
    }

    // 읽지 않은 알림 갯수 가져오기
    @GetMapping("/unread-count")
    public ApiResponse<UnreadNotificationCountResponse> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        String userId = userDetails.getUserId();
        UnreadNotificationCountResponse result =
                notificationService.getUnreadCount(userId);

        return ApiResponse.success(result);
    }

    // 알림 하나 읽음 표시
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        String userId = userDetails.getUserId();
        notificationService.markAsRead(id, userId);

        return ResponseEntity
                .noContent().build();
    }

    // 알림 전체 읽음 표시
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        String userId = userDetails.getUserId();
        notificationService.markAllAsRead(userId);

        return ResponseEntity
                .noContent().build();
    }
}
