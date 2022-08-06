package com.grieex.helper;

import android.content.Context;
import android.text.format.DateUtils;

/**
 * Created by Griee on 18.03.2016.
 */
public class TraktSettings {

    private static final long REFRESH_THRESHOLD = DateUtils.DAY_IN_MILLIS;

    public static void logOut(Context ctx){
        setTraktAccessToken(ctx,"");
        setTraktRefreshToken(ctx, "");
        setTraktExpiresIn(ctx, 0);
        setTraktUserName(ctx, "");
        setTraktAccessTokenExpiryDate(ctx, 0);
    }

    public static boolean isTimeToRefreshAccessToken(Context ctx) {
        long expiryDate = getTraktAccessTokenExpiryDate(ctx);
        return expiryDate != 0 && expiryDate - REFRESH_THRESHOLD < System.currentTimeMillis();
    }

    public static String getTraktAccessToken(Context ctx) {
        return Prefs.with(ctx).getString(Constants.Pref_TraktAccessToken, "");
    }

    public static void setTraktAccessToken(Context ctx, String value) {
        Prefs.with(ctx).save(Constants.Pref_TraktAccessToken, value);
    }

    public static String getTraktRefreshToken(Context ctx) {
        return Prefs.with(ctx).getString(Constants.Pref_TraktRefreshToken, "");
    }

    public static void setTraktRefreshToken(Context ctx, String value) {
        Prefs.with(ctx).save(Constants.Pref_TraktRefreshToken, value);
    }

    public static long getTraktExpiresIn(Context ctx) {
        return Prefs.with(ctx).getLong(Constants.Pref_TraktExpiresIn, 0);
    }

    public static void setTraktExpiresIn(Context ctx, long value) {
        Prefs.with(ctx).save(Constants.Pref_TraktExpiresIn, value);
    }

    public static String getTraktUserName(Context ctx) {
        return Prefs.with(ctx).getString(Constants.Pref_TraktUserName, "");
    }

    public static void setTraktUserName(Context ctx, String value) {
        Prefs.with(ctx).save(Constants.Pref_TraktUserName, value);
    }

    private static long getTraktAccessTokenExpiryDate(Context ctx) {
        return Prefs.with(ctx).getLong(Constants.Pref_TraktAccessTokenExpiryDate, 0);
    }

    public static void setTraktAccessTokenExpiryDate(Context ctx, long value) {
        Prefs.with(ctx).save(Constants.Pref_TraktAccessTokenExpiryDate, value);
    }

}
