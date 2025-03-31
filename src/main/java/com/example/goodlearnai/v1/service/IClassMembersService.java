package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ClassMembers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.goodlearnai.v1.entity.Classes;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Mouse
 * @since 2025-03-31
 */
public interface IClassMembersService extends IService<ClassMembers> {

    Result<String> intoClass(ClassMembers classMembers );
}
