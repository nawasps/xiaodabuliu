package com.campus.book.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CartDTO {
    @NotNull(message = "商品ID不能为空")
    private Long bookId;

    private Integer quantity = 1;
}
