package com.grieex.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.grieex.R;
import com.grieex.model.Page;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class GrieeXSettings {
    public static final int DB_VERSION = 11;
    public static final boolean RELEASE_MODE = true;
    private static final String GrieeXPackageName = "com.grieex.pro";
    public static final String GrieeXPro = "com.grieex.pro";

    public static final int FreeRecordLimitMovie = 500;
    public static final int FreeRecordLimitSeries = 25;

    public static final String TmdbPosterSize = "w342";
    public static final String TmdbCastPosterSize = "w342";
    public static final String TmdbBackdropPosterSize = "w500";
    public static final String TmdbApiKey = "3c440487ed696400b3f8f1dc6d342ff6";
    public static final String TvDbApiKey = "4FE5A00CD16DC051";

    public static final String TraktApiKey = "3bc9efe66cf2cb32eef95824929aaa6c99756f818e5f9f32b53a0437c3e738b9";
    public static final String TraktClientSecret = "adafa8368dbe94e475fb754cc2ffdce419436440867b191df0a5cc339aa248df";
    public static final String TraktCallbackUrl = "grieex://callback";


    public static final String BACKUP_PATH = Environment.getExternalStorageDirectory() + "/GrieeX Backup/";
    //public static final String DB_PATH = Environment.getExternalStorageDirectory() + "/android/data/" + GrieeXPackageName + "/databases/";
    public static final String ASSETS_DB_NAME = "GrieeX.db";
    public static final String DB_NAME = "GrieeX.db";
    @SuppressLint("SdCardPath")
    public static final String DB_PATH = "/data/data/" + GrieeXPackageName + "/databases/";
    public static final String DB_PATHFULL = DB_PATH + DB_NAME;
    public static final String DB_PATHFULL_REPAIR = DB_PATH + DB_NAME + ".repair";
    public static final String DB_MERGE_PATHFULL = DB_PATH + DB_NAME + ".merge";
    public static final String GrieeXURL = "http://www.griee.com";
    public static final String DROPBOX_APP_KEY = "v949u8qg4zk87kq";
    public static final String DROPBOX_APP_SECRET = "z2ghn5clfj3wztt";
    public static final String DB_PATH_DROPBOX = GrieeXSettings.getTempPath() + "GrieeX.db";

    public static String getLocale(Context ctx) {
        String value = Prefs.with(ctx).getString(Constants.Pref_Locale_Key, "");
        if (TextUtils.isEmpty(value)) {
            value = Locale.getDefault().getLanguage();
            Prefs.with(ctx).save(Constants.Pref_Locale_Key, value);
        }
        return value;
    }

    public static void setLocale(Context ctx, String value) {
        Prefs.with(ctx).save(Constants.Pref_Locale_Key, value);
    }

    private static int themeId = -1;

    public static int getTheme(Context ctx) {
        int iReturn = R.style.AppTheme;
        if (themeId == -1) {
            themeId = Prefs.with(ctx).getInt(Constants.Pref_ThemeId, 0);
        }

        switch (themeId) {
            case 0:
                iReturn = R.style.AppTheme;
                break;
            case 2:
                iReturn = R.style.AppTheme2;
                break;
            case 3:
                iReturn = R.style.AppTheme3;
                break;
            case 4:
                iReturn = R.style.AppTheme4;
                break;
            case 5:
                iReturn = R.style.AppTheme5;
                break;
            case 6:
                iReturn = R.style.AppTheme6;
                break;
            case 7:
                iReturn = R.style.AppTheme7;
                break;
            case 8:
                iReturn = R.style.AppTheme8;
                break;
            case 9:
                iReturn = R.style.AppTheme9;
                break;
            case 10:
                iReturn = R.style.AppTheme10;
                break;
        }

        return iReturn;
    }

    public static void setTheme(Context ctx, int id) {
        themeId = id;
        Prefs.with(ctx).save(Constants.Pref_ThemeId, themeId);
    }

    private static String mTempPath;

    @SuppressLint("SdCardPath")
    private static String getTempPath() {
        if (mTempPath == null) {
            if (Utils.isExtStorageAvailable()) {
                mTempPath = Environment.getExternalStorageDirectory() + "/android/data/" + GrieeXPackageName + File.separator + "Temp" + File.separator;
            } else {
                mTempPath = "/data/data/" + GrieeXPackageName + "/Temp/";
            }
        }

        File f = new File(mTempPath);
        if (!f.exists()) {
            f.mkdirs();
            try {
                new File(mTempPath, ".nomedia").createNewFile();
            } catch (IOException e) {
            }
        }

        return mTempPath;
    }

    public static int getNotificationTime(Context ctx) {
        return Prefs.with(ctx).getInt(Constants.Pref_Notification_Time, -1);
    }

    public static int getNotificationTimeIndex(Context ctx) {
        int value = Prefs.with(ctx).getInt(Constants.Pref_Notification_Time, -1);

        switch (value) {
            case -1:
                return 0;
            case 0:
                return 1;
            case 15:
                return 2;
            case 30:
                return 3;
            case 60:
                return 4;
            case 120:
                return 5;
            case 180:
                return 6;
            case 360:
                return 7;
        }

        return 3;
    }

    public static void setNotificationTimeFromIndex(Context ctx, int index) {
        int value = 30;

        switch (index) {
            case 0:
                value = -1;
                break;
            case 1:
                value = 0;
                break;
            case 2:
                value = 15;
                break;
            case 3:
                value = 30;
                break;
            case 4:
                value = 60;
                break;
            case 5:
                value = 120;
                break;
            case 6:
                value = 180;
                break;
            case 7:
                value = 360;
                break;
        }

        Prefs.with(ctx).save(Constants.Pref_Notification_Time, value);
    }

    public static void setNotificationTime(Context ctx, int value) {
        Prefs.with(ctx).save(Constants.Pref_Notification_Time, value);
    }

    public static int getLastPage(Context ctx) {
        return Prefs.with(ctx).getInt(Constants.Pref_Last_Page, Page.PageTypes.MovieList.getValue());
    }

    public static void setLastPage(Context ctx, int value) {
        Prefs.with(ctx).save(Constants.Pref_Last_Page, value);
    }

}
