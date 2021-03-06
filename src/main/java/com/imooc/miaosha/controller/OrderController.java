package com.imooc.miaosha.controller;

import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.OrderService;
import com.imooc.miaosha.vo.GoodsVo;
import com.imooc.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    //,method = RequestMethod.GET
    @RequestMapping(value = "/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, MiaoshaUser user,@RequestParam("orderId") long orderId) {
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
       OrderInfo orderInfo= orderService.getOrderById(orderId);
        if(orderInfo==null){
            return Result.error(CodeMsg.ORDER_ERROR);
        }
        Long goodsId = orderInfo.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoById(goodsId);
        OrderDetailVo orderDetailVo=new OrderDetailVo();
        orderDetailVo.setGoods(goods);
        orderDetailVo.setOrder(orderInfo);

        return Result.success(orderDetailVo);

    }



}
