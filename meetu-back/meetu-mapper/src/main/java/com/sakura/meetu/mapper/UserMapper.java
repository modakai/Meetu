package com.sakura.meetu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakura.meetu.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

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
    User selectOneByUsernameEmail(String username);

    @Update("UPDATE sys_user SET score = score + #{score} WHERE id = #{id}")
    void updateUserScore(@Param("score") int score, @Param("id") Integer id);
}
