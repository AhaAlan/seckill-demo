package com.xxxx.seckill.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * MD5工具类
 */
public class MD5Util {

	public static String md5(String src){
		return DigestUtils.md5Hex(src);
	}

	private static final String salt="1a2b3c4d";

	//第一次加密：客户端到服务端加密
	public static String inputPassToFromPass(String inputPass){
		String str = "" +salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);
		return md5(str);
	}

	//第二次加密：后端密码到数据库
	public static String formPassToDBPass(String formPass,String salt){
		String str = "" +salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);
		return md5(str);
	}

	public static String inputPassToDBPass(String inputPass,String salt){
		String fromPass = inputPassToFromPass(inputPass);
		return formPassToDBPass(fromPass, salt);
	}

	//测试
	public static void main(String[] args) {
		// d3b1294a61a07da9b49b6e22b2cbd7f9
		System.out.println(inputPassToFromPass("123456"));
		System.out.println(formPassToDBPass("d3b1294a61a07da9b49b6e22b2cbd7f9","1a2b3c4d"));
		System.out.println(inputPassToDBPass("123456","1a2b3c4d"));
	}

}