package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.FileResource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 文件资源服务接口
 * </p>
 *
 * @author Mouse
 * @since 2025-06-20
 */
public interface IFileResourceService extends IService<FileResource> {

    /**
     * 上传文件
     *
     * @param file 文件对象
     * @param request HTTP请求
     * @return 文件访问路径
     */
    Result<String> uploadFile(MultipartFile file, HttpServletRequest request);
} 