package com.sakura.meetu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakura.meetu.entity.Comments;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author sakura
 * @since 2023-09-10
 */
@Mapper
public interface CommentsMapper extends BaseMapper<Comments> {

    List<Comments> selectAllByDynamicId(Integer dynamicId);

    List<Comments> selectAllByDynamicName(String dynamicName);
}
