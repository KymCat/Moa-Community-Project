package com.example.blogStudy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PushSubscriptionDeleteRequest {

    @NotBlank
    @Size(max = 2048)
    private String endpoint;
}
