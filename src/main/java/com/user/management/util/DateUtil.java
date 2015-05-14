package com.user.management.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class DateUtil {

    private static final DateTimeFormatter ISO8061_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static ZonedDateTime getDateFromIso8061DateString(String dateString) {
        return ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String getCurrentDateAsIso8061String() {
        ZonedDateTime today = ZonedDateTime.now();
        return today.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

     public static String getDateDateAsIso8061String(ZonedDateTime date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
