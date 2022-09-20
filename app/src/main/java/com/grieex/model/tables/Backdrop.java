package com.grieex.model.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;

import java.io.Serializable;

public class Backdrop implements IDataModelObject, Serializable {
    public static final String TABLE_NAME = "Backdrops";
    private static final String TAG = Backdrop.class.getName();
    private int _id;
    private String mUrl;
    private String mImdbNumber;
    private Integer mObjectID;
    private Integer mCollectionType;
    public Backdrop() {

    }
    public Backdrop(String Url, String ImdbNumber, int ObjectID, Constants.CollectionType collectionType) {
        this.mUrl = Url;
        this.mImdbNumber = ImdbNumber;
        this.mObjectID = ObjectID;
        this.mCollectionType = collectionType.value;
    }

    public int getID() {
        return _id;
    }

    private void setID(int _id) {
        this._id = _id;
    }

    public String getUrl() {
        return mUrl;
    }

    private void setUrl(String Url) {
        this.mUrl = Url;
    }

    public String getImdbNumber() {
        return mImdbNumber;
    }

    private void setImdbNumber(String ImdbNumber) {
        this.mImdbNumber = ImdbNumber;
    }

    public Integer getObjectID() {
        return mObjectID;
    }

    private void setObjectID(Integer ObjectID) {
        this.mObjectID = ObjectID;
    }

    public Integer getCollectionType() {
        return mCollectionType;
    }

    private void setCollectionType(Integer collectionType) {
        this.mCollectionType = collectionType;
    }

    public ContentValues GetContentValuesForDB() {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.Url, mUrl);
        values.put(COLUMNS.ImdbNumber, mImdbNumber);
        values.put(COLUMNS.ObjectID, mObjectID);
        values.put(COLUMNS.CollectionType, mCollectionType);

        return values;
    }

    public String GetTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    public String[] GetColumnMapping() {
        return new String[]{COLUMNS._ID, COLUMNS.Url, COLUMNS.ImdbNumber, COLUMNS.ObjectID, COLUMNS.CollectionType};
    }

    @Override
    public void LoadWithCursorRow(Cursor cursor) {
        try {
            if (cursor != null) {
                setID(cursor.getInt(cursor.getColumnIndex(COLUMNS._ID)));
                setUrl(cursor.getString(cursor.getColumnIndex(COLUMNS.Url)));
                setImdbNumber(cursor.getString(cursor.getColumnIndex(COLUMNS.ImdbNumber)));
                setObjectID(cursor.getInt(cursor.getColumnIndex(COLUMNS.ObjectID)));
                setCollectionType(cursor.getInt(cursor.getColumnIndex(COLUMNS.CollectionType)));
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
        public static final String Url = "Url";
        public static final String ImdbNumber = "ImdbNumber";
        public static final String ObjectID = "ObjectID";
        public static final String CollectionType = "CollectionType";
        static final String _ID = "_id";
    }
}
