package com.xxxx.seckill.exception;

import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * 全局异常处理类
 * @ RestControllerAdvice是对 Controller进行增强的，可以全局捕获spring mvc抛的异常。
 * @ ExceptionHandler的作用是用来捕获指定的异常。
 */
@RestControllerAdvice
public class GlobalExceptionHandler{

	@ExceptionHandler(Exception.class)
	public RespBean ExceptionHandler(Exception e) {
		//如果异常e属于全局异常类的情况
		if (e instanceof GlobalException) {
			GlobalException ex = (GlobalException) e;
			return RespBean.error(ex.getRespBeanEnum());
		}
		//如果异常e是绑定异常
		if(e instanceof BindException) {
			BindException ex = (BindException) e;
			RespBean respBean = RespBean.error(RespBeanEnum.BIND_ERROR);
			respBean.setMessage("参数校验异常：" + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
			return respBean;
		}
		return RespBean.error(RespBeanEnum.ERROR);
	}
}