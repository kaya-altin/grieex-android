package com.grieex.model.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.grieex.helper.Constants.ContentProviders;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;

import java.io.Serializable;

public class Queue implements IDataModelObject, Serializable {
    public static final String TABLE_NAME = "Queues";
    private static final String TAG = Queue.class.getName();
    private int mID;
    private int mObjectID;
    private String mUrl;
    private ContentProviders mType;

    public int getID() {
        return mID;
    }

    private void setID(int ID) {
        this.mID = ID;
    }

    public int getObjectID() {
        return mObjectID;
    }

    private void setObjectID(int ObjectID) {
        this.mObjectID = ObjectID;
    }

    public String getUrl() {
        return mUrl;
    }

    private void setUrl(String Url) {
        this.mUrl = Url;
    }

    public ContentProviders getType() {
        return mType;
    }

    private void setType(ContentProviders Type) {
        this.mType = Type;
    }

    @Override
    public String GetTableName() {
        return TABLE_NAME;
    }

    @Override
    public ContentValues GetContentValuesForDB() {
        ContentValues values = new ContentValues();
        values.put(COLUMNS._ID, mID);
        values.put(COLUMNS.ObjectID, mObjectID);
        values.put(COLUMNS.Url, mUrl);
        values.put(COLUMNS.Type, mType.value);

        return values;
    }

    @Override
    public String[] GetColumnMapping() {
        return new String[]{COLUMNS._ID, COLUMNS.ObjectID, COLUMNS.Url, COLUMNS.Type};
    }

    @Override
    public void LoadWithCursorRow(Cursor cursor) {
        try {
            if (cursor != null) {
                setID(cursor.getInt(cursor.getColumnIndex(COLUMNS._ID)));
                setObjectID(cursor.getInt(cursor.getColumnIndex(COLUMNS.ObjectID)));
                setUrl(cursor.getString(cursor.getColumnIndex(COLUMNS.Url)));
                setType(ContentProviders.fromValue(cursor.getInt(cursor.getColumnIndex(COLUMNS.Type))));
            }
        } catch (Exception e) {
            Log.e("Queue", e.getMessage());
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
        public static final String ObjectID = "ObjectID";
        static final String _ID = "_id";
        static final String Url = "Url";
        static final String Type = "Type";
    }
}
