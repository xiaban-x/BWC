package com.metabubble.BWC.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


@Component
@Slf4j
public class RedisUtils {
    @Autowired
    private static RedisTemplate redisTemplate;
    /**
     * 获取redis中对应key的键值对的剩余有效时间，如果该键值对已失效，返回 -2
     */
    public static Long ttl(String key){

        Long l = null;
        try {
            l = redisTemplate.getExpire(key);
            log.info("redis获取【"+key+"】的剩余时间成功，剩余的时间是：" + l+"秒");
        } catch (Exception e) {
            log.info("redis获取【"+key+"】的剩余时间失败，失败原因：" + e.getMessage());
        }

        return l;
    }


    /**
     * @Title: get
     * @Description: TODO(根据键获取对应的字符串值)
     * @param @param key
     * @param @return    参数
     * @return String    返回类型
     * @throws
     */
    public static String get(String key){

        String value=null;
        try{
            value= (String) redisTemplate.opsForValue().get(key);
            log.info("redis获取值成功，获取 key【"+key+"】的值是："+value);
        }catch(Exception e){
            log.info("redis获取"+key+"的值失败，失败原因：" + e.getMessage());
        }
        return value;
    }


    /**
     * @Title: set
     * @Description: TODO(存储带有效期的字符串键值对)
     * @param @param key
     * @param @param value
     * @param @param seconds 设置超时失效时间，单位为秒
     * @param @return    参数
     * @return Long    如果成功地为该键设置了超时时间，返回 1,否则返回0
     * @throws
     */
    public static void set(String key, String value, Integer seconds) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Long l = null;
        try {
            valueOperations.set(key,value,seconds, TimeUnit.SECONDS);
            log.info("redis设置值成功，key是：" + key + "有效期是：" + seconds + "秒");
        } catch (Exception e) {
            log.info("redis存值失败，失败原因：" + e.getMessage());
        }



    }


    /**
     * @Title: del
     * @Description: TODO(删除键值对)
     * @param @param key
     * @param @return    参数
     * @return Long    返回1表示删除成功，返回0表示删除失败
     * @throws
     */
    public static Boolean del(String key) {
        Boolean l = null;
        try {

            if(key!=null){
                l = redisTemplate.delete(key);
            }
            log.info("redis删值成功，被删除的key是：" + key);
        } catch (Exception e) {
            log.info("redis删值失败，失败原因：" + e.getMessage());
        }

        return l;
    }

}
