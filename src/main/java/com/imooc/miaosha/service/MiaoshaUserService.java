package com.imooc.miaosha.service;

import com.imooc.miaosha.dao.MiaoshaUserDao;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.exception.GlobalException;
import com.imooc.miaosha.redis.MiaoshaUserKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.util.MD5Util;
import com.imooc.miaosha.util.UUIDUtil;
import com.imooc.miaosha.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {

    public static final String COOKI_NAME_TOKEN = "token";

   @Autowired
   MiaoshaUserDao miaoshaUserDao;

    @Autowired
    RedisService redisService;

    //查对象现在缓存中取，没有就在数据库中取，并加载到缓存中
    public MiaoshaUser getById(long id){
        //取缓存
        MiaoshaUser user=redisService.get(MiaoshaUserKey.getById,""+id,MiaoshaUser.class);
        if(user!=null){
            return user;
        }
        //取数据库
        user=miaoshaUserDao.getById(id);
        if (user!=null){  //判断用户是否存在
            redisService.set(MiaoshaUserKey.getById,""+id,user); //将用户写入缓存
        }
        return user;
    }

    //当用户更新密码时，要注意更新数据库后，处理缓存
    public boolean updatePassword(String token,long id,String formPass){
        //根据id取user
        MiaoshaUser user=getById(id);
        if(user==null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);  //用户不存在
        }

        //更新数据库
        //新建对象去更新，为了提高效率，想更新那个字段就只更新那一个字段，不要全部更新。
        MiaoshaUser toBeUpdate=new MiaoshaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass,user.getSalt()));
        miaoshaUserDao.update(toBeUpdate);

        //处理缓存
        //将gtById缓存删除,将token更新，不能删除，删除就不能登录了
        redisService.delete(MiaoshaUserKey.getById,""+id);
        user.setPassword(toBeUpdate.getPassword()); //更新缓存中的密码
        redisService.set(MiaoshaUserKey.token,token,user);

        return true;

    }

    //业务层遇到异常就抛出
    public String login(HttpServletResponse response, LoginVo loginVo) {
        if(loginVo==null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断手机号是否存在
        MiaoshaUser user = miaoshaUserDao.getById(Long.parseLong(mobile));
        if (user==null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        //计算出来的密码
        String calcPass = MD5Util.formPassToDBPass(formPass,saltDB);
        if(!calcPass.equals(dbPass)){
          throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token= UUIDUtil.uuid();
        //cookie写到response，session写到redis
        addCookie(response,token,user);
        return token;
    }

    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        redisService.set(MiaoshaUserKey.token,token,user);
        Cookie cookie=new Cookie(COOKI_NAME_TOKEN,token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public MiaoshaUser getByToken(HttpServletResponse response,String token) {
        if(StringUtils.isEmpty(token)){
            return null;
        }

        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        //延长有效期
        if(user==null){
            addCookie(response,token,user);
        }

        return  user;
    }
}
