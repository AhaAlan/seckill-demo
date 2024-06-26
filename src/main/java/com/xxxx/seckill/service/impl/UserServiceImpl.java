package com.xxxx.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.mapper.UserMapper;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.utils.CookieUtil;
import com.xxxx.seckill.utils.MD5Util;
import com.xxxx.seckill.utils.UUIDUtil;
import com.xxxx.seckill.vo.LoginVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	//功能描述: 登录
	@Override
	public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
		String mobile = loginVo.getMobile();
		String password = loginVo.getPassword();


		//根据手机号获取用户
		User user = userMapper.selectById(mobile);
		if (null == user) {
			//对异常进行处理
			//return RespBean.error(RespBeanEnum.LOGIN_ERROR);
			throw  new GlobalException(RespBeanEnum.LOGIN_ERROR);
		}

		//判断密码是否正确
		if (!MD5Util.formPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
			//return RespBean.error(RespBeanEnum.LOGIN_ERROR);
			throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
		}

		//生成cookie
		String ticket = UUIDUtil.uuid();
		//将用户信息存入redis中（优化二：对象缓存）
		redisTemplate.opsForValue().set("user:" + ticket, user);
		CookieUtil.setCookie(request, response, "userTicket", ticket);
		return RespBean.success(ticket);	//这里要返回ticket
	}

	//功能描述: 根据cookie获取用户
	@Override
	public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isEmpty(userTicket)) {
			return null;
		}
		User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
		if (user != null) {
			CookieUtil.setCookie(request, response, "userTicket", userTicket);
		}
		return user;
	}


	/**
	 * 功能描述:更新密码
	 * 由于用户一直是存在redis里且永不失效的，如果需要更改密码，需要对redis里的旧信息进行变更
	 */
	@Override
	public RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response) {
		User user = getUserByCookie(userTicket, request, response);
		if (user == null) {
			throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
		}
		user.setPassword(MD5Util.inputPassToDBPass(password, user.getSalt()));	//更新密码
		int result = userMapper.updateById(user);
		if (1 == result) {	//说明上述操作成功
			//删除Redis
			redisTemplate.delete("user:" + userTicket);
			return RespBean.success();
		}
		return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
	}
}
