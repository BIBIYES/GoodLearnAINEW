package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ClassMembers;
import com.example.goodlearnai.v1.entity.Classes;
import com.example.goodlearnai.v1.service.IClassMembersService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 * <p>
 * 班级成员的控制
 *
 * @author Mouse
 * @since 2025 -03-31
 */
@RestController
@RequestMapping("/v1/class-members")
public class ClassMembersController {
    @Resource
    private IClassMembersService iclassMembersService;

    /**
     * 学生加入班级
     *
     * @param classMembers 班级成员对象
     * @return 返回加入成功或者失败
     */
    @PostMapping("/into-classes")
    public Result<String> intoClass(@RequestBody ClassMembers classMembers) {
        return iclassMembersService.intoClass(classMembers);
    }
}
