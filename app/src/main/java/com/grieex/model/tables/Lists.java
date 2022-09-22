package com.grieex.model.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;

import java.io.Serializable;

public class Lists implements IDataModelObject, Serializable {
    public static final String TABLE_NAME = "Lists";
    private static final String TAG = Lists.class.getName();
    private int _id;
    private String mListName;
    private String mUpdateDate;
    private int mListType;

    public Lists() {

    }

    public int getID() {
        return _id;
    }

    public void setID(int _id) {
        this._id = _id;
    }

    public String getListName() {
        return mListName;
    }

    public void setListName(String ListName) {
        this.mListName = ListName;
    }

    public String getUpdateDate() {
        return mUpdateDate;
    }

    public void setUpdateDate(String UpdateDate) {
        this.mUpdateDate = UpdateDate;
    }

    public int getListType() {
        return mListType;
    }

    public void setListType(int ListType) {
        this.mListType = ListType;
    }

    public ContentValues GetContentValuesForDB() {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.ListName, mListName);
        values.put(COLUMNS.UpdateDate, mUpdateDate);
        values.put(COLUMNS.ListType, mListType);

        return values;
    }

    public String GetTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    public String[] GetColumnMapping() {
        return new String[]{COLUMNS._ID, COLUMNS.ListName, COLUMNS.UpdateDate, COLUMNS.ListType};
    }

    @Override
    public void LoadWithCursorRow(Cursor cursor) {
        try {
            if (cursor != null) {
                setID(cursor.getInt(cursor.getColumnIndex(COLUMNS._ID)));
                setListName(cursor.getString(cursor.getColumnIndex(COLUMNS.ListName)));
                setUpdateDate(cursor.getString(cursor.getColumnIndex(COLUMNS.UpdateDate)));
                setListType(cursor.getInt(cursor.getColumnIndex(COLUMNS.ListType)));
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

    public String toString() {
        return mListName;
    }

    public static class COLUMNS {
        public static final String _ID = "_id";
        static final String ListName = "ListName";
        static final String UpdateDate = "UpdateDate";
        static final String ListType = "ListType";
    }
}
