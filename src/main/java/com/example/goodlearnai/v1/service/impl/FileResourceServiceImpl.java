package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.FileResource;
import com.example.goodlearnai.v1.mapper.FileResourceMapper;
import com.example.goodlearnai.v1.service.IFileResourceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件资源服务实现类
 * </p>
 *
 * @author Mouse
 * @since 2025-06-20
 */
@Slf4j
@Service
public class FileResourceServiceImpl extends ServiceImpl<FileResourceMapper, FileResource> implements IFileResourceService {

    /**
     * 文件上传基本路径
     */
    @Value("${file.upload.base-path}")
    private String baseUploadPath;

    /**
     * 文件访问URL前缀
     */
    @Value("${file.upload.access-url}")
    private String fileAccessUrl;

    @Override
    public Result<String> uploadFile(MultipartFile file, HttpServletRequest request) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return Result.error("上传文件不能为空");
            }

            // 读取文件为字节数组
            InputStream in = file.getInputStream();
            byte[] bytes = in.readAllBytes();

            // 计算文件的SHA256哈希值，用于文件去重
            String hashData = DigestUtils.sha256Hex(bytes);

            // 通过哈希值查询数据库检查文件是否已存在
            LambdaQueryWrapper<FileResource> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FileResource::getHashData, hashData);
            FileResource exist = getOne(queryWrapper);
            
            // 如果文件已存在，直接返回原文件路径
            if (exist != null) {
                return Result.success("文件已存在", fileAccessUrl + exist.getPath());
            }

            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            
            // 提取文件扩展名
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 文件名 = 哈希值 + 原始扩展名
            String fileName = hashData + extension;
            
            // 创建存储目录的年月子文件夹，方便管理大量文件
            LocalDateTime now = LocalDateTime.now();
            String yearMonth = now.getYear() + "-" + String.format("%02d", now.getMonthValue());
            String relativePath = yearMonth + "/";
            
            // 确保目标目录存在
            File destDir = new File(baseUploadPath + relativePath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            // 完整文件保存路径
            String fullPath = baseUploadPath + relativePath + fileName;
            
            // 保存文件到目标路径
            File destFile = new File(fullPath);
            file.transferTo(destFile);

            // 构建文件资源对象
            FileResource fileResource = new FileResource();
            fileResource.setHashData(hashData);
            fileResource.setPath(relativePath + fileName);
            fileResource.setOriginalName(originalFilename);
            fileResource.setFileType(file.getContentType());
            fileResource.setFileSize(file.getSize());
            fileResource.setUploadTime(now);
            
            // 保存文件信息到数据库
            this.save(fileResource);

            log.info("文件上传成功，文件路径：{}", fileResource.getPath());

            // 返回成功结果和文件访问路径
            return Result.success("上传成功", fileAccessUrl + fileResource.getPath());

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("上传失败，请稍后再试");
        }
    }
} 