package com.imooc.miaosha.access;

import com.imooc.miaosha.domain.MiaoshaUser;

/*在拦截器中将获取的用户存在这个类的TreadLocal里
threalocal干嘛的？多线程的时候，保护线程安全。
当前线程绑定，往threadlocal放东西，放的是当前线程里面。
* */
public class UserContext {
    private static ThreadLocal<MiaoshaUser> userHolder=new ThreadLocal<>();

    //将用户存到当前线程
    public static void setUser(MiaoshaUser user){
        userHolder.set(user);
    }

    public static MiaoshaUser getUser(){
        return userHolder.get();
    }
}
