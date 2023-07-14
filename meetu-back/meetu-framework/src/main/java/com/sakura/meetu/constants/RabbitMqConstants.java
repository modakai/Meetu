package com.sakura.meetu.constants;

/**
 * RabbitMQ的常量类 用于保存 RabbitMQ的队列, 队列路由的值
 *
 * @author sakura
 * @date 2023/7/5 14:14:11 周三
 */
public interface RabbitMqConstants {
    /**
     * 邮箱服务交换机名称
     */
    String EMAIL_EXCHANGE_NAME = "meetu-email";
    /**
     * 与meetu-dired 交互机绑定的 email-normal 队列的路由key
     */
    String EMAIL_QUEUE_ROUTER_KEY = "send";

    /**
     * 发布邮箱队列名称
     */
    String EMAIL_QUEUE_NAME = "meetu-email-send";

    /**
     * 邮箱死信队列的名称
     */
    String EMAIL_DEAD_QUEUE_NAME = "meetu-email-dead";
}
