package com.campus.book.vo;

import lombok.Data;
import java.util.List;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private Integer sort;
    private String status;
    private List<CategoryVO> children;
}
