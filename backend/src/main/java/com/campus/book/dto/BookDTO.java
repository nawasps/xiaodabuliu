package com.campus.book.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BookDTO {
    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private String coverImage;

    private List<String> images;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "新旧程度不能为空")
    private String condition;

    private List<String> tags;
}
