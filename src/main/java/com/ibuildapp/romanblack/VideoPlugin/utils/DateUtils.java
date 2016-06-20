package com.ibuildapp.romanblack.VideoPlugin.utils;


import android.app.Activity;
import android.content.Context;

import com.ibuildapp.romanblack.VideoPlugin.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static SimpleDateFormat OUT_FORMAT;
    private static SimpleDateFormat YOUTUBE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'", Locale.getDefault());
    private static SimpleDateFormat VIMEO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
    public static Date parsYouTubeDate(String date){
        try {
            return YOUTUBE_DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    public static String getAgoDate(Context ctx, Long publishedMills){
        if (OUT_FORMAT == null)
            OUT_FORMAT = new SimpleDateFormat(ctx.getString(R.string.video_plugin_date_format), Locale.getDefault());

        Long secDiff = (new Date().getTime() - publishedMills)/1000;

        if (secDiff > 60*60*24*30)
            return ctx.getString(R.string.video_plugin_added)+ " " + OUT_FORMAT.format(new Date(publishedMills));

        if (secDiff > 60*60*24*7) {
            int weeks = Math.abs((int) (secDiff/(60*60*24*7)));
            return ctx.getResources().getQuantityString(R.plurals.numberOfWeeks, weeks, weeks);
        }

        if (secDiff > 60*60*24) {
            int days = Math.abs((int) (secDiff/(60*60*24)));
            return ctx.getResources().getQuantityString(R.plurals.numberOfDays, days, days);
        }

        if (secDiff > 60*60) {
            int hours = (int) (secDiff/(60*60));
            return ctx.getResources().getQuantityString(R.plurals.numberOfHours, hours, hours);
        }

        if (secDiff > 60) {
            int minutes = (int) (secDiff/(60));
            return ctx.getResources().getQuantityString(R.plurals.numberOfMinutes, minutes, minutes);
        }

        return  ctx.getResources().getQuantityString(R.plurals.numberOfSeconds, Math.abs(secDiff.intValue()), Math.abs(secDiff.intValue()));
    }

    public static String parseYouTubeDuration(String youTubeDuration){
        String time = youTubeDuration.substring(2);
        Object[][] indexes = new Object[][]{{"H", 3600}, {"M", 60}, {"S", 1}};

        StringBuilder resultString = new StringBuilder();
        for (Object[] letter : indexes) {
            int index = time.indexOf((String) letter[0]);
            if (index != -1) {
                String value = time.substring(0, index);
                resultString.append(String.format(Locale.getDefault(),"%02d", Integer.valueOf(value))).append(":");
                time = time.substring(value.length() + 1);
            }
        }

        if (resultString.length()> 0)
            resultString.deleteCharAt(resultString.length()-1);

        if (resultString.length()> 0 && resultString.charAt(0) == '0')
            resultString.deleteCharAt(0);

        return resultString.toString();
    }

    public static Date parseVimeoDate(String uploadDate) {
        try {
            return VIMEO_DATE_FORMAT.parse(uploadDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    public static String parseStringDurationFromLong(Long mills){
        StringBuilder result = new StringBuilder();
        int seconds = (int) (mills/1000);

        int secondsCount = seconds%60;
        result.append(":").append(String.format(Locale.getDefault(),"%02d", secondsCount));

        int minutes = seconds/60;
        int minutesCount = minutes%60;
        result.insert(0, String.format(Locale.getDefault(),"%02d", minutesCount));

        if (minutes/60!= 0){
            int hours = minutes/60;
            int hoursCount = hours%60;
            result.insert(0, ":");
            result.insert(0, String.format(Locale.getDefault(),"%02d", hoursCount));
        }

        if (result.charAt(0) == '0')
            result.deleteCharAt(0);

        return result.toString();
    }

    public static String getAgoDateWithAgo(Context ctx, Long publishedMills){
        if (OUT_FORMAT == null)
            OUT_FORMAT = new SimpleDateFormat(ctx.getString(R.string.video_plugin_date_format), Locale.getDefault());

        Long secDiff = (new Date().getTime() - publishedMills)/1000;

        if (secDiff > 60*60*24*30)
            return ctx.getString(R.string.video_plugin_added)+ " " + OUT_FORMAT.format(new Date(publishedMills));

        if (secDiff > 60*60*24*7) {
            int weeks = Math.abs((int) (secDiff/(60*60*24*7)));
            return ctx.getResources().getQuantityString(R.plurals.numberOfWeeks, weeks, weeks) + " " + ctx.getString(R.string.video_plugin_back);
        }

        if (secDiff > 60*60*24) {
            int days = Math.abs((int) (secDiff/(60*60*24)));
            return ctx.getResources().getQuantityString(R.plurals.numberOfDays, days, days) + " " + ctx.getString(R.string.video_plugin_back);
        }

        if (secDiff > 60*60) {
            int hours = (int) (secDiff/(60*60));
            return ctx.getResources().getQuantityString(R.plurals.numberOfHours, hours, hours) + " " + ctx.getString(R.string.video_plugin_back);
        }

        if (secDiff > 60) {
            int minutes = (int) (secDiff/(60));
            return ctx.getResources().getQuantityString(R.plurals.numberOfMinutes, minutes, minutes) + " " + ctx.getString(R.string.video_plugin_back);
        }

        return  ctx.getResources().getQuantityString(R.plurals.numberOfSeconds, Math.abs(secDiff.intValue()), Math.abs(secDiff.intValue())) + " " + ctx.getString(R.string.video_plugin_back);
    }
    public static String getAgoDate(Context ctx, Date publishedDate) {
        return  getAgoDate(ctx, publishedDate.getTime());
    }
}
