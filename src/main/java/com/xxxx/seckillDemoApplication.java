package com.xxxx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xxxx.seckill.mapper") //扫描mapper文件夹
public class seckillDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(seckillDemoApplication.class, args);
	}

}
