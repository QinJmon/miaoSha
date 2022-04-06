package com.imooc.miaosha.service;

import com.imooc.miaosha.dao.GoodsDao;
import com.imooc.miaosha.domain.MiaoshaGoods;
import com.imooc.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class GoodsService {

    @Autowired
    GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVo(){
       return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoById(long id) {
        return goodsDao.getGoodsVoById(id);
    }

    public int reduceStock(GoodsVo goods) {
        MiaoshaGoods g = new MiaoshaGoods();
        g.setGoodsId(goods.getId());
        //flag标记这条SQL是否执行成功。0不成功，1成功。
        int flag=goodsDao.reduceStock(g);
        return flag;

    }
}
