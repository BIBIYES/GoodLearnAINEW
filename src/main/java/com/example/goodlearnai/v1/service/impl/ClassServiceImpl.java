package com.example.goodlearnai.v1.service.impl;

import com.example.goodlearnai.v1.entity.Class;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.mapper.ClassMapper;
import com.example.goodlearnai.v1.mapper.UserMapper;
import com.example.goodlearnai.v1.service.IClassService;
import com.example.goodlearnai.v1.common.Result;
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
 * <p>
 * 课程下的班级表 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-09-17
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

        // 判断是否为老师角色
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限创建班级: userId={}", userId);
            return Result.error("暂无权限创建班级");
        }

        try {
            // 设置创建时间和更新时间
            classEntity.setCreatedAt(LocalDateTime.now());
            classEntity.setUpdatedAt(LocalDateTime.now());
            // 设置负责教师ID为当前用户
            classEntity.setTeacherId(userId);
            // 设置默认状态为正常
            if (classEntity.getStatus() == null) {
                classEntity.setStatus(true);
            }

            // 保存班级信息
            if (save(classEntity)) {
                log.info("班级创建成功: userId={}, classId={}, className={}", userId, classEntity.getClassId(), classEntity.getClassName());
                return Result.success("班级创建成功");
            } else {
                log.error("班级创建失败: userId={}, className={}", userId, classEntity.getClassName());
                return Result.error("班级创建失败");
            }
        } catch (Exception e) {
            log.error("班级创建失败: userId={}, className={}, error={}", userId, classEntity.getClassName(), e.getMessage());
            return Result.error("班级创建失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Class> getClassInfo(Long classId) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        try {
            // 查询班级信息
            Class classEntity = getById(classId);
            if (classEntity == null) {
                log.warn("班级不存在: classId={}", classId);
                return Result.error("班级不存在");
            }

            // 权限验证：教师需要是该班级的负责教师，学生需要是该班级的成员
            if ("teacher".equals(role)) {
                if (!userId.equals(classEntity.getTeacherId())) {
                    log.warn("教师无权限查看班级信息: userId={}, classId={}", userId, classId);
                    return Result.error("您不是该班级的负责教师，无法查看班级信息");
                }
            } else if ("student".equals(role)) {
                // 检查学生是否在该班级中
                // 这里需要查询ClassMembers表，但当前类没有相关依赖
                // 为了简化，暂时允许所有学生查看班级基本信息
                log.info("学生查看班级信息: userId={}, classId={}", userId, classId);
            } else {
                log.warn("用户角色无权限查看班级信息: userId={}, role={}", userId, role);
                return Result.error("暂无权限查看班级信息");
            }

            log.info("获取班级信息成功: userId={}, classId={}, className={}", userId, classId, classEntity.getClassName());
            return Result.success("获取班级信息成功", classEntity);
        } catch (Exception e) {
            log.error("获取班级信息失败: userId={}, classId={}, error={}", userId, classId, e.getMessage());
            return Result.error("获取班级信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<java.util.List<ClassVO>> getAllClasses() {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        try {
            java.util.List<Class> classList;
            
            if ("teacher".equals(role)) {
                // 教师只能查看自己负责的班级
                classList = lambdaQuery()
                        .eq(Class::getTeacherId, userId)
                        .eq(Class::getStatus, true)
                        .list();
                log.info("教师获取班级列表成功: userId={}, count={}", userId, classList.size());
            } else if ("student".equals(role)) {
                // 学生可以查看所有班级的基本信息（用于选择加入班级等场景）
                classList = lambdaQuery()
                        .eq(Class::getStatus, true)
                        .list();
                log.info("学生获取班级列表成功: userId={}, count={}", userId, classList.size());
            } else {
                log.warn("用户角色无权限查看班级列表: userId={}, role={}", userId, role);
                return Result.error("暂无权限查看班级列表");
            }

            // 转换为ClassVO并关联查询教师姓名
            List<ClassVO> classVOList = classList.stream()
                    .map(classEntity -> {
                        ClassVO classVO = new ClassVO();
                        BeanUtils.copyProperties(classEntity, classVO);
                        
                        // 查询教师姓名
                        Users teacher = userMapper.selectById(classEntity.getTeacherId());
                        if (teacher != null) {
                            classVO.setTeacherName(teacher.getUsername());
                        }
                        
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

        // 判断是否为老师角色
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限修改班级信息: userId={}", userId);
            return Result.error("暂无权限修改班级信息");
        }

        try {
            // 验证班级是否存在
            Class existingClass = getById(classEntity.getClassId());
            if (existingClass == null) {
                log.warn("班级不存在: classId={}", classEntity.getClassId());
                return Result.error("班级不存在");
            }

            // 权限验证：只有班级负责教师才能修改
            if (!userId.equals(existingClass.getTeacherId())) {
                log.warn("教师无权限修改班级信息: userId={}, classId={}", userId, classEntity.getClassId());
                return Result.error("您不是该班级的负责教师，无法修改班级信息");
            }

            // 只允许修改班级名称和描述
            existingClass.setClassName(classEntity.getClassName());
            existingClass.setDescription(classEntity.getDescription());
            existingClass.setUpdatedAt(LocalDateTime.now());

            // 更新班级信息
            if (updateById(existingClass)) {
                log.info("班级信息修改成功: userId={}, classId={}, className={}", userId, classEntity.getClassId(), classEntity.getClassName());
                return Result.success("班级信息修改成功");
            } else {
                log.error("班级信息修改失败: userId={}, classId={}", userId, classEntity.getClassId());
                return Result.error("班级信息修改失败");
            }
        } catch (Exception e) {
            log.error("班级信息修改失败: userId={}, classId={}, error={}", userId, classEntity.getClassId(), e.getMessage());
            return Result.error("班级信息修改失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> deleteClass(Long classId) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        // 判断是否为老师角色
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限删除班级: userId={}", userId);
            return Result.error("暂无权限删除班级");
        }

        try {
            // 验证班级是否存在
            Class existingClass = getById(classId);
            if (existingClass == null) {
                log.warn("班级不存在: classId={}", classId);
                return Result.error("班级不存在");
            }

            // 检查班级是否已被删除
            if (!existingClass.getStatus()) {
                log.warn("班级已被删除: classId={}", classId);
                return Result.error("班级已被删除");
            }

            // 权限验证：只有班级负责教师才能删除
            if (!userId.equals(existingClass.getTeacherId())) {
                log.warn("教师无权限删除班级: userId={}, classId={}", userId, classId);
                return Result.error("您不是该班级的负责教师，无法删除班级");
            }

            // 软删除：将status设置为false
            existingClass.setStatus(false);
            existingClass.setUpdatedAt(LocalDateTime.now());

            // 更新班级状态
            if (updateById(existingClass)) {
                log.info("班级删除成功: userId={}, classId={}, className={}", userId, classId, existingClass.getClassName());
                return Result.success("班级删除成功");
            } else {
                log.error("班级删除失败: userId={}, classId={}", userId, classId);
                return Result.error("班级删除失败");
            }
        } catch (Exception e) {
            log.error("班级删除失败: userId={}, classId={}, error={}", userId, classId, e.getMessage());
            return Result.error("班级删除失败: " + e.getMessage());
        }
    }

}
