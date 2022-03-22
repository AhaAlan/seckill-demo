package com.xxxx.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公共返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespBean {
	//状态码
	private long code;
	//消息
	private String message;
	//返回可能带对象
	private Object obj;	//注意这里的命名，和传回ajax中的值相关

	/**
	 * 功能描述: 成功返回结果
	 */
	public static RespBean success(){
		return new RespBean(RespBeanEnum.SUCCESS.getCode(),RespBeanEnum.SUCCESS.getMessage(),null);
	}

	/**
	 * 功能描述: 成功返回结果
	 * 重载，带返回对象
	 */
	public static RespBean success(Object obj){
		return new RespBean(RespBeanEnum.SUCCESS.getCode(),RespBean.success().getMessage(),obj);
	}


	/**
	 * 功能描述: 失败返回结果
	 */
	public static RespBean error(RespBeanEnum respBeanEnum){
		return new RespBean(respBeanEnum.getCode(),respBeanEnum.getMessage(),null);
	}


	/**
	 * 功能描述: 失败返回结果
	 */
	public static RespBean error(RespBeanEnum respBeanEnum,Object obj){
		return new RespBean(respBeanEnum.getCode(),respBeanEnum.getMessage(),obj);
	}

}