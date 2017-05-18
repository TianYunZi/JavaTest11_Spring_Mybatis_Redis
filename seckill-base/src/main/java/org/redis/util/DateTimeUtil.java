package org.redis.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Admin on 2017/5/18.
 * 日期转换类
 */
public class DateTimeUtil {

    /**
     * 返回当前系统时间字符串类型
     *
     * @return 当前系统时间
     */
    public static String getSysDatetimeStr() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime dateTime = LocalDateTime.now();
        return formatter.format(dateTime);
    }
}
