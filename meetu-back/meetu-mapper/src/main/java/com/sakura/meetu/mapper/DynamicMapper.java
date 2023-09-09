package com.sakura.meetu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakura.meetu.entity.Dynamic;
import com.sakura.meetu.vo.DynamicVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    List<DynamicVo> selectALLBYUserIdOrDynamicId(
            @Param("dynamicId") Integer dynamicId,
            @Param("userId") Integer userId,
            @Param("dynamicName") String dynamicName
    );
}
