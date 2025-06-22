package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.service.IFileResourceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 文件资源管理控制器
 * </p>
 *
 * @author Mouse
 * @since 2025-06-20
 */
@Slf4j
@RestController
@RequestMapping("/v1/file-resource")
public class FileResourceController {

    /**
     * 文件资源服务
     */
    @Autowired
    private IFileResourceService fileResourceService;

    /**
     * 上传文件接口
     * 
     * @param file 上传的文件对象
     * @param request HTTP请求对象
     * @return 文件访问路径
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        log.info("收到文件上传请求，文件名: {}, 大小: {}", file.getOriginalFilename(), file.getSize());
        return fileResourceService.uploadFile(file, request);
    }
} 