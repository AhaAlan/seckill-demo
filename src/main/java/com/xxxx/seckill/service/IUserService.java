package com.xxxx.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.vo.LoginVo;
import com.xxxx.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 服务类
 */
public interface IUserService extends IService<User> {

	/**
	 * 功能描述: 登录
	 */
	RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);


	/**
	 * 功能描述: 根据cookie获取用户
	 */
	User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);


	/**
	 * 功能描述:更新密码
	 */
	RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response);
}
