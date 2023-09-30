package com.sakura.meetu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.entity.Messages;
import com.sakura.meetu.entity.User;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sakura
 * @since 2023-09-13
 */
public interface IMessagesService extends IService<Messages> {

    void createMessages(User user, Integer dynamicId, Integer dynamicUserId, String content, String operation);
}
