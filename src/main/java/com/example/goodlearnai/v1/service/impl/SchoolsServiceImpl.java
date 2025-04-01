package com.example.goodlearnai.v1.service.impl;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Schools;
import com.example.goodlearnai.v1.mapper.SchoolsMapper;
import com.example.goodlearnai.v1.service.ISchoolsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
}
