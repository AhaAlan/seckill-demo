package com.xxxx.seckill.controller;

import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.vo.DetailVo;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 商品
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private ThymeleafViewResolver thymeleafViewResolver;

	/**
	 * 功能描述: 跳转商品列表页
	 * windows优化前QPS:吞吐量861.7/sec（5000个线程，循环10次，测了3次）
	 * windows优化后QPS：4859.6/sec（5000个线程，循环10次，测了3次）
	 * 优化一：对商品列表页面进行静态化，利用redis
	 */
	@RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
	@ResponseBody
	public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response) {
		//Redis中获取页面，如果不为空，直接返回页面
		ValueOperations valueOperations = redisTemplate.opsForValue();
		String html = (String) valueOperations.get("goodsList");
		if (!StringUtils.isEmpty(html)) {
			return html;
		}

		//参数校验已经被提取出去了，直接传入一个经过校验的user
		model.addAttribute("user", user);
		model.addAttribute("goodsList", goodsService.findGoodsVo());

		//如果为空，手动渲染，存入Redis并返回
		WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
		//thymeleafViewResolver模板引擎，用于手动渲染
		html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
		if (!StringUtils.isEmpty(html)) {	//如果不为空，说明渲染成功
			valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);	//timeout失效时间
		}
		return html;
	}


	/**
	 * 功能描述: 跳转商品详情页
	 * 优化一：url缓存，利用redis
	 */
	@RequestMapping(value = "/toDetail0/{goodsId}", produces = "text/html;charset=utf-8")
	@ResponseBody
	public String toDetail0(Model model, User user, @PathVariable Long goodsId,HttpServletRequest request, HttpServletResponse response) {
		//Redis中获取页面，如果不为空，直接返回页面
		ValueOperations valueOperations = redisTemplate.opsForValue();
		String html = (String) valueOperations.get("goodsDetail:" + goodsId);
		if (!StringUtils.isEmpty(html)) {
			return html;
		}

		model.addAttribute("user", user);
		GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods",goodsVo);
		Date startDate = goodsVo.getStartDate();	//获取秒杀开始时间
		Date endDate = goodsVo.getEndDate();		//获取秒杀结束时间
		Date nowDate = new Date();					//获取当前时间
		int seckillStatus = 0;		//秒杀状态
		int remainSeconds;		//秒杀倒计时
		if (nowDate.before(startDate)) {		//秒杀还未开始
			remainSeconds = ((int) ((startDate.getTime() - nowDate.getTime()) / 1000));
		} else if (nowDate.after(endDate)) {	//	秒杀已结束
			seckillStatus = 2;
			remainSeconds = -1;
		} else {								//秒杀中
			seckillStatus = 1;
			remainSeconds = 0;
		}
		model.addAttribute("remainSeconds", remainSeconds);
		model.addAttribute("seckillStatus", seckillStatus);
		model.addAttribute("goods", goodsVo);


		//如果为空，手动渲染，存入Redis并返回
		WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
		html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
		if (!StringUtils.isEmpty(html)) {
			valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);
		}
		return html;	//传输给前端的时候，是整个页面，数据量较大，可以把页面中的静态部分和变化的数据拆分出来
	}


	/**
	 * 功能描述: 跳转商品详情页
	 * 优化二：传输给前端的时候，是整个页面，数据量较大，可以把页面中的静态部分和变化的数据拆分出来
	 * 后端返回的仅是一个detailVo对象，而不是整个页面了
	 */
	@RequestMapping(value = "/detail/{goodsId}")
	@ResponseBody
	public RespBean toDetail(Model model, User user, @PathVariable Long goodsId) {
		GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
		Date startDate = goodsVo.getStartDate();	//获取秒杀开始时间
		Date endDate = goodsVo.getEndDate();		//获取秒杀结束时间
		Date nowDate = new Date();					//获取当前时间

		int seckillStatus = 0;		//秒杀状态
		int remainSeconds;		//秒杀倒计时
		if (nowDate.before(startDate)) {		//秒杀还未开始
			remainSeconds = ((int) ((startDate.getTime() - nowDate.getTime()) / 1000));
		} else if (nowDate.after(endDate)) {	//	秒杀已结束
			seckillStatus = 2;
			remainSeconds = -1;
		} else {								//秒杀中
			seckillStatus = 1;
			remainSeconds = 0;
		}

		DetailVo detailVo = new DetailVo();
		detailVo.setUser(user);
		detailVo.setGoodsVo(goodsVo);
		detailVo.setSeckillStatus(seckillStatus);
		detailVo.setRemainSeconds(remainSeconds);
		return RespBean.success(detailVo);
	}

}