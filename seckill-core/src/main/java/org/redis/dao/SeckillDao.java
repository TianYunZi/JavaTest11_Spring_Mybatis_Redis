package org.redis.dao;

import org.apache.ibatis.annotations.Param;
import org.redis.entity.Seckill;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 2017/5/19.
 * 秒杀库存DAO接口
 */
public interface SeckillDao {

    /**
     * 减库存.
     *
     * @param seckillId 秒杀库存主键
     * @param killTime  商品被秒杀时间
     * @return 如果影响行数等于>1，表示更新的记录行数
     */
    int reduceNumber(@Param("seckillid") long seckillId, @Param("killTime") LocalDateTime killTime);

    /**
     * 根据id查询秒杀对象.
     *
     * @param seckillId 秒杀对象id
     * @return 返回秒杀对象
     */
    Seckill queryById(@Param("seckillId") long seckillId);

    /**
     * 根据偏移量查询待秒杀商品列表.
     *
     * @param offset 偏移量
     * @param limit  查询数量
     * @return 返回符合条件的秒杀商品列表
     */
    List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 使用存储过程执行秒杀.
     *
     * @param paramMap
     */
    void killByProcedure(Map<String, Object> paramMap);
}
