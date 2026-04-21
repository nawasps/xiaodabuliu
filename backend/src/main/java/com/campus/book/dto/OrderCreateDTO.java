package com.campus.book.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OrderCreateDTO {
    @NotNull(message = "商品ID列表不能为空")
    private List<Long> bookIds;

    private Long addressId;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;
}
