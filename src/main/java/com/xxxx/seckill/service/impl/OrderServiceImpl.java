package com.xxxx.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.mapper.OrderMapper;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.pojo.seckillGoods;
import com.xxxx.seckill.pojo.seckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.service.IseckillGoodsService;
import com.xxxx.seckill.service.IseckillOrderService;
import com.xxxx.seckill.utils.MD5Util;
import com.xxxx.seckill.utils.UUIDUtil;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.OrderDetailVo;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;


@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper,Order> implements IOrderService {

    @Autowired
    private IseckillGoodsService seckillGoodsService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private IseckillOrderService seckillOrderService;

    @Autowired
    private IGoodsService goodsService;


    @Autowired
    private RedisTemplate redisTemplate;

    //解决超卖问题
    @Transactional
    @Override
    public Order seckill(User user, GoodsVo goods) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        seckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<seckillGoods>().eq("goods_id", goods.getId()));
        //库存减1
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
        //原始方法：不能保证超卖
        /*seckillGoodsService.updateById(one);*/

        //优化：不会超卖，但是会出现订单数和库存减少量不匹配
        /*boolean seckillresult = seckillGoodsService.update(new UpdateWrapper<seckillGoods>().set("stock_count", seckillGoods.getStockCount()).eq("id", seckillGoods.getId()).gt("stock_count", 0));*/
        /*if(!seckillresult){
            return null;
        }*/

        //优化：既解决库存超卖问题，又解决订单数问题
        boolean result = seckillGoodsService.update(new UpdateWrapper<seckillGoods>().setSql("stock_count=" + "stock_count-1").eq("goods_id", goods.getId()).gt("stock_count", 0));
        if(seckillGoods.getStockCount()<1){
            //判断是否有库存
            valueOperations.set("isStockEmpty:"+goods.getId(),"0");
            return  null;
        }

        //生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(goods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);

        //生成秒杀订单
        seckillOrder seckillOrder = new seckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(order.getGoodsId());
        seckillOrderService.save(seckillOrder);
        //利用缓存，提高QPS
        redisTemplate.opsForValue().set("order:"+user.getId()+":"+goods.getId(),seckillOrder);
        return order;
    }

    //显示订单详情
    @Override
    public OrderDetailVo detail(Long orderId) {
        if (orderId == null) {
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        //根据订单获取对应商品id
        GoodsVo goodsVobyGoodsId = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        //返回一个orderDetailVo
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setOrder(order);
        orderDetailVo.setGoodsVo(goodsVobyGoodsId);
        return orderDetailVo;
    }

    //获取秒杀地址
    @Override
    public String createPath(User user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        //随机生成的路径，存到redis中
        redisTemplate.opsForValue().set("seckillPath:"+user.getId()+":"+goodsId,str,60, TimeUnit.SECONDS);
        return str;
    }

    //路径校验
    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if (user == null || goodsId < 0 || StringUtils.isEmpty(path)) {
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        return path.equals(redisPath);
    }

    //验证码校验
    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if(captcha==null||user==null||goodsId<0){
            return false;
        }
        String redisCaptcha= (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return captcha.equals(redisCaptcha);
    }
}
