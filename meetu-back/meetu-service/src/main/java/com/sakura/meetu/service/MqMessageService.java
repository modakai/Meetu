package com.sakura.meetu.service;

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
    void sendEmailToMQ(String exchangeName, String nodeKey, String email, String emailType);

    void sendFileToMq(String nodeKey, Object message);


}
