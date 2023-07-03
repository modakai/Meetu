package com.sakura.meetu.constants;

/**
 * RabbitMQ的常量类 用于保存 RabbitMQ的队列, 队列路由的值
 *
 * @author sakura
 * @date 2023/7/5 14:14:11 周三
 */
public interface RabbitMqConstants {
    /**
     * 邮箱队列
     */
    String EMAIL_EXCHANGE_NAME = "meetu-email";
    /**
     * 与meetu-dired 交互机绑定的 email-normal 队列的路由key
     */
    String EMAIL_QUEUE_ROUTER_KEY = "send";

    String EMAIL_QUEUE_NAME = "meetu-email-normal";
}
