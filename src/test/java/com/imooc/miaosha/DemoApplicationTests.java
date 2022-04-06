package com.imooc.miaosha;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() throws SQLException {
        //查看当前默认数据源
        System.out.println(dataSource.getClass());  //class com.zaxxer.hikari.HikariDataSource

        //查看数据库连接
        Connection connection = dataSource.getConnection();
        System.out.println(connection);   //HikariProxyConnection@348084146 wrapping com.mysql.cj.jdbc.ConnectionImpl@46d9aec8

        //关闭
        connection.close();

    }

    @Test
    void contextLoads1(){
        /*Jedis jedis=new Jedis("192.168.43.238",6379);
        System.out.println(jedis.ping());*/
    }

}
