package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ImagePath;
import com.example.goodlearnai.v1.mapper.ImagePathMapper;
import com.example.goodlearnai.v1.service.IImagePathService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class ImagePathServiceImpl extends ServiceImpl<ImagePathMapper, ImagePath> implements IImagePathService {

    @Override
    public Result<String> uploadImage(MultipartFile image, HttpServletRequest request) {
        try {
            if (image.isEmpty()) {
                return Result.error("上传文件不能为空");
            }

            // 读取文件为字节
            InputStream in = image.getInputStream();
            byte[] bytes = in.readAllBytes();

            // 计算 SHA256 哈希
            String hashData = DigestUtils.sha256Hex(bytes);

            // 查数据库去重
            LambdaQueryWrapper<ImagePath> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImagePath::getHashData, hashData);
            ImagePath exist = getOne(queryWrapper);
            if (exist != null) {
                return Result.success("文件已存在", exist.getPath());
            }

            // 获取文件后缀
            String originalFilename = image.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 文件名 = 哈希 + 后缀
            String fileName = hashData + extension;

            // 保存路径（相对路径）
            String basePath = System.getProperty("user.dir") + "/src/main/resources/static/images/";
            String fullPath = basePath + fileName;

            // 创建文件夹
            File destFile = new File(fullPath);
            destFile.getParentFile().mkdirs();

            // 保存文件
            image.transferTo(destFile);

            // 保存数据库记录
            ImagePath imagePath = new ImagePath();
            imagePath.setHashData(hashData);
            imagePath.setPath("/images/" + fileName);
            this.save(imagePath);

            log.info("上传成功，文件路径：{}", imagePath.getPath());

            return Result.success("上传成功", imagePath.getPath());

        } catch (IOException e) {
            log.error("上传失败", e);
            return Result.error("上传失败，请稍后再试");
        }
    }
}
