package com.xxxx.seckill.controller;


import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.vo.OrderDetailVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 前端控制器

 */
@Controller
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private IOrderService orderService;

	/**
	 * 功能描述: 订单详情
	 * 页面静态化了，所以返回RespBean，使用@ResponseBody
	 */
	@RequestMapping("/detail")
	@ResponseBody
	public RespBean detail(User user, Long orderId) {
		if (user == null) {
			return RespBean.error(RespBeanEnum.SESSION_ERROR);
		}
		OrderDetailVo detail = orderService.detail(orderId);
		return RespBean.success(detail);
	}

}
