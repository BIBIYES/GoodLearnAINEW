package com.example.goodlearnai.v1.service.impl;

import com.example.goodlearnai.v1.common.Result;

import com.example.goodlearnai.v1.entity.CourseMembers;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.CourseMembersMapper;
import com.example.goodlearnai.v1.service.ICourseMembersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import com.example.goodlearnai.v1.vo.UserCoursesView;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2025-03-31
 */
@Service
@Slf4j
public class CourseMembersServiceImpl extends ServiceImpl<CourseMembersMapper, CourseMembers> implements ICourseMembersService {

    @Resource
    private
    CourseMembersMapper courseMembersMapper;
    @Override
    public Result<String> intoClass(CourseMembers courseMembers) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        if(!"student".equals(role)) {
            log.warn("用户暂无权限");
            return Result.error("暂无权限");
        }
        try {
            courseMembers.setUserId(userId);
            boolean flag = save(courseMembers);
            if (!flag) {
                return Result.error("加入班级失败");
            }
            return Result.success("加入班级成功");
        } catch (DuplicateKeyException e) {
            log.warn("用户尝试重复加入班级: userId={}, classId={}", userId, courseMembers.getCourseId());
            return Result.error("您已经在这个班级中，请勿重复加入");
        }
        catch (DataIntegrityViolationException e){
            log.warn("用户尝试加入班级但这个班级可能不存在: userId={}, classId={}", userId, courseMembers.getCourseId());
            return Result.error("该班级可能不存在");
        }
        catch (Exception e) {
            log.error("加入班级发生未知异常", e);
            throw new CustomException("加入班级未知异常");
        }
    }

    @Override
    public Result<List<UserCoursesView>> getStudentOwnCourses() {
        Long userId = AuthUtil.getCurrentUserId();

        // 从 Mapper 中获取数据
        List<UserCoursesView> list = courseMembersMapper.getStudentCourses(userId);

        if (list != null && !list.isEmpty()) {
            return Result.success("获取课程成功！",list);
        } else {
            return Result.error("暂无加入的课程");
        }
    }


}
