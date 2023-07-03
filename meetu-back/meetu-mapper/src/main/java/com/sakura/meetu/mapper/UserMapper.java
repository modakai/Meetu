package com.sakura.meetu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakura.meetu.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author sakura
 * @since 2023-05-21
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    User selectUserOne(User user);
}
