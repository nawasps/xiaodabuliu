package com.campus.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.book.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
