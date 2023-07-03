package com.sakura.meetu.service;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

import java.util.Map;

/**
 * @author sakura
 * @date 2023/7/5 14:14:02 周三
 */
public interface MqMessageService {

    /**
     * 发送邮箱业务转发到 MQ
     *
     * @param exchangeName 交换机的名称
     * @param nodeKey      mq的路由key
     */
    void sendEmailToMQ(String exchangeName, String nodeKey, Map<String, String> messageMap);

    void receiveEmail(Message emailMsg, Channel channel);
}
