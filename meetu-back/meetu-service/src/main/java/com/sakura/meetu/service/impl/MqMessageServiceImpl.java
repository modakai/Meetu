package com.sakura.meetu.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import com.aliyun.oss.OSSException;
import com.rabbitmq.client.Channel;
import com.sakura.meetu.constants.Constant;
import com.sakura.meetu.constants.RabbitMqConstants;
import com.sakura.meetu.constants.RedisKeyConstants;
import com.sakura.meetu.service.MqMessageService;
import com.sakura.meetu.service.OssService;
import com.sakura.meetu.utils.EmailUtils;
import com.sakura.meetu.utils.RedisUtils;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.sakura.meetu.constants.RedisKeyConstants.EMAIL_CODE_TTL_5_MS;

/**
 * @author sakura
 * @date 2023/7/5 14:14:03 周三
 */
@Service
public class MqMessageServiceImpl implements MqMessageService {

    private static final Logger log = LoggerFactory.getLogger(MqMessageServiceImpl.class);

    private final OssService ossService;
    private final RabbitTemplate rabbitTemplate;
    private final EmailUtils emailUtils;
    private final RedisUtils redisUtils;

    public MqMessageServiceImpl(OssService ossService, RabbitTemplate rabbitTemplate, EmailUtils emailUtils, RedisUtils redisUtils) {
        this.ossService = ossService;
        this.rabbitTemplate = rabbitTemplate;
        this.emailUtils = emailUtils;
        this.redisUtils = redisUtils;
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
    public void sendEmailToMQ(String exchangeName, String nodeKey, String email, String emailType) {
        Map<String, String> message = new HashMap<>(4);
        message.put("email", email);
        message.put("emailType", emailType);
        try {
            // 将数据转换成 Message对象
            rabbitTemplate.convertAndSend(exchangeName, nodeKey, message);
        } catch (AmqpException e) {
            log.error("RabbitMq 出现异常", e);
        }
        log.info("消息发送完毕! 消息前往 {} --- {} , 邮箱为: {}, 类型: {}", exchangeName, nodeKey, email, emailType);
    }

    /**
     * 发送 消息 给 指定的 消息队列
     *
     * @param nodeKey
     * @param message
     */
    @Override
    public void sendFileToMq(String nodeKey, Object message) {
        try {
            // 将数据转换成 Message对象
            rabbitTemplate.convertAndSend(RabbitMqConstants.OSS_FILE_EXCHANGE_NAME, nodeKey, message);
        } catch (AmqpException e) {
            log.error("RabbitMq 出现异常", e);
        }
        log.info("消息发送完毕! 消息前往 {} --- {} , 消息为：{}",
                RabbitMqConstants.OSS_FILE_EXCHANGE_NAME, nodeKey, message.toString());
    }

    /**
     * 删除阿里云单个文件的业务
     *
     * @param message 文件的url地址
     * @param channel 连接通道
     */
    @RabbitListener(queues = RabbitMqConstants.OSS_FILE_QUEUE_NAME)
    public void receiveDelFile(Message message, Channel channel) {
        // message 是 要删除的url地址
        String fileUrl = new String(message.getBody());
        log.info("将要删除 OSS 中的文件：{}", fileUrl);
        MessageProperties properties = message.getMessageProperties();
        long deliveryTag = properties.getDeliveryTag();

        try {
            ossService.removeFile(fileUrl);
            log.info("oss 删除文件成功：{}", fileUrl);
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("消息出现异常: {}", e.getMessage());
            try {
                channel.basicReject(deliveryTag, true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (OSSException oe) {
            log.error("Error Message: {}", oe.getErrorMessage());
            log.error("Error Code: {}", oe.getErrorCode());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    @RabbitListener(queues = RabbitMqConstants.OSS_FILE_DEAD_QUEUE_NAME)
    public void ossFileDelDeadHandler(Message message, Channel channel) {
        log.info("oss 文件删除死信队列接收到消息：{}", message);

        String fileUrl = new String(message.getBody());
        MessageProperties properties = message.getMessageProperties();
        long deliveryTag = properties.getDeliveryTag();

        boolean fileExist = ossService.checkFileExist(fileUrl);
        if (!fileExist) {
            // 文件不存在，说明也就删除
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // 文件还存在 则进行删除
        try {
            ossService.removeFile(fileUrl);
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("消息出现异常: {}", e.getMessage());
            try {
                channel.basicReject(deliveryTag, true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (OSSException oe) {
            log.error("Error Message: {}", oe.getErrorMessage());
            log.error("Error Code: {}", oe.getErrorCode());
            try {
                channel.basicReject(deliveryTag, true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * 处理发送邮箱的业务
     *
     * @param emailMsg 邮箱消息对象
     * @param channel  连接通道
     */
    @RabbitListener(queues = {RabbitMqConstants.EMAIL_QUEUE_NAME})
    public void receiveEmail(Message emailMsg, Channel channel) {
        Map<String, String> messageMap = (Map<String, String>) rabbitTemplate.getMessageConverter().fromMessage(emailMsg);
        String email = messageMap.get("email");
        String emailTypeValue = messageMap.get("emailType");
        log.info("meetu-email交换机接收到 : {} 消息队列接收到信息: {}", RabbitMqConstants.EMAIL_QUEUE_NAME, email);

        MessageProperties properties = emailMsg.getMessageProperties();
        // 拿到消息唯一标识
        long deliveryTag = properties.getDeliveryTag();

        // 获取验证码
        String code = RandomUtil.randomString(Constant.VERIFICATION_CODE, 6);
        log.info("用户(邮箱): {} 获取验证码 {}", email, code);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String context = "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "</head><body><p style='font-size: 20px;font-weight:bold;'>尊敬的：" + email + "，您好！</p>"
                + "<p style='text-indent:2em; font-size: 20px;'>欢迎注册MeetU交友网，您本次的验证码是 "
                + "<span style='font-size:30px;font-weight:bold;color:red'>" + code + "</span>，5分钟之内有效，请尽快使用！</p>"
                + "<p style='text-align:right; padding-right: 20px;'"
                + "<a href='#' style='font-size: 18px'>MeetU</a></p>"
                + "<span style='font-size: 18px; float:right; margin-right: 60px;'>" + sdf.format(new Date()) + "</span></body></html>";

        try {
            emailUtils.sendHtml("『MeetU交友网』邮箱验证提醒", context, email);
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("消息出现异常: {}", e.getMessage());
            try {
                channel.basicReject(deliveryTag, false);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        ThreadUtil.execAsync(() -> {
            // 保存验证码到Redis
            log.info("Redis 存储 email: {} 获取类型 {} 邮箱验证码: {}", email, emailTypeValue, code);
            try {
                redisUtils.setEx(RedisKeyConstants.EMAIL_CODE + emailTypeValue + ":" + email, code, EMAIL_CODE_TTL_5_MS, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("Redis 服务器发生异常: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 处理邮箱发送失败的消息队列
     *
     * @param deadLetterMsg 死信消息
     * @param channel       连接通道
     */
    @RabbitListener(queues = RabbitMqConstants.EMAIL_DEAD_QUEUE_NAME)
    public void emailDeadHandler(Message deadLetterMsg, Channel channel) {
        Map<String, String> message = (Map<String, String>) rabbitTemplate.getMessageConverter().fromMessage(deadLetterMsg);
        log.info("死信队列接收到信息: {}", message);
        long deliveryTag = deadLetterMsg.getMessageProperties().getDeliveryTag();
        try {
            emailUtils.sendHtml("『MeetU交友网』邮箱验证提醒", message.get("context"), message.get("email"));

            // 消息处理成功，确认消息已被消费
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("重新向 {} 发送邮件失败: {}", message.get("email"), e.getMessage());
            // 发送失败，将消息重新放入死信队列（可选择是否重新回滚队列）
            boolean requeue = true;  // 设置为 true 表示重新回滚队列
            try {
                channel.basicNack(deliveryTag, false, requeue);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
