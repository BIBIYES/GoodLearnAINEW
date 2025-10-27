package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.ClassJoinRequest;
import com.example.goodlearnai.v1.entity.Class;
import com.example.goodlearnai.v1.entity.ClassMembers;
import com.example.goodlearnai.v1.entity.Course;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.mapper.ClassMapper;
import com.example.goodlearnai.v1.mapper.ClassMembersMapper;
import com.example.goodlearnai.v1.mapper.CourseMapper;
import com.example.goodlearnai.v1.mapper.UserMapper;
import com.example.goodlearnai.v1.service.IClassMembersService;
import com.example.goodlearnai.v1.utils.AuthUtil;
import com.example.goodlearnai.v1.vo.ClassMemberVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 班级成员服务实现
 */
@Service
@Slf4j
public class ClassMembersServiceImpl extends ServiceImpl<ClassMembersMapper, ClassMembers>
        implements IClassMembersService {

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public Result<String> intoClass(ClassJoinRequest request) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"student".equals(role)) {
            log.warn("用户暂无权限加入班级: userId={}", userId);
            return Result.error("暂无权限加入班级");
        }

        String joinCode = request.getJoinCode() == null ? null : request.getJoinCode().trim().toUpperCase();
        if (joinCode == null || joinCode.isEmpty()) {
            log.warn("加入码不能为空: userId={}", userId);
            return Result.error("请输入班级加入码");
        }

        Class classEntity = null;
        if (request.getClassId() != null) {
            classEntity = classMapper.selectById(request.getClassId());
        }
        if (classEntity == null) {
            QueryWrapper<Class> wrapper = new QueryWrapper<>();
            wrapper.eq("join_code", joinCode);
            classEntity = classMapper.selectOne(wrapper);
        }

        if (classEntity == null) {
            log.warn("根据加入码未找到班级: joinCode={}", joinCode);
            return Result.error("班级加入码错误");
        }

        if (!Boolean.TRUE.equals(classEntity.getStatus())) {
            log.warn("班级已停用: classId={}", classEntity.getClassId());
            return Result.error("该班级已停用，请联系教师");
        }

        if (!Boolean.TRUE.equals(classEntity.getAllowJoin())) {
            log.warn("班级已关闭加入: classId={}", classEntity.getClassId());
            return Result.error("该班级暂不允许加入，请联系教师");
        }

        if (classEntity.getJoinCode() == null
                || !joinCode.equalsIgnoreCase(classEntity.getJoinCode().trim())) {
            log.warn("加入码不匹配: userId={}, classId={}, inputCode={}",
                    userId, classEntity.getClassId(), joinCode);
            return Result.error("班级加入码错误");
        }

        QueryWrapper<ClassMembers> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_id", classEntity.getClassId())
                .eq("user_id", userId)
                .eq("status", true);

        ClassMembers existingMember = getOne(queryWrapper);
        if (existingMember != null) {
            log.info("学生已在班级中: userId={}, classId={}", userId, classEntity.getClassId());
            return Result.error("您已经在该班级中");
        }

        try {
            // 查找之前是否有退出班级的记录
            LambdaUpdateWrapper<ClassMembers> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ClassMembers::getClassId, classEntity.getClassId())
                    .eq(ClassMembers::getUserId, userId)
                    .eq(ClassMembers::getStatus, false)
                    .set(ClassMembers::getStatus, true)
                    .set(ClassMembers::getJoinTime, LocalDateTime.now());

            boolean update = update(updateWrapper);

            if (update) {
                log.info("学生重新加入班级成功: userId={}, classId={}", userId, classEntity.getClassId());
                return Result.success("加入班级成功");
            }

            // 如果没有找到需要更新的记录，则插入新记录
            ClassMembers classMember = new ClassMembers();
            classMember.setClassId(classEntity.getClassId());
            classMember.setUserId(userId);
            classMember.setJoinTime(LocalDateTime.now());
            classMember.setStatus(true);

            if (save(classMember)) {
                log.info("学生加入班级成功: userId={}, classId={}", userId, classEntity.getClassId());
                return Result.success("加入班级成功");
            }

            log.error("学生加入班级失败: userId={}, classId={}", userId, classEntity.getClassId());
            return Result.error("加入班级失败");
        } catch (Exception e) {
            log.error("学生加入班级失败: userId={}, classId={}, error={}",
                    userId, classEntity.getClassId(), e.getMessage());
            return Result.error("加入班级失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<ClassMemberVO>> getClassMembers(Long classId) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        Class classEntity = classMapper.selectById(classId);
        if (classEntity == null) {
            log.warn("班级不存在: classId={}", classId);
            return Result.error("班级不存在");
        }

        if ("teacher".equals(role)) {
            if (!userId.equals(classEntity.getTeacherId())) {
                log.warn("教师无权限查看班级成员: userId={}, classId={}", userId, classId);
                return Result.error("您不是该班级的负责教师，无法查看成员列表");
            }
        } else if ("student".equals(role)) {
            QueryWrapper<ClassMembers> memberWrapper = new QueryWrapper<>();
            memberWrapper.eq("class_id", classId)
                    .eq("user_id", userId)
                    .eq("status", true);
            ClassMembers studentMember = getOne(memberWrapper);
            if (studentMember == null) {
                log.warn("学生不在班级中: userId={}, classId={}", userId, classId);
                return Result.error("您不是该班级的成员，无法查看成员列表");
            }
        } else {
            log.warn("用户角色无权限查看班级成员: userId={}, role={}", userId, role);
            return Result.error("暂无权限查看班级成员");
        }

        try {
            QueryWrapper<ClassMembers> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("class_id", classId)
                    .eq("status", true)
                    .orderByAsc("join_time");

            List<ClassMembers> membersList = list(queryWrapper);
            if (membersList.isEmpty()) {
                log.info("班级暂无成员: classId={}", classId);
                return Result.success("班级暂无成员", Collections.emptyList());
            }

            Users teacher = userMapper.selectById(classEntity.getTeacherId());

            List<ClassMemberVO> memberVOList = membersList.stream()
                    .map(member -> buildMemberVO(member, classEntity, teacher))
                    .collect(Collectors.toList());

            log.info("获取班级成员列表成功: classId={}, memberCount={}", classId, memberVOList.size());
            return Result.success("获取班级成员列表成功", memberVOList);
        } catch (Exception e) {
            log.error("获取班级成员列表失败: classId={}, error={}", classId, e.getMessage());
            return Result.error("获取班级成员列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> exitClass(Long userId, Long classId) {
        Long currentUserId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if ("student".equals(role)) {
            if (!currentUserId.equals(userId)) {
                log.warn("学生无权限操作其他用户: currentUserId={}, targetUserId={}", currentUserId, userId);
                return Result.error("您只能退出自己加入的班级");
            }
        } else if ("teacher".equals(role)) {
            Class classEntity = classMapper.selectById(classId);
            if (classEntity == null) {
                log.warn("班级不存在: classId={}", classId);
                return Result.error("班级不存在");
            }
            if (!currentUserId.equals(classEntity.getTeacherId())) {
                log.warn("教师无权限移除班级成员: teacherId={}, classId={}", currentUserId, classId);
                return Result.error("您不是该班级的负责教师，无法移除班级成员");
            }
        } else {
            log.warn("用户角色无权限操作班级成员: userId={}, role={}", currentUserId, role);
            return Result.error("暂无权限操作班级成员");
        }

        try {
            QueryWrapper<ClassMembers> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("class_id", classId)
                    .eq("user_id", userId)
                    .eq("status", true);

            ClassMembers classMember = getOne(queryWrapper);
            if (classMember == null) {
                log.warn("用户不在班级中或已退出: userId={}, classId={}", userId, classId);
                return Result.error("用户不在该班级中或已退出");
            }

            // 硬删除：直接从数据库删除记录
            if (removeById(classMember.getId())) {
                String action = "student".equals(role) ? "退出班级" : "移除班级成员";
                log.info("{}成功: operatorId={}, userId={}, classId={}", action, currentUserId, userId, classId);
                return Result.success(action + "成功");
            }

            String action = "student".equals(role) ? "退出班级" : "移除班级成员";
            log.error("{}失败: operatorId={}, userId={}, classId={}", action, currentUserId, userId, classId);
            return Result.error(action + "失败");
        } catch (Exception e) {
            String action = "student".equals(role) ? "退出班级" : "移除班级成员";
            log.error("{}失败: operatorId={}, userId={}, classId={}, error={}",
                    action, currentUserId, userId, classId, e.getMessage());
            return Result.error(action + "失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<ClassMemberVO>> getMyClasses() {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"student".equals(role)) {
            log.warn("用户角色无权限查询加入的班级: userId={}, role={}", userId, role);
            return Result.error("暂无权限查询加入的班级");
        }

        try {
            QueryWrapper<ClassMembers> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .eq("status", true)
                    .orderByDesc("join_time");

            List<ClassMembers> membersList = list(queryWrapper);
            if (membersList.isEmpty()) {
                log.info("用户暂未加入任何班级: userId={}", userId);
                return Result.success("您暂未加入任何班级", Collections.emptyList());
            }

            List<ClassMemberVO> classVOList = membersList.stream()
                    .map(member -> {
                        Class classEntity = classMapper.selectById(member.getClassId());
                        Users teacher = null;
                        if (classEntity != null) {
                            teacher = userMapper.selectById(classEntity.getTeacherId());
                        }
                        return buildMemberVO(member, classEntity, teacher);
                    })
                    .collect(Collectors.toList());

            log.info("获取用户加入的班级列表成功: userId={}, classCount={}", userId, classVOList.size());
            return Result.success("获取加入的班级列表成功", classVOList);
        } catch (Exception e) {
            log.error("获取用户加入的班级列表失败: userId={}, error={}", userId, e.getMessage());
            return Result.error("获取加入的班级列表失败: " + e.getMessage());
        }
    }

    private ClassMemberVO buildMemberVO(ClassMembers member, Class classEntity, Users teacher) {
        ClassMemberVO memberVO = new ClassMemberVO();
        memberVO.setId(member.getId());
        memberVO.setClassId(member.getClassId());
        memberVO.setUserId(member.getUserId()); // 设置学生ID
        memberVO.setJoinTime(member.getJoinTime());
        memberVO.setStatus(member.getStatus());

        if (classEntity != null) {
            memberVO.setClassName(classEntity.getClassName());
            memberVO.setDescription(classEntity.getDescription());
            memberVO.setCourseId(classEntity.getCourseId());
            memberVO.setJoinCode(classEntity.getJoinCode());
            memberVO.setClassStatus(classEntity.getStatus());
            
            // 查询并设置课程名称
            if (classEntity.getCourseId() != null) {
                Course course = courseMapper.selectById(classEntity.getCourseId());
                if (course != null) {
                    memberVO.setCourseName(course.getClassName());
                }
            }
        }

        if (teacher != null) {
            memberVO.setTeacherName(teacher.getUsername());
            memberVO.setTeacherEmail(teacher.getEmail());
            memberVO.setTeacherAvatar(teacher.getAvatar());
            memberVO.setUsername(teacher.getUsername());
            memberVO.setAvatar(teacher.getAvatar());
        }

        Users student = userMapper.selectById(member.getUserId());
        if (student != null) {
            memberVO.setStudentName(student.getUsername());
            memberVO.setStudentAvatar(student.getAvatar());
            memberVO.setEmail(student.getEmail());
            memberVO.setSchoolNumber(student.getSchoolNumber());
        }

        return memberVO;
    }
}
