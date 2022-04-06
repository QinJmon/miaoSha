package com.imooc.miaosha.vo;

import com.imooc.miaosha.domain.MiaoshaUser;

/*
* 用来传递页面的值
* */
public class GoodsDetailVo {
    //这些值都是要传递给前端的，封装起来
    private  int miaoshaStatus = 0;
    private  int remainSeconds = 0;//秒杀剩余时间
    private GoodsVo goods;
    private MiaoshaUser user;

    public int getMiaoshaStatus() {
        return miaoshaStatus;
    }

    public void setMiaoshaStatus(int miaoshaStatus) {
        this.miaoshaStatus = miaoshaStatus;
    }

    public int getRemainSeconds() {
        return remainSeconds;
    }

    public void setRemainSeconds(int remainSeconds) {
        this.remainSeconds = remainSeconds;
    }

    public GoodsVo getGoods() {
        return goods;
    }

    public void setGoods(GoodsVo goods) {
        this.goods = goods;
    }

    public MiaoshaUser getUser() {
        return user;
    }

    public void setUser(MiaoshaUser user) {
        this.user = user;
    }
}
