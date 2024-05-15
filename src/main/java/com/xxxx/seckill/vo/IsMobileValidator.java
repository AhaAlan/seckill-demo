package com.xxxx.seckill.vo;

import com.xxxx.seckill.utils.ValidatorUtil;
import com.xxxx.seckill.validator.IsMobile;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 编写校验器类——自定义注解的必要元素，传入注解 @Constraint（）中
 * 主要内容：手机号码校验的主要流程
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {

	private boolean required = false;

	@Override
	public void initialize(IsMobile constraintAnnotation) {
		required = constraintAnnotation.required();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		//如果手机号是必填的
		if (required){
			return ValidatorUtil.isMobile(value);	//这里的ValidatorUtil是自己编写的工具类，直接拿来用
		}else {
			if (StringUtils.isEmpty(value)){
				return true;
			}else {
				return ValidatorUtil.isMobile(value);
			}
		}
	}
}