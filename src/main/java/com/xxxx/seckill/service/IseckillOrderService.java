package com.xxxx.seckill.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.seckillOrder;
import com.xxxx.seckill.pojo.User;

public interface IseckillOrderService extends IService<seckillOrder> {

    seckillOrder getOne(QueryWrapper<seckillOrder> eq);

    //获取秒杀结果，成功 ；-1 秒杀失败 ；0 排队中
    Long getResult(User user, Long goodsId);
}
