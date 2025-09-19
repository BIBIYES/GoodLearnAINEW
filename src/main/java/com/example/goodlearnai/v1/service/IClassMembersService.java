package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.entity.ClassMembers;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.vo.ClassMemberVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 班级成员表 服务类
 * </p>
 *
 * @author author
 * @since 2025-09-17
 */
public interface IClassMembersService extends IService<ClassMembers> {

    /**
     * 学生加入班级
     * @param classMembers 班级成员对象
     * @return 加入结果
     */
    Result<String> intoClass(ClassMembers classMembers);

    /**
     * 获取班级成员列表
     * @param classId 班级ID
     * @return 班级成员列表（包含学生基本信息）
     */
    Result<List<ClassMemberVO>> getClassMembers(Long classId);

    /**
     * 班级成员退出班级
     * @param userId 用户ID
     * @param classId 班级ID
     * @return 退出结果
     */
    Result<String> exitClass(Long userId, Long classId);

    /**
     * 查询当前用户加入的班级列表
     * @return 当前用户加入的班级列表（包含班级基本信息）
     */
    Result<List<ClassMemberVO>> getMyClasses();

}
