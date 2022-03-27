package com.xxxx.seckill.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wf.captcha.ArithmeticCaptcha;
import com.xxxx.seckill.config.AccessLimit;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.rabbitmq.MQSender;
import com.xxxx.seckill.pojo.seckillMessage;
import com.xxxx.seckill.pojo.seckillOrder;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.service.IseckillOrderService;
import com.xxxx.seckill.utils.JsonUtil;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *  前端控制器
 */
@Slf4j
@Controller
@RequestMapping("/seckill")
public class seckillController implements InitializingBean {
    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private IseckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    //库存标记，减少对redis的访问，long对应goodsId，Boolean对应是否存在库存
    private Map<Long,Boolean> emptyStockMap = new HashMap<>();

    @Autowired
    private RedisScript<Long> script;


    @AccessLimit(second=5,maxCount=5,needLogin=true)
    @RequestMapping(value="/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request) {
        //健壮性检测
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        //简单接口限流：限制访问次数，5秒内访问5次（最大能承受的QPS的70%-80%）
        //使用计数器，还是可能存在临界问题，比如在59-60s突然涌入大量请求，可能让服务崩溃
        //建议使用令牌桶算法
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        String uri = request.getRequestURI();
//        Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
//        if (count == null) {    //第一次
//            valueOperations.set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);
//        } else if (count < 5) { //判断是否小于5次
//            valueOperations.increment(uri + ":" + user.getId());    //原子性的递增
//        } else {
//            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REAHCED);
//        }

        //校验验证码
        boolean check = orderService.checkCaptcha(user,goodsId,captcha);
        if(!check){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }

        //随机生成秒杀路径
        String str = orderService.createPath(user,goodsId);
        return RespBean.success(str);
    }


    /**
     * 秒杀接口——优化后
     * Windows，优化后QPS吞吐量：2382.7/sec（1000个线程，循环10，重复三次）
     */
    @RequestMapping(value="/{path}/doseckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doseckill(@PathVariable("path") String path, User user, long goodsId){
        //如果用户不存在
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        //优化：使用redis预减库存
        ValueOperations valueOperations = redisTemplate.opsForValue();

        //秒杀接口路径校验
        boolean check = orderService.checkPath(user,goodsId,path);
        if(!check){

            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        //判断是否重复抢购
        seckillOrder one = (seckillOrder)redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if(one!=null){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);  //跳转到秒杀失败页面
        }
        //内存标记，减少对redis的访问
        if(emptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        //递减库存操作
        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);//原子性操作
//        //再优化：利用redis实现分布式锁，使用lua脚本
//        Long stock =(Long)redisTemplate.execute(script, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if(stock<0){
            emptyStockMap.put(goodsId,true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        seckillMessage seckillMessage = new seckillMessage(user, goodsId);
        mqSender.sendseckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0); //0表示排队中


        /*
        //原始代码
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        //如果库存为0
        if(goodsVo.getStockCount()<1){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);  //跳转到秒杀失败页面
        }
        //判断是否重复抢购
        seckillOrder one = (seckillOrder)redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);

        if(one!=null){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);  //跳转到秒杀失败页面
        }

        //满足所有判断条件,开始秒杀
        Order order = orderService.seckill(user,goodsVo);
        return RespBean.success(order);
        */
    }


    /**
     * 秒杀接口——秒杀静态化页面优化前
     * Windows优化前QPS：吞吐量944.9/sec（1000个线程，循环10，重复三次）
     */
    @RequestMapping(value = "/doseckill2", method = RequestMethod.POST)
    public String doseckill2(Model model, User user, long goodsId){
        //如果用户不存在
        if(user==null){
            return "login";
        }
        model.addAttribute("user",user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);

        //如果库存为0
        if(goodsVo.getStockCount()<1){
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "seckillFail";   //跳转到秒杀失败页面
        }
        //判断是否重复抢购
        seckillOrder one = seckillOrderService.getOne(new QueryWrapper<seckillOrder>().eq("user_id", user.getId()).eq("good_id", goodsId));
        if(one!=null){
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return "seckillFail";   //跳转到秒杀失败页面
        }

        //满足所有判断条件,开始秒杀
        Order order = orderService.seckill(user,goodsVo);
        model.addAttribute("order",order);
        model.addAttribute("goods",goodsVo);
        return "orderDetail";
    }


    /**
     *  获取验证码
     */
    @GetMapping(value = "/captcha")
    public void verifyCode(User user, Long goodsId, HttpServletResponse response) {
        if (user == null || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //设置请求头为输出图片的类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam", "No-cache");   //不设置缓存，没次都是新的验证码
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);   //失效时间
        //生成验证码（算术类型）
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        //验证码放在redis里面
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }
    }

    /**
     * 初始化
     * 将商品库存加载到redis中去
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:"+goodsVo.getId(),goodsVo.getStockCount());
            emptyStockMap.put(goodsVo.getId(),false);
            }
        );
    }

    /**
     * 获取秒杀结果
     * @return orderId 成功 ；-1 秒杀失败 ；0 排队中
     **/
    @RequestMapping(value="/result",method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user,Long goodsId){
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

}
