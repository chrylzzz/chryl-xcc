package com.haiyisoft.util;

import cn.hutool.core.date.DatePattern;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Chr.yl on 2023/7/3.
 *
 * @author Chr.yl
 */
public class DateUtil {
    //yyyy-MM-dd HH:mm:ss.SSS
    public static final String LOCAL_DATETIME_MS_PATTERN = DatePattern.NORM_DATETIME_MS_PATTERN;
    private static final DateTimeFormatter DTF_LOCAL_DATETIME_MS_PATTERN = DateTimeFormatter.ofPattern(LOCAL_DATETIME_MS_PATTERN);
    //yyyy-MM-dd HH:mm:ss:SSS
    public static final String LOCAL_DATETIME_MS_PATTERN_ = "yyyy-MM-dd HH:mm:ss:SSS";
    private static final DateTimeFormatter DTF_LOCAL_DATETIME_MS_PATTERN_ = DateTimeFormatter.ofPattern(LOCAL_DATETIME_MS_PATTERN_);

    //yyyy-MM-dd HH:mm:ss
    public static final String LOCAL_DATETIME_PATTERN = DatePattern.NORM_DATETIME_PATTERN;
    private static final DateTimeFormatter DTF_LOCAL_DATETIME_PATTERN = DateTimeFormatter.ofPattern(LOCAL_DATETIME_PATTERN);
    //yyyy-MM-dd
    public static final String LOCAL_DATE_PATTERN = DatePattern.NORM_DATE_PATTERN;
    private static final DateTimeFormatter DTF_LOCAL_DATE_PATTERN = DateTimeFormatter.ofPattern(LOCAL_DATE_PATTERN);

    /**
     * 获取当前时间
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getLocalDateTime() {
        LocalDateTime now = LocalDateTime.now(); // 当前时间
        return now.format(DTF_LOCAL_DATETIME_PATTERN);
    }

    /**
     * 获取当前日期
     *
     * @return yyyy-MM-dd
     */
    public static String getLocalDate() {
        LocalDate now = LocalDate.now(); // 当前时间
        return now.format(DTF_LOCAL_DATE_PATTERN);
    }

    /**
     * 时间转换
     *
     * @param dateStr 2023-07-03 18:13:59:919
     * @return 2023-07-03 18:13:59
     */
    public static String parseLocalDateTime(String dateStr) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, DTF_LOCAL_DATETIME_MS_PATTERN_);
        return localDateTime.format(DTF_LOCAL_DATETIME_PATTERN);
    }

    /**
     * 时间转换
     *
     * @param dateStr 2023-07-03 18:13:59.919
     * @return 2023-07-03 18:13:59
     */
    public static String parseLocalDateTime_(String dateStr) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, DTF_LOCAL_DATETIME_MS_PATTERN);
        return localDateTime.format(DTF_LOCAL_DATETIME_PATTERN);
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(getLocalDate());
        System.out.println(getLocalDateTime());

        LocalTime now = LocalTime.now();
        System.out.println("LocalTime:" + now);
        String aaa = "2023-07-03 18:13:59:919";
        String bbb = "2020-05-29 07:51:33.106";
        String ccc = "2020-05-29T07:51:33.106";

        String s = parseLocalDateTime(aaa);
        System.out.println(s);
        String s1 = parseLocalDateTime_(bbb);
        System.out.println(s1);


    }

}
