package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import com.example.goodlearnai.v1.service.impl.ImagePathServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 图片路径存储 前端控制器
 * </p>
 *
 * @since 2025-06-20
 */
@Slf4j
@RestController
@RequestMapping("/v1/image-path")
public class ImagePathController {

    @Autowired
    private ImagePathServiceImpl imagePathService;

    /**
     * 上传图片返回对应路径
     */
    @PostMapping("/upload-image")
    public Result<String> uploadImage(@RequestParam("image")MultipartFile  image, HttpServletRequest request) {
        return imagePathService.uploadImage(image, request);
    }

}
