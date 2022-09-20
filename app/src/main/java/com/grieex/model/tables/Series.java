package com.grieex.model.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;
import com.uwetrottmann.trakt5.entities.Comment;

import java.io.Serializable;
import java.util.ArrayList;

public class Series implements IDataModelObject, Serializable {
    public static final String TABLE_NAME = "Series";
    private static final String TAG = Series.class.getName();
    private int _id;
    private String mSeriesName;
    private String mOverview;
    private String mFirstAired;
    private String mNetwork;
    private String mImdbId;
    private Integer mTmdbId;
    private Integer mTvdbId;
    private Integer mTraktId;
    private String mImdbUserRating;
    private String mImdbVotes;
    private String mTmdbUserRating;
    private String mTmdbVotes;
    private String mLanguage;
    private String mCountry;
    private String mGenres;
    private String mRuntime;
    private String mCertification;
    private String mAirDay;
    private String mAirTime;
    private Integer mAirYear;
    private String mTimezone;
    private String mStatus;
    private Double mRating;
    private Integer mVotes;
    private String mSeriesLastUpdate;
    private String mPoster;
    private String mFanart;
    private String mHomepage;
    private int mContentProvider;
    private String mInsertDate;
    private String mUpdateDate;
    // **************** Custom Fields **************** //
    private boolean mIsExisting = false;
    private String mFirstCharacter;
    private ArrayList<Backdrop> mBackdrops;
    private ArrayList<Cast> mCast;
    private ArrayList<Season> mSeasons;
    private ArrayList<Episode> mEpisodes;
    private String mDateInfo;
    private int mEpisodeCount;
    private int mWatchedCount;
    private String mEpisodeName;
    private int mCollectedCount;
    private ArrayList<Comment> mComments;
    private boolean mIsSelected = false;

    public Series() {
    }

    public int getID() {
        return _id;
    }

    public void setID(int _id) {
        this._id = _id;
    }

    public String getSeriesName() {
        return mSeriesName;
    }

    public void setSeriesName(String mSeriesName) {
        this.mSeriesName = mSeriesName;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String mOverview) {
        this.mOverview = mOverview;
    }

    public String getFirstAired() {
        return mFirstAired;
    }

    public void setFirstAired(String mFirstAired) {
        this.mFirstAired = mFirstAired;
    }

    public String getNetwork() {
        return mNetwork;
    }

    public void setNetwork(String mNetwork) {
        this.mNetwork = mNetwork;
    }

    public String getImdbId() {
        return mImdbId;
    }

