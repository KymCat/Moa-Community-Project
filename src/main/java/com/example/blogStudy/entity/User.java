package com.example.blogStudy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    @Id
    @Column(length = 30)
    private String id;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    public static User create(
            String id,
            String password,
            String name
    ) {
        return create(id, password, name, Role.USER);
    }

    public static User create(
            String id,
            String password,
            String name,
            Role role
    ) {
        User user = new User();
        user.id = id;
        user.password = password;
        user.name = name;
        user.role = role;
        return user;
    }

    public void updatePassword(String password) {
        if (password != null)
            this.password = password;
    }

    public void updateName(String name) {
        if (name != null)
            this.name = name;
    }
}
