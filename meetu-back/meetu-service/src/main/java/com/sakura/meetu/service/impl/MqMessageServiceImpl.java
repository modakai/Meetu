package com.sakura.meetu.service.impl;

import com.rabbitmq.client.Channel;
import com.sakura.meetu.constants.RabbitMqConstants;
import com.sakura.meetu.service.MqMessageService;
import com.sakura.meetu.utils.EmailUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * @author sakura
 * @date 2023/7/5 14:14:03 周三
 */
@Service
public class MqMessageServiceImpl implements MqMessageService {

    private static final Logger log = LoggerFactory.getLogger(MqMessageServiceImpl.class);

    private final RabbitTemplate rabbitTemplate;
    private final EmailUtils emailUtils;

    public MqMessageServiceImpl(RabbitTemplate rabbitTemplate, EmailUtils emailUtils) {
        this.rabbitTemplate = rabbitTemplate;
        this.emailUtils = emailUtils;
    }

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息正常到达交换机: {}", LocalDateTime.now());
                return;
            }
            // ack 为 true 则表示 消息成功的发送到交换机
            log.error("消息未到达交换机: {}", cause);
        });
    }

    @Override
    public void sendEmailToMQ(String exchangeName, String nodeKey, Map<String, String> messageMap) {
        String email = messageMap.get("email");
        String code = messageMap.get("code");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String context = "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "</head><body><p style='font-size: 20px;font-weight:bold;'>尊敬的：" + email + "，您好！</p>"
                + "<p style='text-indent:2em; font-size: 20px;'>欢迎注册MeetU交友网，您本次的验证码是 "
                + "<span style='font-size:30px;font-weight:bold;color:red'>" + code + "</span>，5分钟之内有效，请尽快使用！</p>"
                + "<p style='text-align:right; padding-right: 20px;'"
                + "<a href='#' style='font-size: 18px'>MeetU</a></p>"
                + "<span style='font-size: 18px; float:right; margin-right: 60px;'>" + sdf.format(new Date()) + "</span></body></html>";
        messageMap.put("context", context);
        try {
            // 将数据转换成 Message对象
            rabbitTemplate.convertAndSend(exchangeName, nodeKey, messageMap);
        } catch (AmqpException e) {
            log.error("RabbitMq 出现异常", e);
        }
        log.info("消息发送完毕! 消息前往 {} --- {} , 消息为: {}", exchangeName, nodeKey, messageMap);

    }

    @Override
    @RabbitListener(queues = {RabbitMqConstants.EMAIL_QUEUE_NAME})
    public void receiveEmail(Message emailMsg, Channel channel) {
        Map<String, String> message = (Map<String, String>) rabbitTemplate.getMessageConverter().fromMessage(emailMsg);
        log.info("meetu-email交换机接收到 : {} 消息队列接收到信息: {}", RabbitMqConstants.EMAIL_QUEUE_NAME, message);
        MessageProperties properties = emailMsg.getMessageProperties();
        // 拿到消息唯一标识
        long deliveryTag = properties.getDeliveryTag();
        try {
            emailUtils.sendHtml("『MeetU交友网』邮箱验证提醒", message.get("context"), message.get("email"));
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("消息出现异常: {}", e.getMessage());
            try {
                // 参数1,2 与上一致, 参数3 表示是否重新回滚队列
//                channel.basicNack(deliveryTag,false, true);
                channel.basicReject(deliveryTag, false);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
