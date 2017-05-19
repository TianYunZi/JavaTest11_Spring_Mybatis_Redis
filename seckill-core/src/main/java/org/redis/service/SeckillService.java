package org.redis.service;

import org.redis.dto.Exposer;
import org.redis.dto.SeckillExecution;
import org.redis.entity.Seckill;

import java.util.List;

/**
 * Created by Admin on 2017/5/19.
 */
public interface SeckillService {

    /**
     * 查询所有秒杀记录.
     *
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录.
     *
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 秒杀开启时输出秒杀接口地址，否则输出系统时间和秒杀时间
     *
     * @param seckillId
     * @return
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作
     *
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5);

    /**
     * 执行秒杀操作by存储过程
     *
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
    SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5);
}
