package com.grieex.model.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;

import java.io.Serializable;

public class ListsSeries implements IDataModelObject, Serializable {
    public static final String TABLE_NAME = "ListsSeries";
    private static final String TAG = ListsSeries.class.getName();
    private int _id;
    private String mListID;
    private String mSeriesID;
    public ListsSeries() {

    }

    public int getID() {
        return _id;
    }

    private void setID(int _id) {
        this._id = _id;
    }

    public String getListID() {
        return mListID;
    }

    public void setListID(String ListID) {
        this.mListID = ListID;
    }

    public String getSeriesID() {
        return mSeriesID;
    }

    public void setSeriesID(String SeriesID) {
        this.mSeriesID = SeriesID;
    }

    public ContentValues GetContentValuesForDB() {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.ListID, mListID);
        values.put(COLUMNS.SeriesID, mSeriesID);

        return values;
    }

    public String GetTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    public String[] GetColumnMapping() {
        return new String[]{COLUMNS._ID, COLUMNS.ListID, COLUMNS.SeriesID};
    }

    @Override
    public void LoadWithCursorRow(Cursor cursor) {
        try {
            if (cursor != null) {
                setID(cursor.getInt(cursor.getColumnIndex(COLUMNS._ID)));
                setListID(cursor.getString(cursor.getColumnIndex(COLUMNS.ListID)));
                setSeriesID(cursor.getString(cursor.getColumnIndex(COLUMNS.SeriesID)));
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

    public static class COLUMNS {
        public static final String ListID = "ListID";
        public static final String SeriesID = "SeriesID";
        static final String _ID = "_id";
    }
}
