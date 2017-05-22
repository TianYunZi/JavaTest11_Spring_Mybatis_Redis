package org.redis.service.impl;

import org.apache.commons.collections.MapUtils;
import org.redis.dao.SeckillDao;
import org.redis.dao.SuccessKilledDao;
import org.redis.dao.cache.RedisDao;
import org.redis.dto.Exposer;
import org.redis.dto.SeckillExecution;
import org.redis.entity.Seckill;
import org.redis.entity.SuccessKilled;
import org.redis.enums.SeckillStateEnum;
import org.redis.exception.RepeatKillException;
import org.redis.exception.SeckillCloseException;
import org.redis.exception.SeckillException;
import org.redis.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 2017/5/19.
 * 业务接口：站在"使用者"角度设计接口 三个方面：方法定义粒度，参数，返回类型（return 类型/异常）
 */
@Service("seckillService")
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(SeckillServiceImpl.class);

    private SeckillDao seckillDao;

    private SuccessKilledDao successKilledDao;

    private RedisDao redisDao;

    @Autowired
    public SeckillServiceImpl(SeckillDao seckillDao, SuccessKilledDao successKilledDao, RedisDao redisDao) {
        this.seckillDao = seckillDao;
        this.successKilledDao = successKilledDao;
        this.redisDao = redisDao;
    }

    // md5盐值字符串，用于混淆MD5
    private final String salt = "we24524rfrfm%#%%:;''_+67862nmnn";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 6);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    /**
     * 把秒杀库存商品主键加密md5.
     *
     * @param seckillId 秒杀库存商品主键
     * @return md5值
     */
    private String getMD5(long seckillId) {
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        // 优化点：缓存优化：超时的基础上维护一致性
        // 1.访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            //2.访问数据库
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                redisDao.putSeckill(seckill);
            }
        }
        LocalDateTime startTime = seckill.getStartTime();
        LocalDateTime endTime = seckill.getEndTime();
        //系统当前时间
        LocalDateTime nowTime = LocalDateTime.now();
        if (nowTime.isBefore(startTime) || nowTime.isAfter(endTime)) {
            return new Exposer(false, seckillId, nowTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
                    , startTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), endTime.atZone(ZoneId.of
                    ("Asia/Shanghai")).toInstant().toEpochMilli());
        }
        // 转化特定字符串的过程，不可逆
        String md5 = this.getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    /**
     * 使用注解控制事务方法的优点： 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作，RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制
     */
    @Override
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws RepeatKillException,
            SeckillCloseException, SeckillException {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("秒杀数据重写");
        }
        // 执行秒杀逻辑：减库存 + 记录购买行为
        LocalDateTime now = LocalDateTime.now();
        // 记录购买行为
        int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
        if (insertCount <= 0) {
            //重复秒杀
            throw new RepeatKillException("重复秒杀");
        } else {
            //减库存，热点商品竞争
            int updateCount = seckillDao.reduceNumber(seckillId, now);
            if (updateCount <= 0) {
                // 没有更新到记录 rollback
                throw new SeckillCloseException("未更新到数据库，秒杀关闭");
            } else {
                // 秒杀成功 commit
                SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
            }
        }
    }

    /**
     * 使用Mysql存储过程执行秒杀.
     *
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStateEnum.DATA_REWRITE);
        }
        LocalDateTime killTime = LocalDateTime.now();
        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        // 执行存储过程，result被赋值
        seckillDao.killByProcedure(map);
        int result = MapUtils.getInteger(map, "result", -2);
        try {
            if (result == 1) {
                SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
            } else {
                return new SeckillExecution(seckillId, SeckillStateEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
        }
    }
}
