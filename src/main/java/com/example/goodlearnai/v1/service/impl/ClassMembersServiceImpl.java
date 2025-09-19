package com.example.goodlearnai.v1.service.impl;

import com.example.goodlearnai.v1.entity.ClassMembers;
import com.example.goodlearnai.v1.entity.Class;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.mapper.ClassMembersMapper;
import com.example.goodlearnai.v1.mapper.ClassMapper;
import com.example.goodlearnai.v1.mapper.UserMapper;
import com.example.goodlearnai.v1.service.IClassMembersService;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.utils.AuthUtil;
import com.example.goodlearnai.v1.vo.ClassMemberVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 班级成员表 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-09-17
 */
@Service
@Slf4j
public class ClassMembersServiceImpl extends ServiceImpl<ClassMembersMapper, ClassMembers> implements IClassMembersService {

    @Autowired
    private ClassMapper classMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<String> intoClass(ClassMembers classMembers) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        // 判断是否为学生角色
        if (!"student".equals(role)) {
            log.warn("用户暂无权限加入班级: userId={}", userId);
            return Result.error("暂无权限加入班级");
        }

        // 验证班级是否存在
        Class classEntity = classMapper.selectById(classMembers.getClassId());
        if (classEntity == null) {
            log.warn("班级不存在: classId={}", classMembers.getClassId());
            return Result.error("班级不存在");
        }

        // 验证加入码
        if (classMembers.getJoinCode() == null || classMembers.getJoinCode().trim().isEmpty()) {
            log.warn("加入码不能为空: userId={}, classId={}", userId, classMembers.getClassId());
            return Result.error("请输入班级加入码");
        }
        
        if (!classMembers.getJoinCode().equals(classEntity.getJoinCode())) {
            log.warn("加入码错误: userId={}, classId={}, inputCode={}", userId, classMembers.getClassId(), classMembers.getJoinCode());
            return Result.error("班级加入码错误");
        }

        // 检查学生是否已经在班级中
        QueryWrapper<ClassMembers> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_id", classMembers.getClassId())
                   .eq("user_id", userId)
                   .eq("status", true);
        
        ClassMembers existingMember = getOne(queryWrapper);
        if (existingMember != null) {
            log.info("学生已在班级中: userId={}, classId={}", userId, classMembers.getClassId());
            return Result.error("您已经在该班级中");
        }

