package com.xxxx.seckill.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参数校验工具类
 * 主要针对手机号码的格式进行校验
 */
public class ValidatorUtil {
	//正则表达式，手机号码的格式
	private static final Pattern mobile_pattern = Pattern.compile("[1]([3-9])[0-9]{9}$");
	//判断是否为手机号
	public static boolean isMobile(String mobile){
		if (StringUtils.isEmpty(mobile)){
			return false;
		}
		Matcher matcher = mobile_pattern.matcher(mobile);
		return matcher.matches();
	}
}
