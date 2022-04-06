package com.imooc.miaosha.controller;

import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.redis.GoodsKey;
import com.imooc.miaosha.redis.RedisService;

import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.vo.GoodsDetailVo;
import com.imooc.miaosha.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {


    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;

   @Autowired
    GoodsService goodsService;

   @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

   @Autowired
   ApplicationContext applicationContext;

   /*
   * 1、QPS：138.5
   * 2、QPS：283
   * */
    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response,
                       Model model, MiaoshaUser user) {
        model.addAttribute("user", user);


        //1、先取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if(!StringUtils.isEmpty(html)){
            //缓存不为空
            return html;
        }
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsList);
        //return "goods_list";
        //缓存为空
        IWebContext ctx=new WebContext(request,response,request.getServletContext(),
                request.getLocale(),model.asMap());
        //手动渲染
        html=thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList,"",html); //存入缓存
        }
        return html;
    }

    /*将商品详情页面静态化，前后端分离， 创建一个类用来传递值*/
    @RequestMapping(value="/detail/{id}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response,
                                        Model model, MiaoshaUser user, @PathVariable("id")long goodsId) {
        GoodsVo goodsVo = goodsService.getGoodsVoById(goodsId);
        //查看商品秒杀的状态，是否开始
        long startTime = goodsVo.getStartDate().getTime();
        long endTime = goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        int remainSeconds = 0;//秒杀剩余时间

        if(now < startTime){ //秒杀还未开始
        miaoshaStatus=0;
         remainSeconds=(int)((startTime-now)/1000);

        }else if(now > endTime){ //秒杀结束
            miaoshaStatus=2;
            remainSeconds=-1;
        }else {
            //秒杀进行中
            miaoshaStatus=1;
            remainSeconds=0;
        }
        //将商品的秒杀状态和秒杀剩余时间返回给前台
        GoodsDetailVo vo=new GoodsDetailVo();
        vo.setGoods(goodsVo);
        vo.setUser(user);
        vo.setMiaoshaStatus(miaoshaStatus);
        vo.setRemainSeconds(remainSeconds);

        return Result.success(vo); //将封装好的vo传过去
    }


    @RequestMapping(value = "/to_detail1/{id}",produces = "text/html")
    @ResponseBody
    public String detail1(HttpServletRequest request, HttpServletResponse response,
                         Model model, MiaoshaUser user, @PathVariable("id")long id) {
        model.addAttribute("user", user);

        //取缓存
        String html=redisService.get(GoodsKey.getGoodsDetail,""+id,String.class);
        if(!StringUtils.isEmpty(html)){
            //缓存不为空
            return html;
        }
        //手动渲染
        GoodsVo goodsVo = goodsService.getGoodsVoById(id);
        model.addAttribute("goods",goodsVo);

        //查看商品秒杀的状态，是否开始
        long startTime = goodsVo.getStartDate().getTime();
        long endTime = goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        int remainSeconds = 0;//秒杀剩余时间

        if(now < startTime){ //秒杀还未开始
            miaoshaStatus=0;
            remainSeconds=(int)((startTime-now)/1000);

        }else if(now > endTime){ //秒杀结束
            miaoshaStatus=2;
            remainSeconds=-1;
        }else {
            //秒杀进行中
            miaoshaStatus=1;
            remainSeconds=0;
        }
        //将商品的秒杀状态和秒杀剩余时间返回给前台
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);

        //return "goods_detail";
        IWebContext ctx=new WebContext(request,response,request.getServletContext(),
                request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        if(!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail, ""+id, html);
        }
        return html;
    }
    @RequestMapping(value = "/to_detail2/{id}",produces = "text/html")
    @ResponseBody
    public String detail2(HttpServletRequest request, HttpServletResponse response,
                         Model model, MiaoshaUser user, @PathVariable("id")long id) {
        model.addAttribute("user", user);

        //取缓存
        String html=redisService.get(GoodsKey.getGoodsDetail,""+id,String.class);
        if(!StringUtils.isEmpty(html)){
            //缓存不为空
            return html;
        }
        //手动渲染
        GoodsVo goodsVo = goodsService.getGoodsVoById(id);
        model.addAttribute("goods",goodsVo);

        //查看商品秒杀的状态，是否开始
        long startTime = goodsVo.getStartDate().getTime();
        long endTime = goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        int remainSeconds = 0;//秒杀剩余时间

        if(now < startTime){ //秒杀还未开始
            miaoshaStatus=0;
            remainSeconds=(int)((startTime-now)/1000);

        }else if(now > endTime){ //秒杀结束
            miaoshaStatus=2;
            remainSeconds=-1;
        }else {
            //秒杀进行中
            miaoshaStatus=1;
            remainSeconds=0;
        }
        //将商品的秒杀状态和秒杀剩余时间返回给前台
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);

        //return "goods_detail";
        IWebContext ctx=new WebContext(request,response,request.getServletContext(),
                request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        if(!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail, ""+id, html);
        }
        return html;
    }
}
