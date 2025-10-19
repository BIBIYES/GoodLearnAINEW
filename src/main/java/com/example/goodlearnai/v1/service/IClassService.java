package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.entity.Class;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.vo.ClassVO;
import com.example.goodlearnai.v1.vo.ClassDetailVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 课程下的班级表 服务类
 * </p>
 *
 * @author author
 * @since 2025-09-17
 */
public interface IClassService extends IService<Class> {

    /**
     * 创建班级
     * @param classEntity 班级实体
     * @return 创建结果
     */
    Result<String> createClass(Class classEntity);

    /**
     * 获取班级信息
     * @param classId 班级ID
     * @return 班级信息
     */
    Result<Class> getClassInfo(Long classId);

    /**
     * 获取所有班级信息
     * @return 所有班级信息列表（包含教师姓名）
     */
    Result<java.util.List<ClassVO>> getAllClasses();

    /**
     * 修改班级信息（班级名称和描述）
     * @param classEntity 班级实体
     * @return 修改结果
     */
    Result<String> updateClassMember(Class classEntity);

    /**
     * 删除班级（软删除）
     * @param classId 班级ID
     * @return 删除结果
     */
    Result<String> deleteClass(Long classId);

    /**
     * 获取班级详细信息（包含教师姓名、课程名、课程描述、课程创建时间）
     * @param classId 班级ID
     * @return 班级详细信息
     */
    Result<ClassDetailVO> getClassDetail(Long classId);

}
