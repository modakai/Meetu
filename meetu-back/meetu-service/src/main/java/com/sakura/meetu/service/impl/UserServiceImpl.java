package com.sakura.meetu.service.impl;

import com.sakura.meetu.entity.User;
import com.sakura.meetu.mapper.UserMapper;
import com.sakura.meetu.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

}
