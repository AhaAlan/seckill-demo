package com.xxxx.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.mapper.seckillOrderMapper;
import com.xxxx.seckill.pojo.seckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IseckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class seckillOrderServiceImpl extends ServiceImpl<seckillOrderMapper, seckillOrder> implements IseckillOrderService {

    @Autowired
    private seckillOrderMapper seckillOrderMapper;

    @Resource
    private RedisTemplate redisTemplate;


    public seckillOrder getOne(QueryWrapper<seckillOrder> eq) {
        return null;
    }


    @Override
    public Long getResult(User user, Long goodsId) {
        seckillOrder seckillOrder = seckillOrderMapper.selectOne(new QueryWrapper<seckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if (null != seckillOrder) {
            return seckillOrder.getOrderId();
        } else if (redisTemplate.hasKey("isStockEmpty:" + goodsId)) {
            return -1L; //秒杀失败
        } else {
            return 0L;  //排队中
        }
    }


}
