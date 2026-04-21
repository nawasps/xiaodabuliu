package com.campus.book.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class MessageDTO {
    private Long id;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String images;

    private Long toUserId;
}
