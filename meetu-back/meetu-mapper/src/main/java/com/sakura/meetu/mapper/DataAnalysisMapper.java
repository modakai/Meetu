package com.sakura.meetu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * @author sakura
 * @date 2023/9/15 20:20:39 周五
 */
@Mapper
public interface DataAnalysisMapper {

    @Select("SELECT " +
            "(SELECT COUNT(*) FROM sys_user WHERE deleted = 0) AS user_count, " +
            "(SELECT COUNT(*) FROM orders WHERE deleted = 0) AS order_count, " +
            "(SELECT COUNT(*) FROM dynamic WHERE deleted = 0) AS dynamic_count, " +
            "(SELECT COUNT(*) FROM tag) AS tag_count;")
    Map<String, Long> selectSystemData();
}
