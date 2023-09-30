package com.sakura.meetu.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.constants.Constant;
import com.sakura.meetu.entity.Messages;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.mapper.MessagesMapper;
import com.sakura.meetu.service.IMessagesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-09-13
 */
@Service
public class MessagesServiceImpl extends ServiceImpl<MessagesMapper, Messages> implements IMessagesService {

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void createMessages(User user, Integer dynamicId, Integer dynamicUserId, String content, String operation) {
        // TODO (sakura, 2023/9/13, 21:10, 记得跟前端要访问地址)
        switch (operation) {
            case Constant.OPERATION_COLLECT:
            case Constant.OPERATION_PRAISE:
                content = user.getName() + operation + "了您的动态<strong>+" + content + "</strong>";
                break;
            case Constant.OPERATION_COMMENTS:
                String[] split = content.split(",");
                String title = split[0];
                String commentsContent = split[1];
                content = user.getName() + operation + "了您的动态<strong>" + title + "</strong> <br/>回复的内容：<span>" + commentsContent + "</span>";
                break;
        }

        Messages messages = new Messages();
        messages.setUserId(dynamicUserId);
        messages.setContent(content);
        messages.setTime(DateUtil.now());
        messages.setDynamicId(dynamicId);
        save(messages);
    }
}
