package com.xxxx.seckill.controller;

import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.vo.LoginVo;
import com.xxxx.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 登录
 * @Slf4j lombok的注解，用来输出日志
 */

@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

	@Autowired
	private IUserService userService;

	/**
	 * 功能描述: 跳转到登录页面
	 */
	@RequestMapping(value ="/toLogin",method = RequestMethod.GET)
	public String toLogin(){
		return "login";
	}

	/**
	 * 功能描述: 登录功能
	 * 注解 @Valid 用于参数校验
	 */
	@RequestMapping(value = "/doLogin", method = RequestMethod.POST)
	@ResponseBody
	public RespBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response){
		return userService.doLogin(loginVo,request,response);
	}

}