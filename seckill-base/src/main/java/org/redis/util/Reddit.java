package org.redis.util;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Created by Admin on 2017/5/18.
 * 帖子排名算法,Reddit算法
 */
public class Reddit {

    private static final long BASE_TIME = ZonedDateTime.parse("2017-01-01T00:00:00.0+08:00[Asia/Shanghai]").toEpochSecond();

    private static long age(ZonedDateTime dateTime) {
        return dateTime.toEpochSecond() - BASE_TIME;
    }

    private static int score(int ups, int downs) {
        return ups - downs;
    }

    /**
     * 根据赞成票，反对票，发帖时间，分享次数获得帖子得分
     *
     * @param ups   赞成票
     * @param downs 反对票
     * @param date  发帖时间
     * @param share 分享次数
     * @return 返回帖子得分
     */
    public static double hot(int ups, int downs, ZonedDateTime date, int share) {
        int score = score(ups, downs);
        double distance = Math.log10(Math.max(Math.abs(score), 1));//赞成票与反对票的差额越大，得分越高。
        double shareRank = Math.log(share) / Math.log(2);//分享越多得分越高
        int sign = score > 0 ? 1 : (score < 0 ? -1 : 0);//产生加分或减分
        return distance + shareRank + (double) (sign * age(date)) / 4500.0;//相同情况下,新帖子的得分会高于老帖子。
    }

    public static void main(String[] args) {
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        LocalDate date = LocalDate.of(2017, Month.MAY, 18);
        double hoter = hot(10000, 10, date.atStartOfDay(zoneId), 100);
        System.out.println(hoter);
    }
}
