package com.xxxx.seckill.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;

public interface ISecKillOrderService extends IService<SeckillOrder> {

    SeckillOrder getOne(QueryWrapper<SeckillOrder> eq);

    Long getResult(User user, Long goodsId);
}
