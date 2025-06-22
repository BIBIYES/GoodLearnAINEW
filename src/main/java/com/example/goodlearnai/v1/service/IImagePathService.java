package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ImagePath;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 图片路径存储 服务类
 * </p>
 *
 * @since 2025-06-20
 */
public interface IImagePathService extends IService<ImagePath> {

    /**
     * 上传图片
     */
    Result<String> uploadImage(MultipartFile image, HttpServletRequest request);
}
