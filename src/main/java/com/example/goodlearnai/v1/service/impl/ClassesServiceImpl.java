package com.example.goodlearnai.v1.service.impl;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Classes;
import com.example.goodlearnai.v1.mapper.ClassesMapper;
import com.example.goodlearnai.v1.service.IClassesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2025-03-01
 */
@Service
public class ClassesServiceImpl extends ServiceImpl<ClassesMapper, Classes> implements IClassesService {

    @Override
    public Result<String> addClass(Classes classes) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        log.debug(String.valueOf(userId));
        log.debug(role);
        if(!role.equals("teacher")) {
            log.warn("用户暂无权限");
            return Result.error("暂无权限");
        }
        classes.setTeacherId(userId);
        boolean flag = save(classes);
        if(flag){
            return Result.success("班级创建成功");
        }else {
            return Result.error("创建失败");
        }
    }
}
