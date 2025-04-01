package com.example.goodlearnai.v1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.goodlearnai.v1.entity.StudentAttendanceRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 学生签到记录表 Mapper 接口
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-01
 */
@Mapper
public interface StudentAttendanceRecordMapper extends BaseMapper<StudentAttendanceRecord> {
    
}