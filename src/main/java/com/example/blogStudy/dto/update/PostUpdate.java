package com.example.blogStudy.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostUpdate {

    @Size(max = 100, message = "제목은 100자를 초과해서 입력하실 수 없습니다.")
    private String title;

    @Size(max = 6000, message = "본문은 6000자를 초과해서 입력하실 수 없습니다.")
    private String content;

}
