package com.xxxx.seckill.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.SeckillGoods;
import com.xxxx.seckill.pojo.SeckillOrder;


public interface ISecKillGoodsService extends IService<SeckillGoods> {


    SeckillGoods getOne(QueryWrapper<SeckillOrder> eq);
}
