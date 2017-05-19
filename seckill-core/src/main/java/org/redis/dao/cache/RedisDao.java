package org.redis.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.redis.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by Admin on 2017/5/19.
 * Redis操作Seckill
 */
public class RedisDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public RedisDao(String ip, int port) {
        jedisPool = new JedisPool(ip, port);
    }

    /**
     * 获取redis中秒杀库存实体.
     *
     * @param seckillId 秒杀库存表主键
     * @return redis中对应的秒杀库存
     */
    public Seckill getSeckill(long seckillId) {
        //redis操作逻辑
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String key = "seckill:" + seckillId;
            // 并没有实现内部序列化操作
            // get -> byte[] -> 反序列化 -> object[Seckill]
            // 采用自定义序列化
            // protostuff : pojo.
            byte[] bytes = jedis.get(key.getBytes());
            if (bytes != null) {
                Seckill seckill = schema.newMessage();
                ProtobufIOUtil.mergeFrom(bytes, seckill, schema);
                // seckill被反序列化
                return seckill;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 把秒杀库存存入redis
     *
     * @param seckill 待存入redis的秒杀库存
     * @return 返回存入redis的结果
     */
    public String putSeckill(Seckill seckill) {
        // set Object(Seckill) -> 序列号 -> byte[]
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String key = "seckill:" + seckill.getSeckillId();
            byte[] bytes = ProtobufIOUtil.toByteArray(seckill, schema, LinkedBuffer.allocate(LinkedBuffer
                    .DEFAULT_BUFFER_SIZE));
            //超时缓存
            int timeout = 3600;
            String result = jedis.setex(key.getBytes(), timeout, bytes);
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            jedis.close();
        }
        return null;
    }
}
