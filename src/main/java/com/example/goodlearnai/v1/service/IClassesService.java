package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.entity.Classes;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2025-03-01
 */
public interface IClassesService extends IService<Classes> {

    String addClass(Classes classes);
}
