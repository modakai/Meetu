package com.sakura.meetu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakura.meetu.entity.Dynamic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author sakura
 * @since 2023-09-04
 */
@Mapper
public interface DynamicMapper extends BaseMapper<Dynamic> {

    List<Dynamic> selectALLBYUserIdOrDynamicId(
            @Param("dynamicId") Integer dynamicId,
            @Param("userId") Integer userId,
            @Param("dynamicName") String dynamicName
    );

    List<Dynamic> selectHotAll();

    @Select("SELECT " +
            " DATE( dynamic.time ) AS `name`, " +
            " COUNT(*) AS `value` " +
            " FROM " +
            " dynamic " +
            " WHERE " +
            " deleted = 0 " +
            "GROUP BY " +
            " DATE( dynamic.time ); ")
    List<Map<String, Long>> analysisDynamic();

}
