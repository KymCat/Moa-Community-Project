package com.example.blogStudy.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostSearchType {
    TITLE,
    CONTENT,
    AUTHOR
}
