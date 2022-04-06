package com.imooc.miaosha.controller;

import com.imooc.miaosha.domain.User;
import com.imooc.miaosha.rabbitmq.MQSender;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.redis.UserKey;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@Controller
@RequestMapping("/demo")
public class hellocontroller {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender sender;

   /* @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> mqHeader() {
        sender.sendHeader("jiayou");
        return Result.success("Hello，world");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> mqFanout() {
        sender.sendFanout("jiayou");
        return Result.success("Hello，world");
    }

    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> mqTopic() {
        sender.sendTopic("jiayou");
        return Result.success("Hello，world");
    }

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq() {
		sender.send("jiayou");
        return Result.success("Hello，world");
    }
*/
    @RequestMapping("/hello")
    @ResponseBody
    String hello(){
        return "hello";
    }

    //为了在前端以json指定的字符串输出
    @RequestMapping("/hello1")
    @ResponseBody
    public Result<String> hello1(){
        return  Result.success("success_data");
    }

    @RequestMapping("/hello2")
    @ResponseBody
    public Result<String> hello2(){
        return Result.error(CodeMsg.SERVER_ERROR);
    }


    @RequestMapping("/www")
    public String www(Model model){
        model.addAttribute("name","tom");
        return "hello";
    }


    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
        User  user  = redisService.get(UserKey.getById, ""+1, User.class);
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
        User user  = new User();
        user.setId(1);
        user.setName("1111");
        redisService.set(UserKey.getById, ""+1, user);//UserKey:id1
        return Result.success(true);
    }



}
