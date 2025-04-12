package com.example.goodlearnai.v1.service.impl;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Schools;
import com.example.goodlearnai.v1.mapper.SchoolsMapper;
import com.example.goodlearnai.v1.service.ISchoolsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mouse
 * @since 2025-04-01
 */
@Slf4j
@Service
public class SchoolsServiceImpl extends ServiceImpl<SchoolsMapper, Schools> implements ISchoolsService {

    @Override
    public Result<List<Schools>> getSchools() {
        List<Schools> schoolsList = list();
        if(!schoolsList.isEmpty()){
            return Result.success("获取所有学校",schoolsList);
        }
        return Result.error("获取失败");
    }

    /**
     * 超级管理员添加学校
     */
    @Override
    public Result<String> addSchools(Schools schools) {
        String role = AuthUtil.getCurrentRole();
        log.debug("当前用户角色为：{}", role);
        log.debug("当前用户：{}", AuthUtil.getCurrentUserId());


        if (!"root".equals(role)) {
            return Result.error("权限不足");
        }
        boolean flag = save(schools);
        if (flag) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }
}
