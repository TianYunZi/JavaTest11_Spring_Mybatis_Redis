package org.redis.exception;

/**
 * Created by Admin on 2017/5/19.
 */
public class SeckillException extends Exception {

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
