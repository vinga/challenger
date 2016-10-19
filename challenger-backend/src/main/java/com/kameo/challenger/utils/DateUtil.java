package com.kameo.challenger.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static Date addDays(Date from, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(from);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }
    public static Date addHours(Date from, int h) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(from);
        cal.add(Calendar.HOUR_OF_DAY, h);
        return cal.getTime();
    }

    public static java.util.Date getMidnight(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        resetTimePart(calendar);
        return new Date(calendar.getTimeInMillis());
    }

    private static void resetTimePart(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0); // set hour to midnight
        cal.set(Calendar.MINUTE, 0); // set minute in hour
        cal.set(Calendar.SECOND, 0); // set second in minute
        cal.set(Calendar.MILLISECOND, 0);
    }


}
