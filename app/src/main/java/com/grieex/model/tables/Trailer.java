package com.grieex.model.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;

import java.io.Serializable;

public class Trailer implements IDataModelObject, Serializable {
	private static final String TAG = Trailer.class.getName();
	public static final String TABLE_NAME = "Trailers";

	public class COLUMNS {
		static final String _ID = "_id";
		public static final String ObjectID = "ObjectID";
		public static final String Url = "Url";
		public static final String Type = "Type";
		public static final String CollectionType = "CollectionType";
	}

	public Trailer() {

	}

	public Trailer(int ObjectID, String Url, String Type, Constants.CollectionType collectionType) {
		this.mObjectID = ObjectID;
		this.mUrl = Url;
		this.mType = Type;
		this.mCollectionType = collectionType.value;
	}

	private int mID;
	private Integer mObjectID;
	private String mUrl;
	private String mType;
	private Integer mCollectionType;

	private void setID(int ID) {
		this.mID = ID;
	}

	public int getID() {
		return mID;
	}

	private void setObjectID(Integer ObjectID) {
		this.mObjectID = ObjectID;
	}

	public Integer getObjectID() {
		return mObjectID;
	}

	private void setUrl(String Url) {
		this.mUrl = Url;
	}

	public String getUrl() {
		return mUrl;
	}

	private void setType(String Type) {
		this.mType = Type;
	}

	public String getType() {
		return mType;
	}

	public Integer getCollectionType() {
		return mCollectionType;
	}

	private void setCollectionType(Integer collectionType) {
		this.mCollectionType = collectionType;
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
		values.put(COLUMNS.Type, mType);
		values.put(COLUMNS.CollectionType, mCollectionType);

		return values;
	}

	@Override
	public String[] GetColumnMapping() {
		return new String[] { COLUMNS._ID, COLUMNS.ObjectID, COLUMNS.Url, COLUMNS.Type, COLUMNS.CollectionType };
	}

	@Override
	public void LoadWithCursorRow(Cursor cursor) {
		try {
			if (cursor != null) {
				setID(cursor.getInt(cursor.getColumnIndex(COLUMNS._ID)));
				setObjectID(cursor.getInt(cursor.getColumnIndex(COLUMNS.ObjectID)));
				setUrl(cursor.getString(cursor.getColumnIndex(COLUMNS.Url)));
				setType(cursor.getString(cursor.getColumnIndex(COLUMNS.Type)));
				setCollectionType(cursor.getInt(cursor.getColumnIndex(COLUMNS.CollectionType)));
			}
		} catch (Exception e) {
			Log.e("Trailers", e.getMessage());
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
}
