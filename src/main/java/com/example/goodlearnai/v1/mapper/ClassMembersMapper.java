package com.example.goodlearnai.v1.mapper;

import com.example.goodlearnai.v1.entity.ClassMembers;
import com.example.goodlearnai.v1.vo.ClassMemberVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 班级成员表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-09-17
 */
public interface ClassMembersMapper extends BaseMapper<ClassMembers> {

    /**
     * 使用JOIN查询班级成员及用户信息，避免N+1问题
     * @param classId 班级ID
     * @return 班级成员VO列表
     */
    List<ClassMemberVO> getClassMembersWithUserInfo(@Param("classId") Long classId);

}
