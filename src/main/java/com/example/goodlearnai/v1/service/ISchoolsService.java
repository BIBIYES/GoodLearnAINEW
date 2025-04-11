package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Schools;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mouse
 * @since 2025-04-01
 */
public interface ISchoolsService extends IService<Schools> {

    Result<List<Schools>> getSchools();

    Result<List<Schools>> addSchools(Schools schools);
}
