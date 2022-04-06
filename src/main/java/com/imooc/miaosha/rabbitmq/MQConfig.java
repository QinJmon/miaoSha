package com.imooc.miaosha.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MQConfig {

    public static final String QUEUE="queue";
    public static final String MIAOSHA_QUEUE="miaosha.queue";
    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String HEADER_QUEUE = "header.queue";
    public static  final String TOPIC_EXCHANGE="TopicExchange";
    public static final String FANOUT_EXCHANGE = "fanoutExchange";
    public static final String HEADER_EXCHANGE = "headerExchange";


   /* @Bean
    public Queue queue(){
        return new Queue(MIAOSHA_QUEUE,true);
    }*/

   /* *//*
     * Direct模式 交换机Exchange
     * *//*
    @Bean
    public Queue queue(){  //看好导的那个包
        return new Queue(QUEUE,true);//（名称，是否要做持久化）
    }

    *//*
    Topic 模式 交换机Exchange
    *//*
    @Bean
    public Queue topicQueue1() {
        return new Queue(TOPIC_QUEUE1, true);
    }
    @Bean
    public Queue topicQueue2() {
        return new Queue(TOPIC_QUEUE2, true);
    }
    @Bean
    public TopicExchange topicExchage(){
        return new TopicExchange(TOPIC_EXCHANGE);
    }
    @Bean
    public Binding topicBinding1() {
        return BindingBuilder.bind(topicQueue1()).to(topicExchage()).with("topic.key1");
    }
    @Bean
    public Binding topicBinding2() {
        return BindingBuilder.bind(topicQueue2()).to(topicExchage()).with("topic.#");
    }

    *//*
    * Fanout模式  交换机Exchange 广播模式
    * *//*
    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(FANOUT_EXCHANGE);
    }
    //绑定队列
    @Bean
    public Binding FanoutBinding1(){
        return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
    }
    @Bean
    public Binding FanoutBinding2(){
        return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
    }
    *//*
    Header模式  交换机
    *//*
    @Bean
    public HeadersExchange headersExchange(){
        return new HeadersExchange(HEADER_EXCHANGE);
    }
    //创建queue
    @Bean
    public Queue headerQueue(){
        return new Queue(HEADER_QUEUE,true);
    }
    //绑定
    @Bean
    public Binding headerBinding(){
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("header1","v1");
        map.put("h2","v2");
        return BindingBuilder.bind(headerQueue()).to(headersExchange()).whereAll(map).match();
    }
*/

}