        try {
            // 设置加入时间和用户ID
            classMembers.setJoinTime(LocalDateTime.now());
            classMembers.setUserId(userId);
            // 设置默认状态为正常
            if (classMembers.getStatus() == null) {
                classMembers.setStatus(true);
            }

            // 保存班级成员信息
            if (save(classMembers)) {
                log.info("学生加入班级成功: userId={}, classId={}", userId, classMembers.getClassId());
                return Result.success("加入班级成功");
            } else {
                log.error("学生加入班级失败: userId={}, classId={}", userId, classMembers.getClassId());
                return Result.error("加入班级失败");
            }
        } catch (Exception e) {
            log.error("学生加入班级失败: userId={}, classId={}, error={}", userId, classMembers.getClassId(), e.getMessage());
            return Result.error("加入班级失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<ClassMemberVO>> getClassMembers(Long classId) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        // 验证班级是否存在
        Class classEntity = classMapper.selectById(classId);
        if (classEntity == null) {
            log.warn("班级不存在: classId={}", classId);
            return Result.error("班级不存在");
        }

        // 权限验证：教师需要是该班级的负责教师，学生需要是该班级的成员
        if ("teacher".equals(role)) {
            if (!userId.equals(classEntity.getTeacherId())) {
                log.warn("教师无权限查看班级成员: userId={}, classId={}", userId, classId);
                return Result.error("您不是该班级的负责教师，无法查看成员列表");
            }
        } else if ("student".equals(role)) {
            // 检查学生是否在该班级中
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
            // 查询班级成员列表
            QueryWrapper<ClassMembers> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("class_id", classId)
                       .eq("status", true)
                       .orderByAsc("join_time");
            
            List<ClassMembers> membersList = list(queryWrapper);
            
            if (membersList.isEmpty()) {
                log.info("班级暂无成员: classId={}", classId);
                return Result.success("班级暂无成员", List.of());
            }
            
            // 转换为ClassMemberVO并关联查询学生信息
            List<ClassMemberVO> memberVOList = membersList.stream()
                    .map(member -> {
                        ClassMemberVO memberVO = new ClassMemberVO();
                        BeanUtils.copyProperties(member, memberVO);
                        
                        // 查询学生基本信息
                        Users student = userMapper.selectById(member.getUserId());
                        if (student != null) {
                            memberVO.setUsername(student.getUsername());
                            memberVO.setEmail(student.getEmail());
                            memberVO.setSchoolNumber(student.getSchoolNumber());
                            memberVO.setAvatar(student.getAvatar());
                        }
                        
                        return memberVO;
                    })
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

        // 权限验证：学生只能退出自己的班级，教师可以移除班级成员
        if ("student".equals(role)) {
            if (!currentUserId.equals(userId)) {
                log.warn("学生无权限操作其他用户: currentUserId={}, targetUserId={}", currentUserId, userId);
                return Result.error("您只能退出自己加入的班级");
            }
        } else if ("teacher".equals(role)) {
            // 验证教师是否为该班级的负责教师
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
            // 查找班级成员记录
            QueryWrapper<ClassMembers> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("class_id", classId)
                       .eq("user_id", userId)
                       .eq("status", true);
            
            ClassMembers classMember = getOne(queryWrapper);
            if (classMember == null) {
                log.warn("用户不在班级中或已退出: userId={}, classId={}", userId, classId);
                return Result.error("用户不在该班级中或已退出");
            }

            // 软删除：将status设置为false
            classMember.setStatus(false);
            
            if (updateById(classMember)) {
                String action = "student".equals(role) ? "退出班级" : "移除班级成员";
                log.info("{}成功: operatorId={}, userId={}, classId={}", action, currentUserId, userId, classId);
                return Result.success(action + "成功");
            } else {
                String action = "student".equals(role) ? "退出班级" : "移除班级成员";
                log.error("{}失败: operatorId={}, userId={}, classId={}", action, currentUserId, userId, classId);
                return Result.error(action + "失败");
            }
        } catch (Exception e) {
            String action = "student".equals(role) ? "退出班级" : "移除班级成员";
            log.error("{}失败: operatorId={}, userId={}, classId={}, error={}", action, currentUserId, userId, classId, e.getMessage());
            return Result.error(action + "失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<ClassMemberVO>> getMyClasses() {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        // 只有学生可以查询自己加入的班级
        if (!"student".equals(role)) {
            log.warn("用户角色无权限查询加入的班级: userId={}, role={}", userId, role);
            return Result.error("暂无权限查询加入的班级");
        }

        try {
            // 查询当前用户加入的班级成员记录
            QueryWrapper<ClassMembers> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("status", true)
                       .orderByDesc("join_time");
            
            List<ClassMembers> membersList = list(queryWrapper);
            
            if (membersList.isEmpty()) {
                log.info("用户暂未加入任何班级: userId={}", userId);
                return Result.success("您暂未加入任何班级", List.of());
            }
            
            // 转换为ClassMemberVO并关联查询班级和教师信息
            List<ClassMemberVO> classVOList = membersList.stream()
                    .map(member -> {
                        ClassMemberVO memberVO = new ClassMemberVO();
                        // 复制班级成员基本信息
                        memberVO.setId(member.getId());
                        memberVO.setClassId(member.getClassId());
                        memberVO.setJoinTime(member.getJoinTime());
                        memberVO.setStatus(member.getStatus());
                        
                        // 查询班级基本信息
                        Class classEntity = classMapper.selectById(member.getClassId());
                        if (classEntity != null) {
                            // 查询教师信息并填充到VO中
                            Users teacher = userMapper.selectById(classEntity.getTeacherId());
                            if (teacher != null) {
                                memberVO.setUsername(teacher.getUsername());
                                memberVO.setEmail(teacher.getEmail());
                                memberVO.setSchoolNumber(teacher.getSchoolNumber());
                                memberVO.setAvatar(teacher.getAvatar());
                            }
                        }
                        
                        return memberVO;
                    })
                    .collect(Collectors.toList());
            
            log.info("获取用户加入的班级列表成功: userId={}, classCount={}", userId, classVOList.size());
            return Result.success("获取加入的班级列表成功", classVOList);
        } catch (Exception e) {
            log.error("获取用户加入的班级列表失败: userId={}, error={}", userId, e.getMessage());
            return Result.error("获取加入的班级列表失败: " + e.getMessage());
        }
    }

}
