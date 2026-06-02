package com.example.blogStudy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 6, max = 30, message = "아이디는 6자 이상 30자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)[a-z0-9]+$", message = "아이디는 영어 소문자와 숫자 조합만 가능합니다.")
    private final String id;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 15, message = "비밀번호는 8자 이상 15자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d!@#$%^&*()_+=-]+$",
            message = "비밀번호는 공백 없이 영문자와 숫자를 각각 1개 이상 포함해야 합니다."
    )
    private final String password;
}
