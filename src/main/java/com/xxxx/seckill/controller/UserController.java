package com.xxxx.seckill.controller;


import com.xxxx.seckill.rabbitmq.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 前端控制器
 */
@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private MQSender mqSender;

//	/**
//	 * 功能描述: 用户信息(测试)
//	 */
//	@RequestMapping("/info")
//	@ResponseBody
//	public RespBean info(User user) {
//		return RespBean.success(user);
//	}
//
//
//	 /**
//	  * 功能描述: 测试发送RabbitMQ消息
//	  * 默认的交换器就是direct
//	  */
//	 @RequestMapping("/mq")
//	 @ResponseBody
//	 public void mq() {
//	 	mqSender.send("Hello");
//	 }
//
//
//	 /**
//	  * 功能描述: 测试rabbitmq交换器的 Fanout模式
//	  */
//	 @RequestMapping("/mq/fanout")
//	 @ResponseBody
//	 public void mq01() {
//	 	mqSender.send("Hello");
//	 }
//
//	 /**
//	  * 功能描述: 测试rabbitmq交换器的 Direct模式
//	  */
//	 @RequestMapping("/mq/direct01")
//	 @ResponseBody
//	 public void mq02() {
//	 	mqSender.send01("Hello,Green");
//	 }
//
//
//	 /**
//	  * 功能描述: 测试rabbitmq交换器的Direct模式
//	  */
//	 @RequestMapping("/mq/direct02")
//	 @ResponseBody
//	 public void mq03() {
//	 	mqSender.send02("Hello,Red");
//	 }
//
//
//	 /**
//	  * 功能描述: Topic模式
//	  */
//	 @RequestMapping("/mq/topic01")
//	 @ResponseBody
//	 public void mq04() {
//	 	mqSender.send03("Hello,Red");
//	 }
//
//	 @RequestMapping("/mq/topic02")
//	 @ResponseBody
//	 public void mq05() {
//	 	mqSender.send04("Hello,Green");
//	 }



}
