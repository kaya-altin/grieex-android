package com.grieex.model.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;

import java.io.Serializable;

public class Imdb250 implements IDataModelObject, Serializable {
    private static final String TAG = Imdb250.class.getName();
    private static final String TABLE_NAME = "Imdb250";

    class COLUMNS {
        static final String _ID = "_id";
        static final String RANK = "Rank";
        static final String TITLE = "Title";
        static final String RATING = "Rating";
        static final String VOTES = "Votes";
        static final String ImageLink = "ImageLink";
        static final String IMDB_NUMBER = "ImdbNumber";
        static final String TYPE = "Type";
    }

    public Imdb250() {

    }

    public Imdb250(int Rank, String Title, String Rating, int Votes, String ImageLink, String ImdbNumber, int Type) {
        this.mRank = Rank;
        this.mTitle = Title;
        this.mRating = Rating;
        this.mVotes = Votes;
        this.mImageLink = ImageLink;
        this.mImdbNumber = ImdbNumber;
        this.mType = Type;
    }

    private int _id;
    private Integer mRank;
    private String mTitle;
    private String mRating;
    private Integer mVotes;
    private String mImageLink;
    private String mImdbNumber;
    private Integer mType;

    public int getID() {
        return _id;
    }

    private void setID(int _id) {
        this._id = _id;
    }

    public Integer getRank() {
        return mRank;
    }

    private void setRank(Integer Rank) {
        this.mRank = Rank;
    }

    public String getTitle() {
        return mTitle;
    }

    private void setTitle(String Title) {
        this.mTitle = Title;
    }

    public String getRating() {
        return mRating;
    }

    private void setRating(String Rating) {
        this.mRating = Rating;
    }

    public Integer getVotes() {
        return mVotes;
    }

    private void setVotes(Integer Votes) {
        this.mVotes = Votes;
    }

    public String getImageLink() {
        return mImageLink;
    }

    private void setImageLink(String ImageLink) {
        this.mImageLink = ImageLink;
    }

    public String getImdbNumber() {
        return mImdbNumber;
    }

    private void setImdbNumber(String ImdbNumber) {
        this.mImdbNumber = ImdbNumber;
    }

    public Integer getType() {
        return mType;
    }

    private void setType(Integer Type) {
        this.mType = Type;
    }

    public ContentValues GetContentValuesForDB() {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.RANK, mRank);
        values.put(COLUMNS.TITLE, mTitle);
        values.put(COLUMNS.RATING, mRating);
        values.put(COLUMNS.VOTES, mVotes);
        values.put(COLUMNS.ImageLink, mImageLink);
        values.put(COLUMNS.IMDB_NUMBER, mImdbNumber);
        values.put(COLUMNS.TYPE, mType);

        return values;
    }

    public String GetTableName() {
        // TODO Auto-generated method stub
        return "Imdb250";
    }

    public String[] GetColumnMapping() {
        return new String[]{COLUMNS._ID, COLUMNS.RANK, COLUMNS.TITLE, COLUMNS.RATING, COLUMNS.VOTES, COLUMNS.ImageLink, COLUMNS.IMDB_NUMBER, COLUMNS.TYPE};
    }

    @Override
    public void LoadWithCursorRow(Cursor cursor) {
        try {
            if (cursor != null) {
                setID(cursor.getInt(cursor.getColumnIndex(COLUMNS._ID)));
                setRank(cursor.getInt(cursor.getColumnIndex(COLUMNS.RANK)));
                setTitle(cursor.getString(cursor.getColumnIndex(COLUMNS.TITLE)));
                setRating(cursor.getString(cursor.getColumnIndex(COLUMNS.RATING)));
                setVotes(cursor.getInt(cursor.getColumnIndex(COLUMNS.VOTES)));
                setImageLink(cursor.getString(cursor.getColumnIndex(COLUMNS.ImageLink)));
                setImdbNumber(cursor.getString(cursor.getColumnIndex(COLUMNS.IMDB_NUMBER)));
                setType(cursor.getInt(cursor.getColumnIndex(COLUMNS.TYPE)));
            }
        } catch (Exception e) {
            NLog.e("MovieImdb250", e);
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

    // Custom Fields
    private boolean mIsExisting = false;

    public void setIsExisting(boolean isExisting) {
        mIsExisting = isExisting;
    }

    public boolean getIsExisting() {
        return mIsExisting;
    }
}
