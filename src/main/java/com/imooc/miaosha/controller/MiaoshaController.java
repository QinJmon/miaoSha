package com.imooc.miaosha.controller;

import com.imooc.miaosha.access.AccessLimit;
import com.imooc.miaosha.domain.MiaoshaMessage;
import com.imooc.miaosha.rabbitmq.MQSender;
import com.imooc.miaosha.redis.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.service.OrderService;
import com.imooc.miaosha.vo.GoodsVo;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;

	@Autowired
	MQSender sender;

	private HashMap<Long,Boolean> localOverMap=new HashMap<Long,Boolean>();


	//系统初始化时，将库存加载进缓存,并将秒杀商品的状态标记为false
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		//判断一下商品列表是否为空
		if(goodsList==null){
			return;
		}
		for (GoodsVo goods : goodsList) {
			Integer stockCount = goods.getStockCount();
			redisService.set(GoodsKey.getMiaoshaGoodsStock,""+goods.getId(),stockCount);
			localOverMap.put(goods.getId(),false);
		}

	}


	
	/**
	 * QPS:
	 * 1000 * 10
	 * */

    @RequestMapping(value="/{path}/do_miaosha", method=RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser user,
								   @RequestParam("goodsId")long goodsId,
								   @PathVariable("path")String path) {
    	model.addAttribute("user", user);
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}

    	//校验秒杀path
		boolean check=miaoshaService.checkPath(user,goodsId,path);
		if(!check){
			return Result.error(CodeMsg.REQUEST_ERROR);
		}

    	//先判断一下该秒杀商品的状态(内存标记，减少redis访问)
		Boolean b = localOverMap.get(goodsId);
    	if(b){ //说明缓存中的商品已经减为0
    		return  Result.error(CodeMsg.MIAO_SHA_OVER);
		}

		//收到请求，减少缓存中的库存
		long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
    	if(stock<0){
    		localOverMap.put(goodsId,true);
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		//判断是否已经秒杀到了
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		//入队
		MiaoshaMessage miaoshaMessage=new MiaoshaMessage();
		miaoshaMessage.setUser(user);
		miaoshaMessage.setGoodsId(goodsId);
		sender.sendMiaoshaMessage(miaoshaMessage);
		return Result.success(0);//0代表排队中



    	/*//判断库存
    	GoodsVo goods = goodsService.getGoodsVoById(goodsId);//10个商品，req1 req2
    	int stock = goods.getStockCount();
    	if(stock <= 0) {
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//减库存 下订单 写入秒杀订单
    	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
        return Result.success(orderInfo);*/
    }

	/*
	 * 返回orderId ：成功
	 * -1：秒杀失败
	 * 0：排队中
	 * */
	@RequestMapping(value="/result", method=RequestMethod.GET)
	@ResponseBody
	public Result<Long> miaoshaResult(Model model,MiaoshaUser user,
									  @RequestParam("goodsId")long goodsId) {
		model.addAttribute("user", user);
		if (user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//获取秒杀结果
		long result=miaoshaService.getMiaoshaResult(user.getId(), goodsId);
		return Result.success(result);

	}

	@AccessLimit(seconds = 10,maxCount = 5,needLogin = true)
	@RequestMapping(value="/path", method=RequestMethod.GET)
	@ResponseBody
	public Result<String> getmiaoshaPath(HttpServletRequest request,MiaoshaUser user,
										 @RequestParam("goodsId")long goodsId,
										 @RequestParam(value = "verifyCode",defaultValue = "0")int verifyCode) {
		if (user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}

		//判断验证码
		boolean check=miaoshaService.checkverifyCode(user,goodsId,verifyCode);
		if(!check){
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}
		//生成秒杀path
		String path= miaoshaService.setmiaoshaPath(user,goodsId);

		return Result.success(path);
	}

	@RequestMapping(value="/verifyCode", method=RequestMethod.GET)
	@ResponseBody
	public Result<String> getmiaoshaVerifyCode(HttpServletResponse response, MiaoshaUser user,
											   @RequestParam("goodsId")long goodsId) {
		if (user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}


		try {
			//创建验证码
			BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
			//将验证码以流输出
			ServletOutputStream out = response.getOutputStream();
			ImageIO.write(image,"JPEG",out);
			out.flush();
			out.close();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return Result.error(CodeMsg.VERIFYCODE_FAIL);
		}


	}
}
