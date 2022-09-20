package com.grieex.model.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;
import com.grieex.model.TransactionObject;

import java.io.Serializable;

public class Episode extends TransactionObject implements IDataModelObject, Serializable {
    public static final String TABLE_NAME = "Episodes";
    private static final String TAG = Episode.class.getName();
    private int _id;
    private Integer mSeriesId;
    private Integer mSeasonId;
    private String mEpisodeName;
    private Integer mEpisodeNumber;
    private long mFirstAiredMs = 0;
    private String mGuestStars;
    private String mOverview;
    private String mRating;
    private String mRatingCount;
    private Integer mSeasonNumber;
    private Integer mAirsAfterSeason;
    private Integer mAirsBeforeSeason;
    private Integer mAirsBeforeEpisode;
    private String mEpisodeImage;
    private Integer mLastUpdated;
    private Integer mTvdbSeriesId;
    private Integer mTvdbSeasonId;
    private Integer mTvdbEpisodeId;
    private Integer mCollected = 0;
    private Integer mWatched = 0;
    private Integer mFavorite = 0;
    //Custom Columns
    private String mFirstAired;
    public Episode() {

    }

    public int getID() {
        return _id;
    }

    private void setID(int _id) {
        this._id = _id;
    }

    public Integer getSeriesId() {
        return mSeriesId;
    }

    public void setSeriesId(Integer SeriesId) {
        this.mSeriesId = SeriesId;
    }

    public Integer getSeasonId() {
        return mSeasonId;
    }

    private void setSeasonId(Integer SeasonId) {
        this.mSeasonId = SeasonId;
    }

    public String getEpisodeName() {
        return mEpisodeName;
    }

    public void setEpisodeName(String EpisodeName) {
        this.mEpisodeName = EpisodeName;
    }

    public Integer getEpisodeNumber() {
        return mEpisodeNumber;
    }

    public void setEpisodeNumber(Integer EpisodeNumber) {
        this.mEpisodeNumber = EpisodeNumber;
    }

    public long getFirstAiredMs() {
        return mFirstAiredMs;
    }

    public void setFirstAiredMs(long firstAiredMs) {
        this.mFirstAiredMs = firstAiredMs;
    }

    public String getGuestStars() {
        return mGuestStars;
    }

    private void setGuestStars(String GuestStars) {
        this.mGuestStars = GuestStars;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String Overview) {
        this.mOverview = Overview;
    }

    public String getRating() {
        return mRating;
    }

    public void setRating(String Rating) {
        this.mRating = Rating;
    }

    public String getRatingCount() {
        return mRatingCount;
    }

    private void setRatingCount(String RatingCount) {
        this.mRatingCount = RatingCount;
    }

    public Integer getSeasonNumber() {
        return mSeasonNumber;
    }

    public void setSeasonNumber(Integer SeasonNumber) {
        this.mSeasonNumber = SeasonNumber;
    }

    public Integer getAirsAfterSeason() {
        return mAirsAfterSeason;
    }

    public void setAirsAfterSeason(Integer AirsAfterSeason) {
        this.mAirsAfterSeason = AirsAfterSeason;
    }

    public Integer getAirsBeforeSeason() {
        return mAirsBeforeSeason;
    }

    public void setAirsBeforeSeason(Integer AirsBeforeSeason) {
        this.mAirsBeforeSeason = AirsBeforeSeason;
    }

    public Integer getAirsBeforeEpisode() {
        return mAirsBeforeEpisode;
    }

    public void setAirsBeforeEpisode(Integer AirsBeforeEpisode) {
        this.mAirsBeforeEpisode = AirsBeforeEpisode;
    }

    public String getEpisodeImage() {
        return mEpisodeImage;
    }

    public void setEpisodeImage(String EpisodeImage) {
        this.mEpisodeImage = EpisodeImage;
    }

    public Integer getLastUpdated() {
        return mLastUpdated;
    }

