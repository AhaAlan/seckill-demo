package com.xxxx.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.mapper.seckillGoodsMapper;
import com.xxxx.seckill.pojo.seckillGoods;
import com.xxxx.seckill.pojo.seckillOrder;
import com.xxxx.seckill.service.IseckillGoodsService;
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
public class seckillGoodsServiceImpl extends ServiceImpl<seckillGoodsMapper, seckillGoods> implements IseckillGoodsService {


    @Override
    public seckillGoods getOne(QueryWrapper<seckillOrder> eq) {
        return null;
    }
}
