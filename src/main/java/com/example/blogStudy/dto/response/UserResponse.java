package com.example.blogStudy.dto.response;

import com.example.blogStudy.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String name;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName()
        );
    }
}
