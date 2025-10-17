-- 如果表已存在，先删除
DROP TABLE IF EXISTS `file_resource`;

-- 创建文件资源表
CREATE TABLE `file_resource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `hash_data` varchar(64) NOT NULL COMMENT '文件SHA256哈希值，用于文件去重',
  `path` varchar(255) NOT NULL COMMENT '文件存储相对路径',
  `original_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件MIME类型',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小（字节）',
  `upload_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hash_data` (`hash_data`) COMMENT '哈希值唯一索引，用于去重',
  KEY `idx_upload_time` (`upload_time`) COMMENT '上传时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件资源存储表';

-- 添加索引说明
ALTER TABLE `file_resource` COMMENT='文件资源存储表，用于存储上传的各类文件的元数据信息'; 