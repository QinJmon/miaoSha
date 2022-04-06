package com.imooc.miaosha.service;

import com.imooc.miaosha.dao.OrderDao;
import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.redis.OrderKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {

    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    //去缓存中取订单
    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(Long userId, long goodsId) {
     return redisService.get(OrderKey.getMiaoshaOrderByUidGid,""+userId+"_"+goodsId,MiaoshaOrder.class);
       // return orderDao.getMiaoshaOrderByUserIdGoodsId(userId,goodsId);
    }
    public OrderInfo getOrderById(long orderId){
        return orderDao.getOrderById(orderId);
    }

    //生成订单和秒杀订单(主键都是自增的)，原子操作
    @Transactional
    public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {
        OrderInfo orderInfo=new OrderInfo(); //生成订单对象
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getMiaoshaPrice()); //注意这里设置的是秒杀的价格
        orderInfo.setOrderChannel(1); //1pc,2android,3ios
        orderInfo.setUserId(user.getId());
        orderInfo.setStatus(0);//0新建未支付,1已支付, 2已发货, 3已收货,4已退款, 5已完成
        //将订单插入数据区库，并返回订单id
        orderDao.insert(orderInfo);


        //创建秒杀订单对象
        MiaoshaOrder miaoshaOrder=new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setOrderId(orderInfo.getId());
        miaoshaOrder.setUserId(user.getId());
        //将秒杀订单插入数据库，不返回
        orderDao.insertMiaoshaOrder(miaoshaOrder);

        //将订单写入缓存
        redisService.set(OrderKey.getMiaoshaOrderByUidGid,""+user.getId()+"_"+goods.getId(),miaoshaOrder);

        //返回订单对象
        return orderInfo;




    }
}
