package com.xxxx.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息发送者
 */

@Service
@Slf4j
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //发送秒杀信息
    public void sendSeckillMessage(String msg) {
        log.info("发送消息：" + msg);
        rabbitTemplate.convertAndSend("seckillExchange","seckill.messager", msg);
    }


//    /**
//     * 发送信息
//     **/
////    public void send(String message) {
////        log.info("发送消息" + message);
////        rabbitTemplate.convertAndSend("queue", message);
////    }
//
//
//    /**
//     * 发送消息
//     * 发送到fanout交换器
//     */
//    public void send(Object msg) {
//        log.info("发送消息：" + msg);
//        rabbitTemplate.convertAndSend("fanoutExchange","", msg);
//    }
//
//
//    /**
//     * 发送消息
//     * 发送到direct交换器
//     * @param msg
//     */
//    public void send01(Object msg) {
//        log.info("发送red" + msg);
//        rabbitTemplate.convertAndSend("directExchange", "queue.red", msg);
//    }
//
//    public void send02(Object msg) {
//        log.info("发送green" + msg);
//        rabbitTemplate.convertAndSend("directExchange", "queue.green", msg);
//    }
//
//    /**
//     * 发送消息
//     * 发送到topic交换器
//     */
//    public void send03(Object msg) {
//        log.info("发送消息(QUEUE01接收)：" + msg);
//        rabbitTemplate.convertAndSend("topicExchange", "queue.red.message", msg);
//    }
//
//
//    public void send04(Object msg) {
//        log.info("发送消息(QUEUE01和QUEUE02接收)：" + msg);
//        rabbitTemplate.convertAndSend("topicExchange", "green.queue.green.message", msg);
//    }





}
