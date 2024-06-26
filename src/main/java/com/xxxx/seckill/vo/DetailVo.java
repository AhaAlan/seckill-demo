package com.xxxx.seckill.vo;

import com.xxxx.seckill.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 详情返回对象
 * 为了页面静态化
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailVo {

	private User user;

	private GoodsVo goodsVo;

	private int seckillStatus;

	private int remainSeconds;
}
