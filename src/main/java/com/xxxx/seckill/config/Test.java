package com.xxxx.seckill.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 乐字节：专注线上IT培训
 * 答疑老师微信：lezijie
 *
 * @author zhoubin
 * @since 1.0.0
 */
public class Test {
    public static final String QUEUE_NAME="hello";
    public static void main(String[] args) throws IOException, TimeoutException {
        //创建一个连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        System.out.println(factory.getPort());
         //工厂IP，连接RabbitMQ的队列
        factory.setHost("192.168.41.102");
        //用户名
        factory.setUsername("admin");
            //密码
        factory.setPassword("admin");
        //创建连接
        Connection connection = factory.newConnection();
        //获取信道
        Channel channel = connection.createChannel();
/**
 * 生成一个队列
 * 1、队列名称
 * 2、队列里的消息是否持久化（存放到磁盘） 默认情况消息存放在内存中
 * 3、该队列是否只供一个消费者进行消费， 是否进行共享，true表示不共享，false表示共
 享，可以供多个消费者消费
 * 4、是否自动删除，最后一个消费者断开连接后，该队列是否自动删除，true表示自动删除，
 false表示不自动删除
 * 5、其他参数，进阶使用
 */
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
//发消息
        String message = "hello world!";
        /**
        * 发送一个消息
        * 1、发送到哪个交换机，""：空串代表默认交换机
        * 2、路由的key值是哪个，本次是队列名称
        * 3、其他参数信息
        * 4、发送的消息的消息体，要传入二进制
        */
        channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
        System.out.println("消息发送完毕");

    }
}
