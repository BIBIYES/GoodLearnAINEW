package com.example.goodlearnai.v1.service.impl;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Class;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.mapper.ClassMapper;
import com.example.goodlearnai.v1.mapper.UserMapper;
import com.example.goodlearnai.v1.service.IClassService;
import com.example.goodlearnai.v1.utils.AuthUtil;
import com.example.goodlearnai.v1.vo.ClassVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 班级服务实现
 */
@Service
@Slf4j
public class ClassServiceImpl extends ServiceImpl<ClassMapper, Class> implements IClassService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<String> createClass(Class classEntity) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限创建班级: userId={}", userId);
            return Result.error("暂无权限创建班级");
        }

        try {
            classEntity.setCreatedAt(LocalDateTime.now());
            classEntity.setUpdatedAt(LocalDateTime.now());
            classEntity.setTeacherId(userId);
            if (classEntity.getJoinCode() != null) {
                classEntity.setJoinCode(classEntity.getJoinCode().trim().toUpperCase());
            }
            if (classEntity.getStatus() == null) {
                classEntity.setStatus(true);
            }

            if (save(classEntity)) {
                log.info("班级创建成功: userId={}, classId={}, className={}",
                        userId, classEntity.getClassId(), classEntity.getClassName());
                return Result.success("班级创建成功");
            }

            log.error("班级创建失败: userId={}, className={}", userId, classEntity.getClassName());
            return Result.error("班级创建失败");
        } catch (Exception e) {
            log.error("班级创建失败: userId={}, className={}, error={}",
                    userId, classEntity.getClassName(), e.getMessage());
            return Result.error("班级创建失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Class> getClassInfo(Long classId) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        try {
            Class classEntity = getById(classId);
            if (classEntity == null) {
                log.warn("班级不存在: classId={}", classId);
                return Result.error("班级不存在");
            }

            if ("teacher".equals(role)) {
                if (!userId.equals(classEntity.getTeacherId())) {
                    log.warn("教师无权限查看班级信息: userId={}, classId={}", userId, classId);
                    return Result.error("您不是该班级的负责教师，无法查看班级信息");
                }
            } else if (!"student".equals(role)) {
                log.warn("用户角色无权限查看班级信息: userId={}, role={}", userId, role);
                return Result.error("暂无权限查看班级信息");
            }

            log.info("获取班级信息成功: userId={}, classId={}, className={}",
                    userId, classId, classEntity.getClassName());
            return Result.success("获取班级信息成功", classEntity);
        } catch (Exception e) {
            log.error("获取班级信息失败: userId={}, classId={}, error={}",
                    userId, classId, e.getMessage());
            return Result.error("获取班级信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<ClassVO>> getAllClasses() {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        try {
            List<Class> classList;

            if ("teacher".equals(role)) {
                classList = lambdaQuery()
                        .eq(Class::getTeacherId, userId)
                        .eq(Class::getStatus, true)
                        .list();
                log.info("教师获取班级列表成功: userId={}, count={}", userId, classList.size());
            } else if ("student".equals(role)) {
                classList = lambdaQuery()
                        .eq(Class::getStatus, true)
                        .list();
                log.info("学生获取班级列表成功: userId={}, count={}", userId, classList.size());
            } else {
                log.warn("用户角色无权限查看班级列表: userId={}, role={}", userId, role);
                return Result.error("暂无权限查看班级列表");
            }

            List<ClassVO> classVOList = classList.stream()
                    .map(classEntity -> {
                        ClassVO classVO = new ClassVO();
                        BeanUtils.copyProperties(classEntity, classVO);

                        Users teacher = userMapper.selectById(classEntity.getTeacherId());
                        if (teacher != null) {
                            classVO.setTeacherName(teacher.getUsername());
                        }
                        classVO.setJoinCode(classEntity.getJoinCode());

                        return classVO;
                    })
                    .collect(Collectors.toList());

            return Result.success("获取班级列表成功", classVOList);
        } catch (Exception e) {
            log.error("获取班级列表失败: userId={}, error={}", userId, e.getMessage());
            return Result.error("获取班级列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> updateClassMember(Class classEntity) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限修改班级信息: userId={}", userId);
            return Result.error("暂无权限修改班级信息");
        }

        try {
            Class existingClass = getById(classEntity.getClassId());
            if (existingClass == null) {
                log.warn("班级不存在: classId={}", classEntity.getClassId());
                return Result.error("班级不存在");
            }

            if (!userId.equals(existingClass.getTeacherId())) {
                log.warn("教师无权限修改班级信息: userId={}, classId={}", userId, classEntity.getClassId());
                return Result.error("您不是该班级的负责教师，无法修改班级信息");
            }

            existingClass.setClassName(classEntity.getClassName());
            existingClass.setDescription(classEntity.getDescription());
            existingClass.setUpdatedAt(LocalDateTime.now());

            if (updateById(existingClass)) {
                log.info("班级信息修改成功: userId={}, classId={}, className={}",
                        userId, classEntity.getClassId(), classEntity.getClassName());
                return Result.success("班级信息修改成功");
            }

            log.error("班级信息修改失败: userId={}, classId={}", userId, classEntity.getClassId());
            return Result.error("班级信息修改失败");
        } catch (Exception e) {
            log.error("班级信息修改失败: userId={}, classId={}, error={}",
                    userId, classEntity.getClassId(), e.getMessage());
            return Result.error("班级信息修改失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> deleteClass(Long classId) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限删除班级: userId={}", userId);
            return Result.error("暂无权限删除班级");
        }

        try {
            Class existingClass = getById(classId);
            if (existingClass == null) {
                log.warn("班级不存在: classId={}", classId);
                return Result.error("班级不存在");
            }

            if (!Boolean.TRUE.equals(existingClass.getStatus())) {
                log.warn("班级已被删除: classId={}", classId);
                return Result.error("班级已被删除");
            }

            if (!userId.equals(existingClass.getTeacherId())) {
                log.warn("教师无权限删除班级: userId={}, classId={}", userId, classId);
                return Result.error("您不是该班级的负责教师，无法删除班级");
            }

            existingClass.setStatus(false);
            existingClass.setUpdatedAt(LocalDateTime.now());

            if (updateById(existingClass)) {
                log.info("班级删除成功: userId={}, classId={}, className={}",
                        userId, classId, existingClass.getClassName());
                return Result.success("班级删除成功");
            }

            log.error("班级删除失败: userId={}, classId={}", userId, classId);
            return Result.error("班级删除失败");
        } catch (Exception e) {
            log.error("班级删除失败: userId={}, classId={}, error={}",
                    userId, classId, e.getMessage());
            return Result.error("班级删除失败: " + e.getMessage());
        }
    }
}
