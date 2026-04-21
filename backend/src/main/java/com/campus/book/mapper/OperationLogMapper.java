package com.campus.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.book.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
