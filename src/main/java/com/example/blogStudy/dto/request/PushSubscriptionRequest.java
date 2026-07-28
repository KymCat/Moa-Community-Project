package com.example.blogStudy.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class PushSubscriptionRequest {

    @NotBlank
    @Size(max = 2048)
    private String endpoint;

    @Valid
    @NotNull
    private Keys keys;

    @Getter
    @NoArgsConstructor
    public static class Keys {

        @NotBlank
        @Size(max = 128)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$")
        private String p256h;

        @NotBlank
        @Size(max = 64)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$")
        private String auth;
    }
}