    public void setImdbId(String imdbId) {
        this.mImdbId = imdbId;
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

    public String getImdbUserRating() {
        return mImdbUserRating;
    }

    public void setImdbUserRating(String ImdbUserRating) {
        this.mImdbUserRating = ImdbUserRating;
    }

    public String getImdbVotes() {
        return mImdbVotes;
    }

    public void setImdbVotes(String ImdbVotes) {
        this.mImdbVotes = ImdbVotes;
    }

    public String getTmdbUserRating() {
        return mTmdbUserRating;
    }

    public void setTmdbUserRating(String TmdbUserRating) {
        this.mTmdbUserRating = TmdbUserRating;
    }

    public String getTmdbVotes() {
        return mTmdbVotes;
    }

    public void setTmdbVotes(String TmdbVotes) {
        this.mTmdbVotes = TmdbVotes;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String mLanguage) {
        this.mLanguage = mLanguage;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String country) {
        this.mCountry = country;
    }

    public String getGenres() {
        return mGenres;
    }

    public void setGenres(String mGenres) {
        this.mGenres = mGenres;
    }

    public String getRuntime() {
        return mRuntime;
    }

    public void setRuntime(String mRuntime) {
        this.mRuntime = mRuntime;
    }

    public String getCertification() {
        return mCertification;
    }

    public void setCertification(String mCertification) {
        this.mCertification = mCertification;
    }

    public String getAirDay() {
        return mAirDay;
    }

    public void setAirDay(String day) {
        this.mAirDay = day;
    }

    public String getAirTime() {
        return mAirTime;
    }

    public void setAirTime(String time) {
        this.mAirTime = time;
    }

    public String getTimezone() {
        return mTimezone;
    }

    public void setTimezone(String mTimezone) {
        this.mTimezone = mTimezone;
    }

    public Integer getAirYear() {
        return mAirYear;
    }

    public void setAirYear(Integer year) {
        this.mAirYear = year;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public Double getRating() {
        return mRating;
    }

    public void setRating(Double rating) {
        this.mRating = rating;
    }

    public Integer getVotes() {
        return mVotes;
    }

    public void setVotes(Integer votes) {
        this.mVotes = votes;
    }

    public String getSeriesLastUpdate() {
        return mSeriesLastUpdate;
    }

    public void setSeriesLastUpdate(String mSeriesLastUpdate) {
        this.mSeriesLastUpdate = mSeriesLastUpdate;
    }

    public String getPoster() {
        return mPoster;
    }

    public void setPoster(String mPoster) {
        this.mPoster = mPoster;
    }

    public String getFanart() {
        return mFanart;
    }

    public void setFanart(String mFanart) {
        this.mFanart = mFanart;
    }

    public String getHomepage() {
        return mHomepage;
    }

    public void setHomepage(String homepage) {
        this.mHomepage = homepage;
    }

    public int getContentProvider() {
        return mContentProvider;
    }

    public void setContentProvider(int mContentProvider) {
        this.mContentProvider = mContentProvider;
    }

    public String getInsertDate() {
        return mInsertDate;
    }

    public void setInsertDate(String mInsertDate) {
        this.mInsertDate = mInsertDate;
    }

    public String getUpdateDate() {
        return mUpdateDate;
    }

    public void setUpdateDate(String mUpdateDate) {
        this.mUpdateDate = mUpdateDate;
    }

    public ContentValues GetContentValuesForDB() {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.SeriesName, mSeriesName);
        values.put(COLUMNS.Overview, mOverview);
        values.put(COLUMNS.FirstAired, mFirstAired);
        values.put(COLUMNS.Network, mNetwork);
        values.put(COLUMNS.TmdbId, mTmdbId);
        values.put(COLUMNS.ImdbId, mImdbId);
        values.put(COLUMNS.TvdbId, mTvdbId);
        values.put(COLUMNS.TraktId, mTraktId);
        values.put(COLUMNS.ImdbUserRating, mImdbUserRating);
        values.put(COLUMNS.ImdbVotes, mImdbVotes);
        values.put(COLUMNS.TmdbUserRating, mTmdbUserRating);
        values.put(COLUMNS.TmdbVotes, mTmdbVotes);
        values.put(COLUMNS.Language, mLanguage);
        values.put(COLUMNS.Country, mCountry);
        values.put(COLUMNS.Genres, mGenres);
        values.put(COLUMNS.Runtime, mRuntime);
        values.put(COLUMNS.Certification, mCertification);
        values.put(COLUMNS.AirDay, mAirDay);
        values.put(COLUMNS.AirTime, mAirTime);
        values.put(COLUMNS.AirYear, mAirYear);
        values.put(COLUMNS.Timezone, mTimezone);
        values.put(COLUMNS.Status, mStatus);
        values.put(COLUMNS.Rating, mRating);
        values.put(COLUMNS.Votes, mVotes);
        values.put(COLUMNS.SeriesLastUpdate, mSeriesLastUpdate);
        values.put(COLUMNS.Poster, mPoster);
        values.put(COLUMNS.Fanart, mFanart);
        values.put(COLUMNS.Homepage, mHomepage);
        values.put(COLUMNS.ContentProvider, mContentProvider);
        values.put(COLUMNS.InsertDate, mInsertDate);
        values.put(COLUMNS.UpdateDate, mUpdateDate);
        return values;
    }

    public String GetTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    public String[] GetColumnMapping() {
        return new String[]{COLUMNS._ID, COLUMNS.SeriesName, COLUMNS.Overview, COLUMNS.FirstAired, COLUMNS.Network, COLUMNS.ImdbId, COLUMNS.TmdbId, COLUMNS.TvdbId, COLUMNS.TraktId,
                COLUMNS.ImdbUserRating, COLUMNS.ImdbVotes, COLUMNS.TmdbUserRating, COLUMNS.TmdbVotes,
                COLUMNS.Language, COLUMNS.Country, COLUMNS.Genres, COLUMNS.Runtime, COLUMNS.Certification, COLUMNS.AirDay, COLUMNS.AirTime, COLUMNS.AirYear, COLUMNS.Timezone, COLUMNS.Status, COLUMNS.Rating, COLUMNS.Votes, COLUMNS.SeriesLastUpdate, COLUMNS.Poster, COLUMNS.Fanart, COLUMNS.Homepage, COLUMNS.ContentProvider, COLUMNS.InsertDate, COLUMNS.UpdateDate};
    }

    @Override
    public void LoadWithCursorRow(Cursor cursor) {
        try {
            if (cursor != null) {
                setID(cursor.getInt(cursor.getColumnIndex(COLUMNS._ID)));
                setSeriesName(cursor.getString(cursor.getColumnIndex(COLUMNS.SeriesName)));
                setOverview(cursor.getString(cursor.getColumnIndex(COLUMNS.Overview)));
                setFirstAired(cursor.getString(cursor.getColumnIndex(COLUMNS.FirstAired)));
                setNetwork(cursor.getString(cursor.getColumnIndex(COLUMNS.Network)));
                setImdbId(cursor.getString(cursor.getColumnIndex(COLUMNS.ImdbId)));
                setTmdbId(cursor.getInt(cursor.getColumnIndex(COLUMNS.TmdbId)));
                setTvdbId(cursor.getInt(cursor.getColumnIndex(COLUMNS.TvdbId)));
                setTraktId(cursor.getInt(cursor.getColumnIndex(COLUMNS.TraktId)));
                setImdbUserRating(cursor.getString(cursor.getColumnIndex(COLUMNS.ImdbUserRating)));
                setImdbVotes(cursor.getString(cursor.getColumnIndex(COLUMNS.ImdbVotes)));
                setTmdbUserRating(cursor.getString(cursor.getColumnIndex(COLUMNS.TmdbUserRating)));
                setTmdbVotes(cursor.getString(cursor.getColumnIndex(COLUMNS.TmdbVotes)));
                setLanguage(cursor.getString(cursor.getColumnIndex(COLUMNS.Language)));
                setCountry(cursor.getString(cursor.getColumnIndex(COLUMNS.Country)));
                setGenres(cursor.getString(cursor.getColumnIndex(COLUMNS.Genres)));
                setRuntime(cursor.getString(cursor.getColumnIndex(COLUMNS.Runtime)));
                setCertification(cursor.getString(cursor.getColumnIndex(COLUMNS.Certification)));
                setAirDay(cursor.getString(cursor.getColumnIndex(COLUMNS.AirDay)));
                setAirTime(cursor.getString(cursor.getColumnIndex(COLUMNS.AirTime)));
                setAirYear(cursor.getInt(cursor.getColumnIndex(COLUMNS.AirYear)));
                setTimezone(cursor.getString(cursor.getColumnIndex(COLUMNS.Timezone)));
                setStatus(cursor.getString(cursor.getColumnIndex(COLUMNS.Status)));
                setRating(cursor.getDouble(cursor.getColumnIndex(COLUMNS.Rating)));
                setVotes(cursor.getInt(cursor.getColumnIndex(COLUMNS.Votes)));
                setSeriesLastUpdate(cursor.getString(cursor.getColumnIndex(COLUMNS.SeriesLastUpdate)));
                setPoster(cursor.getString(cursor.getColumnIndex(COLUMNS.Poster)));
                setFanart(cursor.getString(cursor.getColumnIndex(COLUMNS.Fanart)));
                setHomepage(cursor.getString(cursor.getColumnIndex(COLUMNS.Homepage)));
                setContentProvider(cursor.getInt(cursor.getColumnIndex(COLUMNS.ContentProvider)));
                setInsertDate(cursor.getString(cursor.getColumnIndex(COLUMNS.InsertDate)));
                setUpdateDate(cursor.getString(cursor.getColumnIndex(COLUMNS.UpdateDate)));
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

    public boolean getIsExisting() {
        return mIsExisting;
    }

    public void setIsExisting(boolean isExisting) {
        mIsExisting = isExisting;
    }

    public String getFirstCharacter() {
        return mFirstCharacter;
    }

    public void setFirstCharacter(String FirstCharacter) {
        this.mFirstCharacter = FirstCharacter;
    }

    public ArrayList<Backdrop> getBackdrops() {
        return mBackdrops;
    }

    public void setBackdrops(ArrayList<Backdrop> Backdrops) {
        this.mBackdrops = Backdrops;
    }

    public ArrayList<Cast> getCast() {
        return mCast;
    }

    public void setCast(ArrayList<Cast> Cast) {
        this.mCast = Cast;
    }

    public ArrayList<Season> getSeasons() {
        return mSeasons;
    }

    public void setSeasons(ArrayList<Season> _Seasons) {
        this.mSeasons = _Seasons;
    }

    public ArrayList<Episode> getEpisodes() {
        return mEpisodes;
    }

    public void setEpisodes(ArrayList<Episode> _Episodes) {
        this.mEpisodes = _Episodes;
    }

    public String getDateInfo() {
        return mDateInfo;
    }

    public void setDateInfo(String dateInfo) {
        this.mDateInfo = dateInfo;
    }

    public int getEpisodeCount() {
        return mEpisodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.mEpisodeCount = episodeCount;
    }

    public int getWatchedCount() {
        return mWatchedCount;
    }

    public void setWatchedCount(int watchedCount) {
        this.mWatchedCount = watchedCount;
    }

    public String getEpisodeName() {
        return mEpisodeName;
    }

    public void setEpisodeName(String episodeName) {
        this.mEpisodeName = episodeName;
    }

    public int getCollectedCount() {
        return mCollectedCount;
    }

    public void setCollectedCount(int collectedCount) {
        this.mCollectedCount = collectedCount;
    }

    public ArrayList<Comment> getComments() {
        return mComments;
    }

    public void setComments(ArrayList<Comment> Comments) {
        this.mComments = Comments;
    }

    public void addComments(ArrayList<Comment> Comments) {
        if (mComments == null)
            this.mComments = Comments;
        else
            this.mComments.addAll(Comments);
    }

    public boolean getIsSelected() {
        return mIsSelected;
    }

    public void setIsSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public static class COLUMNS {
        public static final String _ID = "_id";
        public static final String SeriesName = "SeriesName";
        public static final String Network = "Network";
        public static final String ImdbId = "ImdbId";
        public static final String TraktId = "TraktId";
        public static final String AirDay = "AirDay";
        public static final String AirTime = "AirTime";
        public static final String Status = "Status";
        public static final String Poster = "Poster";
        static final String Overview = "Overview";
        static final String FirstAired = "FirstAired";
        static final String TmdbId = "TmdbId";
        static final String TvdbId = "TvdbId";
        static final String ImdbUserRating = "ImdbUserRating";
        static final String ImdbVotes = "ImdbVotes";
        static final String TmdbUserRating = "TmdbUserRating";
        static final String TmdbVotes = "TmdbVotes";
        static final String Language = "Language";
        static final String Country = "Country";
        static final String Genres = "Genres";
        static final String Runtime = "Runtime";
        static final String Certification = "Certification";
        static final String AirYear = "AirYear";
        static final String Timezone = "Timezone";
        static final String Rating = "Rating";
        static final String Votes = "Votes";
        static final String SeriesLastUpdate = "SeriesLastUpdate";
        static final String Fanart = "Fanart";
        static final String Homepage = "Homepage";
        static final String ContentProvider = "ContentProvider";
        static final String InsertDate = "InsertDate";
        static final String UpdateDate = "UpdateDate";
    }
}
