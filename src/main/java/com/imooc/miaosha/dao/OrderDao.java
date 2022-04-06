package com.imooc.miaosha.dao;

import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.OrderInfo;
import org.apache.ibatis.annotations.*;
/*
@SelectKey
* 通过LAST_INSERT_ID() 获得刚插入的自动增长的id的值
* keyProperty属性：填入将会被更新的参数对象的属性的值。
before属性：填入 true 或 false 以指明 SQL 语句应被在插入语句的之前还是之后执行。
resultType属性：填入 keyProperty 的 Java 类型。
*/
@Mapper
public interface OrderDao {

    @Select("select * from miaosha_order where user_id=#{userId} and goods_id=#{goodsId}")
    MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(@Param("userId") Long userId, @Param("goodsId")long goodsId);

    @Insert("insert into order_info(id,user_id,goods_id,delivery_addr_id,goods_name,goods_count,goods_price,order_channel,status,create_date)values(" +
            "#{id},#{userId},#{goodsId},#{deliveryAddrId},#{goodsName},#{goodsCount},#{goodsPrice},#{orderChannel},#{status},#{createDate})")
    @SelectKey(keyColumn = "id",keyProperty = "id",resultType=long.class, before=false,statement="select last_insert_id()" )
    long insert(OrderInfo orderInfo);

    @Insert("insert into miaosha_order(id,user_id,order_id,goods_id)values(#{id},#{userId},#{orderId},#{goodsId})")
    void insertMiaoshaOrder(MiaoshaOrder miaoshaOrder);

    @Select("select * from order_info where id=#{orderId}")
    OrderInfo getOrderById(long orderId);
}
