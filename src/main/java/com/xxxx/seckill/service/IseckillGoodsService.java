package com.xxxx.seckill.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.seckillGoods;
import com.xxxx.seckill.pojo.seckillOrder;


public interface IseckillGoodsService extends IService<seckillGoods> {


    seckillGoods getOne(QueryWrapper<seckillOrder> eq);
}
