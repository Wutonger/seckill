package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;


public class RedisDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;

    private RuntimeSchema<Seckill>  runtimeSchema= RuntimeSchema.createFrom(Seckill.class);
    public RedisDao(String ip,int port){
        jedisPool = new JedisPool(ip ,port);
    }

    public Seckill getSeckill(long seckillId){
        try {
            Jedis jedis = jedisPool.getResource();
            String key = "seckill:"+seckillId;
            //redis内部没有实现反序列化
            //采用自定义序列化机制
            byte[] bytes = jedis.get(key.getBytes());
            if (bytes!=null){
                Seckill seckill = runtimeSchema.newMessage();
                //反序列化Seckill
                ProtostuffIOUtil.mergeFrom(bytes,seckill,runtimeSchema);
                jedis.close();
                return seckill;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public String putSeckill(Seckill seckill){
        try{
            Jedis jedis = jedisPool.getResource();
            String key = "seckill:"+seckill.getSeckillId();
            //序列化seckill对象
            byte[] bytes = ProtostuffIOUtil.toByteArray(seckill,runtimeSchema,
                    LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            int timeout = 60 * 60;   //缓存一个小时
            String result = jedis.setex(key.getBytes(),timeout,bytes);
            jedis.close();
            return result;
        }catch (Exception e){

        }
        return null;
    }
}