    public void setLastUpdated(Integer LastUpdated) {
        this.mLastUpdated = LastUpdated;
    }

    public Integer getTvdbSeriesId() {
        return mTvdbSeriesId;
    }

    public void setTvdbSeriesId(Integer TvdbSeriesId) {
        this.mTvdbSeriesId = TvdbSeriesId;
    }

    public Integer getTvdbSeasonId() {
        return mTvdbSeasonId;
    }

    public void setTvdbSeasonId(Integer TvdbSeasonId) {
        this.mTvdbSeasonId = TvdbSeasonId;
    }

    public Integer getTvdbEpisodeId() {
        return mTvdbEpisodeId;
    }

    public void setTvdbEpisodeId(Integer TvdbEpisodeId) {
        this.mTvdbEpisodeId = TvdbEpisodeId;
    }

    public Integer getCollected() {
        return mCollected;
    }

    public void setCollected(Integer Collected) {
        this.mCollected = Collected;
    }

    public Integer getWatched() {
        return mWatched;
    }

    public void setWatched(Integer watched) {
        this.mWatched = watched;
    }

    public Integer getFavorite() {
        return mFavorite;
    }

    public void setFavorite(Integer favorite) {
        this.mFavorite = favorite;
    }


    public ContentValues GetContentValuesForDB() {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.SeriesId, mSeriesId);
        values.put(COLUMNS.SeasonId, mSeasonId);
        values.put(COLUMNS.EpisodeName, mEpisodeName);
        values.put(COLUMNS.EpisodeNumber, mEpisodeNumber);
        values.put(COLUMNS.FirstAiredMs, mFirstAiredMs);
        values.put(COLUMNS.GuestStars, mGuestStars);
        values.put(COLUMNS.Overview, mOverview);
        values.put(COLUMNS.Rating, mRating);
        values.put(COLUMNS.RatingCount, mRatingCount);
        values.put(COLUMNS.SeasonNumber, mSeasonNumber);
        values.put(COLUMNS.AirsAfterSeason, mAirsAfterSeason);
        values.put(COLUMNS.AirsBeforeSeason, mAirsBeforeSeason);
        values.put(COLUMNS.AirsBeforeEpisode, mAirsBeforeEpisode);
        values.put(COLUMNS.EpisodeImage, mEpisodeImage);
        values.put(COLUMNS.LastUpdated, mLastUpdated);
        values.put(COLUMNS.TvdbSeriesId, mTvdbSeriesId);
        values.put(COLUMNS.TvdbSeasonId, mTvdbSeasonId);
        values.put(COLUMNS.TvdbEpisodeId, mTvdbEpisodeId);
        values.put(COLUMNS.Collected, mCollected);
        values.put(COLUMNS.Watched, mWatched);
        values.put(COLUMNS.Favorite, mFavorite);
        return values;
    }

    public String GetTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    public String[] GetColumnMapping() {
        return new String[]{COLUMNS._ID, COLUMNS.SeriesId, COLUMNS.SeasonId, COLUMNS.EpisodeName, COLUMNS.EpisodeNumber, COLUMNS.FirstAiredMs, COLUMNS.GuestStars, COLUMNS.Overview, COLUMNS.Rating, COLUMNS.RatingCount, COLUMNS.SeasonNumber, COLUMNS.AirsAfterSeason, COLUMNS.AirsBeforeSeason, COLUMNS.AirsBeforeEpisode, COLUMNS.EpisodeImage, COLUMNS.LastUpdated, COLUMNS.TvdbSeriesId, COLUMNS.TvdbSeasonId, COLUMNS.TvdbEpisodeId, COLUMNS.Collected, COLUMNS.Watched, COLUMNS.Favorite};
    }

    @Override
    public void LoadWithCursorRow(Cursor cursor) {
        try {
            if (cursor != null) {
                setID(cursor.getInt(cursor.getColumnIndex(COLUMNS._ID)));
                setSeriesId(cursor.getInt(cursor.getColumnIndex(COLUMNS.SeriesId)));
                setSeasonId(cursor.getInt(cursor.getColumnIndex(COLUMNS.SeasonId)));
                setEpisodeName(cursor.getString(cursor.getColumnIndex(COLUMNS.EpisodeName)));
                setEpisodeNumber(cursor.getInt(cursor.getColumnIndex(COLUMNS.EpisodeNumber)));
                setFirstAiredMs(cursor.getLong(cursor.getColumnIndex(COLUMNS.FirstAiredMs)));
                setGuestStars(cursor.getString(cursor.getColumnIndex(COLUMNS.GuestStars)));
                setOverview(cursor.getString(cursor.getColumnIndex(COLUMNS.Overview)));
                setRating(cursor.getString(cursor.getColumnIndex(COLUMNS.Rating)));
                setRatingCount(cursor.getString(cursor.getColumnIndex(COLUMNS.RatingCount)));
                setSeasonNumber(cursor.getInt(cursor.getColumnIndex(COLUMNS.SeasonNumber)));
                setAirsAfterSeason(cursor.getInt(cursor.getColumnIndex(COLUMNS.AirsAfterSeason)));
                setAirsBeforeSeason(cursor.getInt(cursor.getColumnIndex(COLUMNS.AirsBeforeSeason)));
                setAirsBeforeEpisode(cursor.getInt(cursor.getColumnIndex(COLUMNS.AirsBeforeEpisode)));
                setEpisodeImage(cursor.getString(cursor.getColumnIndex(COLUMNS.EpisodeImage)));
                setLastUpdated(cursor.getInt(cursor.getColumnIndex(COLUMNS.LastUpdated)));
                setTvdbSeriesId(cursor.getInt(cursor.getColumnIndex(COLUMNS.TvdbSeriesId)));
                setTvdbSeasonId(cursor.getInt(cursor.getColumnIndex(COLUMNS.TvdbSeasonId)));
                setTvdbEpisodeId(cursor.getInt(cursor.getColumnIndex(COLUMNS.TvdbEpisodeId)));
                setCollected(cursor.getInt(cursor.getColumnIndex(COLUMNS.Collected)));
                setWatched(cursor.getInt(cursor.getColumnIndex(COLUMNS.Watched)));
                setFavorite(cursor.getInt(cursor.getColumnIndex(COLUMNS.Favorite)));
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
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

    public String getFirstAired() {
        return mFirstAired;
    }

    public void setFirstAired(String firstAired) {
        this.mFirstAired = firstAired;
    }

    public static class COLUMNS {
        public static final String _ID = "_id";
        public static final String SeriesId = "SeriesId";
        public static final String EpisodeName = "EpisodeName";
        public static final String EpisodeNumber = "EpisodeNumber";
        public static final String FirstAiredMs = "FirstAiredMs";
        public static final String GuestStars = "GuestStars";
        public static final String Overview = "Overview";
        public static final String Rating = "Rating";
        public static final String RatingCount = "RatingCount";
        public static final String SeasonNumber = "SeasonNumber";
        public static final String AirsAfterSeason = "AirsAfterSeason";
        public static final String AirsBeforeSeason = "AirsBeforeSeason";
        public static final String AirsBeforeEpisode = "AirsBeforeEpisode";
        public static final String EpisodeImage = "EpisodeImage";
        public static final String LastUpdated = "LastUpdated";
        public static final String TvdbSeriesId = "TvdbSeriesId";
        public static final String TvdbSeasonId = "TvdbSeasonId";
        public static final String TvdbEpisodeId = "TvdbEpisodeId";
        public static final String Collected = "Collected";
        public static final String Watched = "Watched";
        public static final String Favorite = "Favorite";
        static final String SeasonId = "SeasonId";
    }
}
