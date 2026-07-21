package com.example.blogStudy.dto.response;

import com.example.blogStudy.entity.Role;
import com.example.blogStudy.entity.User;

public record CurrentUserResponse(
        String id,
        String name,
        Role role
) {
    public static CurrentUserResponse from(User user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getName(),
                user.getRole()
        );
    }
}
