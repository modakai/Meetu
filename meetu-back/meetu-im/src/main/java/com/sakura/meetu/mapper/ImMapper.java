package com.sakura.meetu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakura.meetu.entity.Im;
import com.sakura.meetu.entity.ImVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 聊天信息表 Mapper 接口
 * </p>
 *
 * @author sakura
 * @since 2023-08-20
 */
@Mapper
public interface ImMapper extends BaseMapper<Im> {

    List<ImVo> selectImMessageLimit(@Param("nums") Integer limit);

    List<ImVo> selectAllWhere(@Param("username") String username, @Param("text") String text);
}
