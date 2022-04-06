package com.imooc.miaosha.redis;

public class GoodsKey extends BasePrefix{



	private GoodsKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	public static GoodsKey getGoodsList = new GoodsKey(60, "gl"); //商品列表存放缓存的有效时间和前缀
	public static GoodsKey getGoodsDetail = new GoodsKey(60, "gd");//商品详情的
	public static KeyPrefix getMiaoshaGoodsStock=new GoodsKey(0,"gs");//秒杀商品的库存
}
