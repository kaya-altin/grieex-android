package com.grieex.model.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;
import com.grieex.model.TransactionObject;

import java.io.Serializable;

public class Season extends TransactionObject implements IDataModelObject, Serializable {
    private static final String TAG = Season.class.getName();
    public static final String TABLE_NAME = "Seasons";

    public class COLUMNS {
        static final String _ID = "_id";
        public static final String SeriesId = "SeriesId";
        public static final String AiredEpisodes = "AiredEpisodes";
        public static final String EpisodeCount = "EpisodeCount";
        public static final String Number = "Number";
        public static final String Overview = "Overview";
        public static final String TmdbId = "TmdbId";
        public static final String TvdbId = "TvdbId";
        public static final String TraktId = "TraktId";
        public static final String Rating = "Rating";
        public static final String Votes = "Votes";
        public static final String Poster = "Poster";
    }

    public Season() {
    }

    private int _id;
    private Integer SeriesId;
    private Integer AiredEpisodes;
    private Integer EpisodeCount;
    private Integer Number;
    private String Overview;
    private Integer mTmdbId;
    private Integer mTvdbId;
    private Integer mTraktId;
    private String Rating;
    private String Votes;
    private String Poster;


    public int getID() {
        return _id;
    }

    private void setID(int _id) {
        this._id = _id;
    }

    public Integer getAiredEpisodes() {
        return AiredEpisodes;
    }

    public void setAiredEpisodes(Integer airedEpisodes) {
        AiredEpisodes = airedEpisodes;
    }

    public Integer getEpisodeCount() {
        return EpisodeCount;
    }

    public void setEpisodeCount(Integer episodeCount) {
        EpisodeCount = episodeCount;
    }

    public String getOverview() {
        return Overview;
    }

    public void setOverview(String overview) {
        Overview = overview;
    }

    public Integer getTmdbId() {
        return mTmdbId;
    }

    public void setTmdbId(Integer tmdbId) {
        this.mTmdbId = tmdbId;
    }

    public Integer getTraktId() {
        return mTraktId;
    }

    public void setTraktId(Integer traktId) {
        this.mTraktId = traktId;
    }

    public Integer getTvdbId() {
        return mTvdbId;
    }

    public void setTvdbId(Integer tvdbId) {
        this.mTvdbId = tvdbId;
    }

    public String getPoster() {
        return Poster;
    }

    private void setPoster(String poster) {
        Poster = poster;
    }

    public int getNumber() {
        return Number;
    }

    public void setNumber(int number) {
        Number = number;
    }

    public String getRating() {
        return Rating;
    }

    public void setRating(String rating) {
        Rating = rating;
    }

    public Integer getSeriesId() {
        return SeriesId;
    }

    private void setSeriesId(Integer seriesId) {
        SeriesId = seriesId;
    }

    public String getVotes() {
        return Votes;
    }

    public void setVotes(String votes) {
        Votes = votes;
    }


    public ContentValues GetContentValuesForDB() {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.SeriesId, SeriesId);
        values.put(COLUMNS.Overview, Overview);
        values.put(COLUMNS.AiredEpisodes, AiredEpisodes);
        values.put(COLUMNS.EpisodeCount, EpisodeCount);
        values.put(COLUMNS.Number, Number);
        values.put(COLUMNS.Overview, Overview);
        values.put(COLUMNS.TmdbId, mTmdbId);
        values.put(COLUMNS.TvdbId, mTvdbId);
        values.put(COLUMNS.TraktId, mTraktId);
        values.put(COLUMNS.Rating, Rating);
        values.put(COLUMNS.Votes, Votes);
        values.put(COLUMNS.Poster, Poster);

        return values;
    }

    public String GetTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    public String[] GetColumnMapping() {
        return new String[]{COLUMNS._ID, COLUMNS.SeriesId, COLUMNS.Overview, COLUMNS.AiredEpisodes, COLUMNS.EpisodeCount, COLUMNS.Number, COLUMNS.Overview, COLUMNS.TmdbId, COLUMNS.TvdbId, COLUMNS.TraktId, COLUMNS.Rating, COLUMNS.Votes, COLUMNS.Poster};
    }

    @Override
    public void LoadWithCursorRow(Cursor cursor) {
        try {
            if (cursor != null) {
                setID(cursor.getInt(cursor.getColumnIndex(COLUMNS._ID)));
                setSeriesId(cursor.getInt(cursor.getColumnIndex(COLUMNS.SeriesId)));
                setOverview(cursor.getString(cursor.getColumnIndex(COLUMNS.Overview)));
                setAiredEpisodes(cursor.getInt(cursor.getColumnIndex(COLUMNS.AiredEpisodes)));
                setEpisodeCount(cursor.getInt(cursor.getColumnIndex(COLUMNS.EpisodeCount)));
                setNumber(cursor.getInt(cursor.getColumnIndex(COLUMNS.Number)));
                setOverview(cursor.getString(cursor.getColumnIndex(COLUMNS.Overview)));
                setTmdbId(cursor.getInt(cursor.getColumnIndex(COLUMNS.TmdbId)));
                setTvdbId(cursor.getInt(cursor.getColumnIndex(COLUMNS.TvdbId)));
                setTraktId(cursor.getInt(cursor.getColumnIndex(COLUMNS.TraktId)));
                setRating(cursor.getString(cursor.getColumnIndex(COLUMNS.Rating)));
                setVotes(cursor.getString(cursor.getColumnIndex(COLUMNS.Votes)));
                setPoster(cursor.getString(cursor.getColumnIndex(COLUMNS.Poster)));

                int idxWatchedCount = cursor.getColumnIndex("WatchedCount");
                if (idxWatchedCount > -1)
                    setWatchedCount(cursor.getInt(idxWatchedCount));

                int idxEpisodeCountCustom = cursor.getColumnIndex("EpisodeCountCustom");
                if (idxEpisodeCountCustom > -1)
                    setEpisodeCountCustom(cursor.getInt(idxEpisodeCountCustom));
            }
        } catch (Exception e) {
            NLog.e("Cast", e);
        }
    }

    @Override
    public void LoadWithWhereColumn(Context ctx, String WhereColumn, String id) {
        Cursor cursor = null;
        try {
            DatabaseHelper dbHandler = DatabaseHelper.getInstance(ctx.getApplicationContext());
            cursor = dbHandler.GetCursor("Select * From " + TABLE_NAME + " Where " + WhereColumn + "=" + id);

            if (cursor.moveToFirst()) {
                LoadWithCursorRow(cursor);
            }

        } catch (Exception e) {
            NLog.e(TAG, e);

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void LoadWithWhere(Context ctx, String Where) {
        Cursor cursor = null;
        try {
            DatabaseHelper dbHandler = DatabaseHelper.getInstance(ctx.getApplicationContext());
            cursor = dbHandler.GetCursor("Select * From " + TABLE_NAME + " Where " + Where);

            if (cursor.moveToFirst()) {
                LoadWithCursorRow(cursor);
            }

        } catch (Exception e) {
            NLog.e(TAG, e);

        } finally {
            if (cursor != null) {

                cursor.close();
            }
        }
    }

    // Custom Columns

    private Integer WatchedCount = 0;

    public int getWatchedCount() {
        return WatchedCount;
    }

    private void setWatchedCount(int watchedCount) {
        WatchedCount = watchedCount;
    }


    private Integer EpisodeCountCustom = 0;

    public int getEpisodeCountCustom() {
        return EpisodeCountCustom;
    }

    private void setEpisodeCountCustom(int episodeCount) {
        EpisodeCountCustom = episodeCount;
    }
}
