package com.grieex.helper;

import android.content.Context;
import android.text.TextUtils;

import com.grieex.model.tables.Episode;
import com.grieex.model.tables.Movie;

import java.util.ArrayList;


public class DbUtils {
    private static final String TAG = DbUtils.class.getName();

    public static int getMoviesCount(Context ctx) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            String count = dbHelper.GetOneField("Select Count(*) From Movies");
            return Utils.parseInt(count);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return 0;
    }

    public static int getSeriesCount(Context ctx) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            String count = dbHelper.GetOneField("Select Count(*) From Series");
            return Utils.parseInt(count);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return 0;
    }

    public static boolean isMovieExistWithImdbNumber(Context ctx, String imdbNumber) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            String count = dbHelper.GetOneField("Select Count(*) From Movies Where ImdbNumber='" + imdbNumber + "'");
            if (Utils.parseInt(count) > 0)
                return true;
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return false;
    }

    public static boolean isMovieExistWithTmdbNumber(Context ctx, String tmdbNumber) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            String count = dbHelper.GetOneField("Select Count(*) From Movies Where TmdbNumber='" + tmdbNumber + "'");
            if (Utils.parseInt(count) > 0)
                return true;
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return false;
    }

    public static void SetMovieSeen(Context ctx, int MovieID, boolean bCheck) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            dbHelper.ExecuteQuery("UPDATE " + Movie.TABLE_NAME + " Set " + Movie.COLUMNS.Seen + "=" + (bCheck ? "1" : "0") + "," + Movie.COLUMNS.UpdateDate + "='" + DateUtils.DateTimeNowString() + "' Where " + Movie.COLUMNS._ID + "=" + MovieID);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public static void SetEpisodeFavorite(Context ctx, int EpisodeId, boolean bCheck) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            dbHelper.ExecuteQuery("UPDATE " + Episode.TABLE_NAME + " Set " + Episode.COLUMNS.Favorite + "=" + (bCheck ? "1" : "0") + " Where " + Episode.COLUMNS._ID + "=" + EpisodeId);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public static void SetSeasonEpisodeAllFavorite(Context ctx, int SeriesId, int SeasonId, boolean bCheck) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            dbHelper.ExecuteQuery("UPDATE " + Episode.TABLE_NAME + " Set " + Episode.COLUMNS.Favorite + "=" + (bCheck ? "1" : "0") + " Where " + Episode.COLUMNS.SeriesId + "=" + SeriesId + " and " + Episode.COLUMNS.SeasonNumber + "=" + SeasonId);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public static void SetEpisodeCollected(Context ctx, int EpisodeId, boolean bCheck) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            dbHelper.ExecuteQuery("UPDATE " + Episode.TABLE_NAME + " Set " + Episode.COLUMNS.Collected + "=" + (bCheck ? "1" : "0") + " Where " + Episode.COLUMNS._ID + "=" + EpisodeId);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public static void SetSeasonEpisodeAllCollected(Context ctx, int SeriesId, int SeasonId, boolean bCheck) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            dbHelper.ExecuteQuery("UPDATE " + Episode.TABLE_NAME + " Set " + Episode.COLUMNS.Collected + "=" + (bCheck ? "1" : "0") + " Where " + Episode.COLUMNS.SeriesId + "=" + SeriesId + " and " + Episode.COLUMNS.SeasonNumber + "=" + SeasonId);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public static void SetEpisodeWatched(Context ctx, int EpisodeId, boolean bCheck) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            dbHelper.ExecuteQuery("UPDATE " + Episode.TABLE_NAME + " Set " + Episode.COLUMNS.Watched + "=" + (bCheck ? "1" : "0") + " Where " + Episode.COLUMNS._ID + "=" + EpisodeId);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public static void SetSeasonEpisodeAllWatched(Context ctx, int SeriesId, int SeasonId, boolean bCheck) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            dbHelper.ExecuteQuery("UPDATE " + Episode.TABLE_NAME + " Set " + Episode.COLUMNS.Watched + "=" + (bCheck ? "1" : "0") + " Where " + Episode.COLUMNS.SeriesId + "=" + SeriesId + " and " + Episode.COLUMNS.SeasonNumber + "=" + SeasonId);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public static void DeleteSeason(Context ctx, int SeriesId, int SeasonId) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            dbHelper.ExecuteQuery("DELETE FROM " + Episode.TABLE_NAME + " Where " + Episode.COLUMNS.SeriesId + "=" + SeriesId + " and " + Episode.COLUMNS.SeasonNumber + "=" + SeasonId);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public static long getLastEpisodeMs(Context ctx, int SeriesId) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        long lReturn = 0L;
        long lNow = DateUtils.DateTimeNow().getTime();
        try {
            String str = dbHelper.GetOneField("SELECT FirstAiredMs FROM Episodes Where FirstAiredMs>0 and SeriesId=" + SeriesId + " and FirstAiredMs>" + lNow + " LIMIT 1");
            if (TextUtils.isEmpty(str))
                str = dbHelper.GetOneField("SELECT FirstAiredMs FROM Episodes Where FirstAiredMs>0 and SeriesId=" + SeriesId + " and FirstAiredMs<" + lNow + " LIMIT 1");

            lReturn = Utils.parseLong(str);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return lReturn;
    }

    public static int getEpisodeWatchedCount(Context ctx, long SeriesId) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        int iReturn = 0;
        try {
            String str = dbHelper.GetOneField("SELECT Count(*) FROM Episodes Where SeriesId=" + SeriesId + " and Watched=1");
            iReturn = Utils.parseInt(str);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return iReturn;
    }

    public static int getEpisodeUnWatchedCount(Context ctx, long SeriesId) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        int iReturn = 0;
        try {
            String str = dbHelper.GetOneField("SELECT Count(*) FROM Episodes Where SeriesId=" + SeriesId + " and Watched=0");
            iReturn = Utils.parseInt(str);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return iReturn;
    }

    public static int getEpisodeCount(Context ctx, long SeriesId) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        int iReturn = 0;
        try {
            String str = dbHelper.GetOneField("SELECT Count(*) FROM Episodes Where SeriesId=" + SeriesId);
            iReturn = Utils.parseInt(str);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return iReturn;
    }

    public static int getCollectedCount(Context ctx, long SeriesId) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        int iReturn = 0;
        try {
            String str = dbHelper.GetOneField("SELECT Count(*) FROM Episodes Where Collected=1 and SeriesId=" + SeriesId);
            iReturn = Utils.parseInt(str);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return iReturn;
    }

    public static Episode getNextEpisode(Context ctx, int SeriesId) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            String str = "SELECT * FROM [Episodes] Where SeriesId=" + SeriesId + " and Watched=0 and SeasonNumber>0 Order By SeasonNumber asc, EpisodeNumber asc Limit 1";
            ArrayList<Episode> episodes = (ArrayList<Episode>) dbHelper.GetCursorWithObject(str, Episode.class);
            if (episodes.size() > 0) {
                return episodes.get(0);
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return null;
    }


    public static String getArchiveNumber(Context ctx) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            String value = dbHelper.GetOneField("SELECT MAX(CAST(ArchivesNumber as INTEGER))+1 FROM Movies");
            if (TextUtils.isEmpty(value))
                return "1";

            return value;
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return "";
    }

}
