package com.kanzaji.catdownloaderlegacy.utils;

import java.time.LocalDateTime;

public class DateUtils {
    public static String getCurrentDate() {
        LocalDateTime time = LocalDateTime.now();
        return time.getDayOfMonth() + "." + time.getMonthValue() + "." + time.getYear();
    }

    public static String getCurrentTime() {
        LocalDateTime time = LocalDateTime.now();
        return time.getHour() + "-" + time.getMinute() + "-" + time.getSecond();
    }

    public static String getCurrentTimeDetail() {
        LocalDateTime time = LocalDateTime.now();
        return getCurrentTime() + "." + time.getNano();
    }

    public static String getCurrentFullDate() {
        return getCurrentDate() + " " + getCurrentTime();
    }

    public static String getCurrentFulLDateDetail() {
        return getCurrentDate() + " " + getCurrentTimeDetail();
    }
}
