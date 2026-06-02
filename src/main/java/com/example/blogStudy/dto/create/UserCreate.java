package com.example.blogStudy.dto.create;

import com.example.blogStudy.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserCreate {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 6, max = 30, message = "아이디는 6자 이상 30자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)[a-z0-9]+$", message = "아이디는 영어 소문자와 숫자 조합만 가능합니다.")
    private String id;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 15, message = "비밀번호는 8자 이상 15자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d!@#$%^&*()_+=-]+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다."
    )
    private String password;


    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하로 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임은 한글, 영어 소문자, 숫자만 가능합니다.")
    private String name;

    public User toEntity(String password) {
        return User.create(this.id, password, this.name);
    }
}
