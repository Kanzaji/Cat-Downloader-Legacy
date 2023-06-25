package com.kanzaji.catdownloaderlegacy.utils;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class DateUtils {
    /**
     * Used to get a {@link String} with current Date.
     * @return {@link String} with current Date.
     */
    public static @NotNull String getCurrentDate() {
        LocalDateTime time = LocalDateTime.now();
        return time.getDayOfMonth() + "." + time.getMonthValue() + "." + time.getYear();
    }

    /**
     * Used to get a {@link String} with current Time (Hours-Minutes-Seconds).
     * @return {@link String} with current Time.
     */
    public static @NotNull String getCurrentTime() {
        LocalDateTime time = LocalDateTime.now();
        return time.getHour() + "-" + time.getMinute() + "-" + time.getSecond();
    }

    /**
     * Used to get a {@link String} with current Time (Hours-Minutes-Seconds.Nano).
     * @return {@link String} with current Time.
     */
    public static @NotNull String getCurrentTimeDetail() {
        LocalDateTime time = LocalDateTime.now();
        return time.getHour() + "-" + time.getMinute() + "-" + time.getSecond() + "." + time.getNano();
    }

    /**
     * Used to get a {@link String} with current Date and Time (Day.Month.Year Hours-Minutes-Seconds).
     * @return {@link String} with current Date and Time.
     */
    public static @NotNull String getCurrentFullDate() {
        return getCurrentDate() + " " + getCurrentTime();
    }

    /**
     * Used to get a {@link String} with current Date and Time (Day.Month.Year Hours-Minutes-Seconds.Nano).
     * @return {@link String} with current Date and Time.
     */
    public static @NotNull String getCurrentFulLDateDetail() {
        return getCurrentDate() + " " + getCurrentTimeDetail();
    }
}
