package com.imooc.miaosha.access;

import com.alibaba.fastjson.JSON;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.redis.AccessKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.MiaoshaUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class AccessInterceptor implements HandlerInterceptor {

    @Autowired
    MiaoshaUserService userService;


    @Autowired
    RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            //先获取用户
            MiaoshaUser user=getUser(request,response);
            //将获取的用户存起来，方便后面的调用传递
            UserContext.setUser(user);

            HandlerMethod hm=(HandlerMethod)handler;
            //获取方法上的注解
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit==null){
                return true; //没有注解
            }
            //有注解获取注解的参数
            int seconds=accessLimit.seconds();
            int maxCount=accessLimit.maxCount();
            boolean needLogin=accessLimit.needLogin();
            //获取key
            String key=request.getRequestURI();
            //如果需要登陆
            if(needLogin){
                if(user==null){
                    //提示错误信息
                    render(response,CodeMsg.SESSION_ERROR);
                    return false;
                }
                //key需要加上用户id
                key+="_"+user.getId();
            }else {
                //如果不需要登陆什么都不做
            }
            //查询访问次数
            AccessKey ak= AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak, key, Integer.class);
            if(count==null){
                //说明是第一次访问
                redisService.set(ak,key,1);
            }else if(count<maxCount){
                redisService.incr(ak,key);
            }else {
                //大于次数
               render(response,CodeMsg.ACCESS_ERROR);
                return false;
            }
        }

        return true;

    }

    private void render(HttpServletResponse response, CodeMsg codeMsg) throws IOException {
        //防止客户端乱码
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out=response.getOutputStream();
        String str = JSON.toJSONString(Result.error(codeMsg));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
        //分别从请求参数中和cookie中获得token，两种方式只有一个有token即可
        String paramToken = request.getParameter(MiaoshaUserService.COOKI_NAME_TOKEN);
        String cookieToken = getCookieValue(request,MiaoshaUserService.COOKI_NAME_TOKEN);
        //但是两者不能都为空
        if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
            return null;
        }
        String token=StringUtils.isEmpty(paramToken)?cookieToken:paramToken;

        return userService.getByToken(response,token);
    }
    private String getCookieValue(HttpServletRequest request, String cookiNameToken) {
        Cookie[] cookies= request.getCookies();  //请求中的cookie是一个数组
        //如果cookie是空
        if(cookies==null || cookies.length<=0){
            return null;
        }
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals(cookiNameToken)){
                return  cookie.getValue();
            }
        }

        return null;
    }
}
