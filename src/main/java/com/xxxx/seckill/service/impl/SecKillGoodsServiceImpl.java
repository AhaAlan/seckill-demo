package com.xxxx.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.mapper.SeckillGoodsMapper;
import com.xxxx.seckill.pojo.SeckillGoods;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.service.ISecKillGoodsService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 * 乐字节：专注线上IT培训
 * 答疑老师微信：lezijie
 *
 * @author zhoubin
 *
 */
@Service
public class SecKillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements ISecKillGoodsService {


    @Override
    public SeckillGoods getOne(QueryWrapper<SeckillOrder> eq) {
        return null;
    }
}
