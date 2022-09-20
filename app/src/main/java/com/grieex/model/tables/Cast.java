package com.grieex.model.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;
import com.grieex.model.TransactionObject;

import java.io.Serializable;

public class Cast extends TransactionObject implements IDataModelObject, Serializable {
    public static final String TABLE_NAME = "Casts";
    private static final String TAG = Cast.class.getName();
    private int _id;
    private String mName;
    private String mCharacter;
    private String mUrl;
    private String mImageUrl = "";
    private String mCastID;
    private String mObjectID;
    private Integer mCollectionType = 1;
    public Cast() {

    }
    public Cast(String Name, String Character, String Url, String ImageUrl, String CastID, String ObjectID, Constants.CollectionType type) {
        this.mName = Name;
        this.mCharacter = Character;
        this.mUrl = Url;
        this.mImageUrl = ImageUrl;
        this.mCastID = CastID;
        this.mObjectID = ObjectID;
        this.mCollectionType = type.value;
    }

    public int getID() {
        return _id;
    }

    private void setID(int _id) {
        this._id = _id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String Name) {
        this.mName = Name;
    }

    public String getCharacter() {
        return mCharacter;
    }

    public void setCharacter(String Character) {
        this.mCharacter = Character;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String Url) {
        this.mUrl = Url;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String ImageUrl) {
        this.mImageUrl = ImageUrl;
    }

    public String getCastID() {
        return mCastID;
    }

    public void setCastID(String CastID) {
        this.mCastID = CastID;
    }

    public String getObjectID() {
        return mObjectID;
    }

    private void setObjectID(String ObjectID) {
        this.mObjectID = ObjectID;
    }

    public Integer getCollectionType() {
        return mCollectionType;
    }

    private void setCollectionType(Integer type) {
        this.mCollectionType = type;
    }

    public ContentValues GetContentValuesForDB() {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.Name, mName);
        values.put(COLUMNS.Character, mCharacter);
        values.put(COLUMNS.Url, mUrl);
        values.put(COLUMNS.ImageUrl, mImageUrl);
        values.put(COLUMNS.CastID, mCastID);
        values.put(COLUMNS.ObjectID, mObjectID);
        values.put(COLUMNS.CollectionType, mCollectionType);

        return values;
    }

    public String GetTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    public String[] GetColumnMapping() {
        return new String[]{COLUMNS._ID, COLUMNS.Name, COLUMNS.Character, COLUMNS.Url, COLUMNS.ImageUrl, COLUMNS.CastID, COLUMNS.ObjectID, COLUMNS.CollectionType};
    }

    @Override
    public void LoadWithCursorRow(Cursor cursor) {
        try {
            if (cursor != null) {
                setID(cursor.getInt(cursor.getColumnIndex(COLUMNS._ID)));
                setName(cursor.getString(cursor.getColumnIndex(COLUMNS.Name)));
                setCharacter(cursor.getString(cursor.getColumnIndex(COLUMNS.Character)));
                setUrl(cursor.getString(cursor.getColumnIndex(COLUMNS.Url)));
                setImageUrl(cursor.getString(cursor.getColumnIndex(COLUMNS.ImageUrl)));
                setCastID(cursor.getString(cursor.getColumnIndex(COLUMNS.CastID)));
                setObjectID(cursor.getString(cursor.getColumnIndex(COLUMNS.ObjectID)));
                setCollectionType(cursor.getInt(cursor.getColumnIndex(COLUMNS.CollectionType)));
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

    public static class COLUMNS {
        public static final String _ID = "_id";
        public static final String Name = "Name";
        public static final String Character = "Character";
        public static final String Url = "Url";
        public static final String ImageUrl = "ImageUrl";
        public static final String CastID = "CastID";
        public static final String ObjectID = "ObjectID";
        public static final String CollectionType = "CollectionType";
    }
}
