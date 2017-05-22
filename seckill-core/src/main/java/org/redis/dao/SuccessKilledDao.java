package org.redis.dao;

import org.apache.ibatis.annotations.Param;
import org.redis.entity.SuccessKilled;
import org.springframework.stereotype.Repository;

/**
 * Created by Admin on 2017/5/19.
 */
@Repository("successKilledDao")
public interface SuccessKilledDao {

    /**
     * 插入购买明细，可过滤重复.
     *
     * @param seckillId 秒杀商品id
     * @param userPhone 顾客手机号码
     * @return 返回插入结果
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /**
     * 根据id查询SuccessKilled并携带秒杀产品对象实体.
     *
     * @param seckillId 秒杀商品主键
     * @param userPhone 顾客手机号码
     * @return 返回成功的秒杀
     */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);
}
