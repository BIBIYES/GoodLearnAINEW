package com.example.goodlearnai.v1.service.impl;

import com.example.goodlearnai.v1.entity.Classes;
import com.example.goodlearnai.v1.mapper.ClassesMapper;
import com.example.goodlearnai.v1.service.IClassesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
    public String addClass(Classes classes) {
        return "";
    }
}
