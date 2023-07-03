package com.sakura.meetu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.mapper.UserMapper;
import com.sakura.meetu.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.utils.Result;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-05-21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result login(User user) {
        // 查询用户
        User result = null;
        try {
            // 放在 Mysql 服务器宕机 导致无法查询出用户
            result = this.getOne(new QueryWrapper<User>().eq("username", user.getUsername()));
        } catch (Exception e) {
            throw new RuntimeException("系统异常");
        }

        if (result == null) {
            return Result.error("用户不存在");
        }
        if (!result.getPassword().equals(user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        return Result.success(result);
    }
}
