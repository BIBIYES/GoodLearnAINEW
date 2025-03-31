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
 *  前端控制器
 * </p>
 *
 * @author Mouse
 * @since 2025-03-31
 */
@RestController
@RequestMapping("/v1/class-members")
public class ClassMembersController {
    @Resource
    private IClassMembersService iclassMembersService;
    @PostMapping("/into-classes")
    public Result<String> intoClass(@RequestBody ClassMembers classMembers) {
        return iclassMembersService.intoClass(classMembers);
    }
}
