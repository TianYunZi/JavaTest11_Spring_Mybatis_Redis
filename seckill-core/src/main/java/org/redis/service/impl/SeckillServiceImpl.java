package org.redis.service.impl;

import org.redis.dto.Exposer;
import org.redis.dto.SeckillExecution;
import org.redis.entity.Seckill;
import org.redis.service.SeckillService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Admin on 2017/5/19.
 * 业务接口：站在"使用者"角度设计接口 三个方面：方法定义粒度，参数，返回类型（return 类型/异常）
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Override
    public List<Seckill> getSeckillList() {
        return null;
    }

    @Override
    public Seckill getById(long seckillId) {
        return null;
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        return null;
    }

    @Override
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) {
        return null;
    }

    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        return null;
    }
}
