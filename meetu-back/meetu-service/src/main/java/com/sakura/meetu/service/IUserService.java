package com.sakura.meetu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.dto.UserDto;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.utils.Result;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sakura
 * @since 2023-05-21
 */
public interface IUserService extends IService<User> {


    Result register(UserDto userDto);

    Result sendEmail(String email, String type);

    Result normalLogin(UserDto loginUserDto);

    Result emailLogin(UserDto loginUserDto);

    Result modifyUser(UserDto userDto);

    Result logout(String uid, String thenLoginType);

    Result insertUserBatch(List<User> list);

    Set<User> verifyBatchData(List<User> list);

    Result removeBatch(List<Integer> ids);
}
