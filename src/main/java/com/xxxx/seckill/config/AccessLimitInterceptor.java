package com.xxxx.seckill.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.utils.CookieUtil;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流
 * 通过拦截器实现
 */
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {
	@Autowired
	private IUserService userService;
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {
			User user = getUser(request, response);	//获取当前用户
			UserContext.setUser(user);
			HandlerMethod hm = (HandlerMethod) handler;
			AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
			//如果没有@AccessLimit，直接继续运行
			if (accessLimit == null) {
				return true;
			}
			//否则，需要获取参数信息
			int second = accessLimit.second();
			int maxCount = accessLimit.maxCount();
			boolean needLogin = accessLimit.needLogin();

			String key = request.getRequestURI();
			//判断是否需要登录
			if (needLogin) {
				if (user == null) {	//这个用户是从threadlocal中拿出来的
					render(response, RespBeanEnum.SESSION_ERROR);
					return false;
				}
				key += ":" + user.getId();
			}
			ValueOperations valueOperations = redisTemplate.opsForValue();
			Integer count = (Integer) valueOperations.get(key);
			if (count == null) {
				valueOperations.set(key, 1, second, TimeUnit.SECONDS);
			} else if (count < maxCount) {
				valueOperations.increment(key);
			} else {
				render(response, RespBeanEnum.ACCESS_LIMIT_REAHCED);
				return false;
			}
		}
		return true;
	}


	/**
	 * 功能描述: 构建返回对象
	 */
	private void render(HttpServletResponse response, RespBeanEnum respBeanEnum) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");	//设置编码格式
		PrintWriter out = response.getWriter();
		RespBean respBean = RespBean.error(respBeanEnum);
		out.write(new ObjectMapper().writeValueAsString(respBean));
		out.flush();
		out.close();
	}

	/**
	 * 功能描述: 获取当前登录用户
	 */
	private User getUser(HttpServletRequest request, HttpServletResponse response) {
		String ticket = CookieUtil.getCookieValue(request, "userTicket");
		if (StringUtils.isEmpty(ticket)) {
			return null;
		}
		return userService.getUserByCookie(ticket, request, response);
	}
}
