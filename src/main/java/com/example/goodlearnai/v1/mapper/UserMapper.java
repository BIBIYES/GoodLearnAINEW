package com.example.goodlearnai.v1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.goodlearnai.v1.entity.Users;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author Mouse
 * @since 2025-04-01
 */
@Mapper
public interface UserMapper extends BaseMapper<Users> {
} 