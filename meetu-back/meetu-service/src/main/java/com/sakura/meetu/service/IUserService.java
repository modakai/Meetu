package com.sakura.meetu.service;

import com.sakura.meetu.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.utils.Result;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sakura
 * @since 2023-05-21
 */
public interface IUserService extends IService<User> {

    Result login(User user);
}
