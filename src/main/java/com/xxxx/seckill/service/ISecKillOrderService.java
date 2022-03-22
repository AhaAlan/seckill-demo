package com.xxxx.seckill.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;

public interface ISecKillOrderService extends IService<SeckillOrder> {

    SeckillOrder getOne(QueryWrapper<SeckillOrder> eq);

    //获取秒杀结果，成功 ；-1 秒杀失败 ；0 排队中
    Long getResult(User user, Long goodsId);
}
