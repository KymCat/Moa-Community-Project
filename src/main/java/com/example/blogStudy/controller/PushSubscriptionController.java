package com.example.blogStudy.controller;

import com.example.blogStudy.dto.request.PushSubscriptionDeleteRequest;
import com.example.blogStudy.dto.request.PushSubscriptionRequest;
import com.example.blogStudy.dto.response.ApiResponse;
import com.example.blogStudy.dto.response.VapidPublicKeyResponse;
import com.example.blogStudy.security.CustomUserDetails;
import com.example.blogStudy.service.PushSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PushSubscriptionController {

    private final PushSubscriptionService pushSubscriptionService;

    @GetMapping("/public-key")
    public ApiResponse<VapidPublicKeyResponse> getPublicKey() {
        VapidPublicKeyResponse result =
                pushSubscriptionService.getPublicKey();

        return ApiResponse.success(result);
    }

    @PutMapping("/push-subscriptions")
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PushSubscriptionRequest request)
    {
        String userId = userDetails.getUserId();
        pushSubscriptionService.subscribe(userId, request);

        return ResponseEntity
                .noContent().build();
    }

    @DeleteMapping("/push-subscriptions")
    public ResponseEntity<Void> unsubscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PushSubscriptionDeleteRequest request)
    {
        String userId = userDetails.getUserId();
        String endpoint = request.getEndpoint();

        pushSubscriptionService.unsubscribe(userId, endpoint);
        return ResponseEntity
                .noContent().build();
    }

}
