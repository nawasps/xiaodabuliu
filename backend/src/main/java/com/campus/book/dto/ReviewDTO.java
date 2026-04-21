package com.campus.book.dto;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class ReviewDTO {
    private Long id;

    private Long bookId;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String images;

    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer rating;
}
