package com.campus.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.book.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}
