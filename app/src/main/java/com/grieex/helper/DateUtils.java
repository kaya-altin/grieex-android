package com.grieex.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.grieex.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    private static final String TAG = DateUtils.class.getName();

    public static String millisToString(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        Date date = cal.getTime();

        return getDateFormat(date, Constants.DATE_FORMAT6);
    }

    public static long getMillisecondsLocale(Date date, String timezone, String time) {
        long millis = 0L;
        int hour = 0;
        int minute = 0;
        try {
            if (date == null)
                return millis;

            millis = date.getTime();

            if (!TextUtils.isEmpty(time)) {
                try {
                    hour = Integer.parseInt(time.substring(0, 2));
                    minute = Integer.parseInt(time.substring(3, 5));
                } catch (Exception e) {
                   // e.printStackTrace();
                }
            }

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(millis);
            cal.set(Calendar.HOUR, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.setTimeZone(TimeZone.getTimeZone(timezone));
            return cal.getTime().getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return millis;
    }

    @SuppressLint("SimpleDateFormat")
    public static Date parseDate(String date) {
        try {
            if (TextUtils.isEmpty(date)) {
                return null;
            }

            String newDate = ConvertDateToString(date);

            DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);

            return df.parse(newDate);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    public static Date DateTimeNow() {
        Calendar c = Calendar.getInstance();
        return c.getTime();
    }

    public static Date DateTimeNow(int iDay) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, iDay);
        return c.getTime();
    }

    public static String DateTimeNowString() {
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        return ConvertDateToString(date);
    }

    public static String DateTimeNowString(int iDay) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, iDay);
        Date date = c.getTime();
        return ConvertDateToString(date);
    }

    public static String getDateFormat(long millis, String targetFormat) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.setTimeZone(TimeZone.getDefault());
        Date date = cal.getTime();

        return getDateFormat(date, targetFormat);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDateFormat(String date, String targetFormat) {
        SimpleDateFormat sdfCurrentFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
        SimpleDateFormat sdfTargetFormat = new SimpleDateFormat(targetFormat);
        try {
            Date d = sdfCurrentFormat.parse(date);
            return sdfTargetFormat.format(d);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    @SuppressLint("SimpleDateFormat")
    private static String getDateFormat(Date date, String targetFormat) {
        DateFormat df = new SimpleDateFormat(targetFormat);
        try {
            return df.format(date);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }

    public static String getDuration(long duration) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(duration - TimeZone.getDefault().getRawOffset()));
    }

    private static String ConvertDateToString(String date) {
        return ConvertDateToString(Constants.DATE_FORMAT, date);
    }

    @SuppressLint("SimpleDateFormat")
    public static String ConvertDateToString(String format, String date) {
        if (TextUtils.isEmpty(date)) {
            return null;
        }

        String returnDate = date;
        try {
            Date parsedDate;
            String[] formats = new String[]{"yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mmZ", "yyyy-MM-dd HH:mm", "yyyy-MM-dd"};
            for (String f : formats) {
                SimpleDateFormat sdf = new SimpleDateFormat(f);
                try {
                    parsedDate = sdf.parse(date);
                    SimpleDateFormat sdfTargetFormat = new SimpleDateFormat(format, Locale.getDefault());
                    returnDate = sdfTargetFormat.format(parsedDate);
                    break;
                } catch (ParseException e) {
                    //e.printStackTrace();
                }
            }

            return returnDate;

        } catch (Exception e) {
            NLog.e(TAG, e);
            return date;
        }
    }

    public static String ConvertDateToString(Date date) {
        if (date == null)
            return "";

        String returnString = "";
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
        try {
            returnString = sdf.format(date);
            returnString = ConvertDateToString(returnString);
        } catch (Exception e) {
            //NLog.e(TAG, e);
        }

        return returnString;
    }

    public static String getDayOfWeek(Context ctx, int year, int month, int day) {

        String result = "";

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(year, month - 1, day);

        int dayConstant = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayConstant) {
            case 1:
                result = ctx.getString(R.string.day_sunday);
                break;
            case 2:
                result = ctx.getString(R.string.day_monday);
                break;
            case 3:
                result = ctx.getString(R.string.day_tuesday);
                break;
            case 4:
                result = ctx.getString(R.string.day_wednesday);
                break;
            case 5:
                result = ctx.getString(R.string.day_thursday);
                break;
            case 6:
                result = ctx.getString(R.string.day_friday);
                break;
            case 7:
                result = ctx.getString(R.string.day_saturday);
                break;
        }
        return result;

    }

    //    public static String millisToDay(Context ctx, long millis){
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(millis);
//
//        String result = "";
//        int iDay =cal.get(Calendar.DAY_OF_MONTH);
//        switch (iDay) {
//            case 1:
//                result = ctx.getString(R.string.);
//                break;
//            case 2:
//                result = ctx.getString(R.string.);
//                break;
//            case 3:
//                result = ctx.getString(R.string.);
//                break;
//            case 4:
//                result = ctx.getString(R.string.);
//                break;
//            case 5:
//                result = ctx.getString(R.string.);
//                break;
//            case 6:
//                result =ctx.getString(R.string.);
//                break;
//            case 7:
//                result = "Cumartesi";
//                break;
//        }
//       return result;
//    }

    @SuppressLint("SimpleDateFormat")
    public static int getYear(String date) {
        try {
            String newDate = ConvertDateToString(date);
            DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);

            Date dt = df.parse(newDate);
            Calendar c = Calendar.getInstance();
            c.setTime(dt);

            return c.get(Calendar.YEAR);
        } catch (Exception e) {
            return 1;
        }
    }

    public static long differenceMinute(long millis) {
        long different = DateUtils.DateTimeNow().getTime() - millis;
        return TimeUnit.MILLISECONDS.toMinutes(different);
    }

    public static Date ThisDayStart() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        return c.getTime();
    }

    public static Date ThisDayEnd() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 23);
        c.set(Calendar.MINUTE, 59);
        return c.getTime();
    }
}
