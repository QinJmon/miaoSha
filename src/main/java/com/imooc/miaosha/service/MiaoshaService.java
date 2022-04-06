package com.imooc.miaosha.service;

import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.redis.MiaoshaKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.util.MD5Util;
import com.imooc.miaosha.util.UUIDUtil;
import com.imooc.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class MiaoshaService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    RedisService redisService;

    /***
    *@Description 1、减库存  2、下订单  3、写入秒杀订单
    *@Param
    *@Return
    */
    @Transactional //原子操作，要加上事务注解
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
        if(goodsService.reduceStock(goods)==0){//如果这条减库存的SQL执行失败返回0，那么就直接返回，不要再执行下面的下订单了。
            //减库存失败做标记
            setGoodsOver(goods.getId());
            return null;
        }else{ //减库存成功
            return orderService.createOrder(user, goods);
        }


    }

    private void setGoodsOver(Long goodsId) {
        //向缓存中添加一个key，如果存在这个key说明是因为减库存失败级库存不足而下单失败
        redisService.set(MiaoshaKey.isGoodsOver,""+goodsId,true);

    }

    public boolean getGoodsOver(Long goodsId){
        //判断缓存中是否存在key
         return  redisService.exists(MiaoshaKey.isGoodsOver,""+goodsId);
    }

    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     * */
    public long getMiaoshaResult(Long userId, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        if(order==null){
            //如果订单为空，有两种状态，排队和库存不足导致的失败，根据标记状态来判断
            boolean isOver = getGoodsOver(goodsId);
            if(isOver){
                return -1;
            }else{
                return 0;
            }
        }else {
            return order.getOrderId();
        }
    }

    public String setmiaoshaPath(MiaoshaUser user, long goodsId) {
        if(user==null|| goodsId<=0){
            return null;
        }
       String path= MD5Util.md5(UUIDUtil.uuid()+"123456");
        //将秒杀path存到redis
        redisService.set(MiaoshaKey.getMiaoshaPath,""+user.getId()+"_"+goodsId,path);
       return path;
    }


    public boolean checkPath(MiaoshaUser user, long goodsId,String path) {
        if(path==null || user==null){
            return false;
        }
        //从redis里面根据key取path
        String str = redisService.get(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, String.class);
        return path.equals(str);

    }

    public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) {
        if(user == null || goodsId <=0) {
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd=calc(verifyCode);//计算出来的结果
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode,user.getId()+","+goodsId, rnd);
        //输出图片
        return image;

    }
    public boolean checkverifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
        if(user == null || goodsId <=0) {
            return false;
        }

        //从缓存中取验证码和输入的比较
        Integer OldCode = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId() + "," + goodsId, Integer.class);
        if(OldCode==null || OldCode-verifyCode!=0){
            return false;
        }
        //验证之后，将缓存中的验证码删除
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode,user.getId() + "," + goodsId);
        return true;
    }

  /*  public static void main(String[] args) {
        int calc = calc("2+4-1");
        System.out.println(calc);
    }*/
    //计算表达式的值
    private static int calc(String exp) {
        try{
            ScriptEngineManager manager=new ScriptEngineManager();
            ScriptEngine engine=manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //操作符数组
    private static char[] ops=new char[]{'+','-','*'};
    //生成三个随机数和+，-，*操作符
    private String generateVerifyCode(Random rdm) {
        int num1=rdm.nextInt(10);
        int num2=rdm.nextInt(10);
        int num3=rdm.nextInt(10);
        char op1=ops[rdm.nextInt(3)];
        char op2=ops[rdm.nextInt(3)];
        String exp=""+num1+op1+num2+op2+num3;
        return exp;
    }


}
