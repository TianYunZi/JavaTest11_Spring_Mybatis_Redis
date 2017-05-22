package org.redis.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Created by XJX on 2017/5/20.
 * 采用AOP的方式处理参数问题
 */
@Aspect
@Component
public class LogAOP {

    private final Logger logger = LoggerFactory.getLogger(LogAOP.class);

    @Pointcut("execution(* org.redis.web.*.*(..))")
    public void aopMethod() {

    }

    @Around("aopMethod()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String classType = joinPoint.getTarget().getClass().getName();
        logger.info("classType: " + classType);
        //运用反射的原理创建对象
        Class<?> clazz = null;
        try {
            clazz = Class.forName(classType);
            String clazzName = clazz.getName();
            String simpleName = clazz.getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            Logger log = LoggerFactory.getLogger(clazzName);
            log.info("simpleName: " + simpleName);
            log.info("clazzName: " + clazzName + ", methodName:" + methodName);
            log.info("在方法调用之前");
            log.info("-----------------------------------------------------");
            log.info("调用类：" + simpleName);
            log.info("调用方法：" + methodName);
            return joinPoint.proceed();
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Throwable throwable) {
            logger.error(throwable.getMessage(), throwable);
            throw throwable;
        }
    }
}
