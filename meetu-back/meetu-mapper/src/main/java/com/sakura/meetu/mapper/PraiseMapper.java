package com.sakura.meetu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakura.meetu.entity.Praise;
import com.sakura.meetu.vo.PraiseVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author sakura
 * @since 2023-09-06
 */
@Mapper
public interface PraiseMapper extends BaseMapper<Praise> {

    List<PraiseVo> selectAllByUser(@Param("praiseId") Integer praiseId, @Param("username") String username);
}
