package com.sakura.meetu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakura.meetu.entity.Collect;
import com.sakura.meetu.vo.CollectVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author sakura
 * @since 2023-09-09
 */
@Mapper
public interface CollectMapper extends BaseMapper<Collect> {

    List<CollectVo> selectAllByUser(@Param("userId") Integer userId, @Param("username") String username);

}
