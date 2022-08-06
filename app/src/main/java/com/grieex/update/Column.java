package com.grieex.update;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;

import java.io.Serializable;

public class Column implements IDataModelObject, Serializable {
	class COLUMNS {
		static final String TYPE = "type";
		static final String NAME = "name";
		static final String NOT_NULL = "notnull";
		static final String DEFAULT_VALUE = "dflt_value";
	}

	public Column() {

	}

	public Column(String Name, String Type, int NotNull, String DefaultValue) {
		this.mName = Name;
		this.mType = Type;
		this.mNotNull = NotNull;
		this.mDefaultValue = DefaultValue;
	}

	private String mName;
	private String mType;
	private int mNotNull = 0;
	private String mDefaultValue;

	public String getType() {
		return mType;
	}

	private void setType(String Type) {
		this.mType = Type;
	}

	public String getName() {
		return mName;
	}

	private void setName(String Name) {
		this.mName = Name;
	}

	public int getNotNull() {
		return mNotNull;
	}

	private void setNotNull(int NotNull) {
		this.mNotNull = NotNull;
	}

	public String getDefaultValue() {
		return mDefaultValue;
	}

	private void setDefaultValue(String DefaultValue) {
		this.mDefaultValue = DefaultValue;
	}

	public ContentValues GetContentValuesForDB() {
		ContentValues values = new ContentValues();
		values.put(COLUMNS.NAME, mName);
		values.put(COLUMNS.TYPE, mType);
		values.put(COLUMNS.NOT_NULL, mNotNull);
		values.put(COLUMNS.DEFAULT_VALUE, mDefaultValue);

		return values;
	}

	public String getSql(String tableName) {
		// ALTER TABLE Companies ADD COLUMN DefaultPageID INTEGER
		String returnValue = "ALTER TABLE " + tableName + " ADD COLUMN " + mName + " " + mType;

		if (mNotNull == 1)
			returnValue = returnValue + " NOT NULL ";

		if (mDefaultValue != null)
			returnValue = returnValue + " default " + mDefaultValue;
		return returnValue;
	}

	public String GetTableName() {
		// TODO Auto-generated method stub
		return "tables";
	}

	public String[] GetColumnMapping() {
		return new String[] { COLUMNS.NAME, COLUMNS.TYPE, COLUMNS.NOT_NULL, COLUMNS.DEFAULT_VALUE };
	}

	@Override
	public void LoadWithCursorRow(Cursor cursor) {
		try {
			if (cursor != null) {
				setName(cursor.getString(cursor.getColumnIndex(COLUMNS.NAME)));
				setType(cursor.getString(cursor.getColumnIndex(COLUMNS.TYPE)));
				setNotNull(cursor.getInt(cursor.getColumnIndex(COLUMNS.NOT_NULL)));
				setDefaultValue(cursor.getString(cursor.getColumnIndex(COLUMNS.DEFAULT_VALUE)));
			}
		} catch (Exception e) {
			NLog.e("Table", e);
		}
	}

	@Override
	public void LoadWithWhereColumn(Context ctx, String WhereColumn, String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void LoadWithWhere(Context ctx, String Where) {
		// TODO Auto-generated method stub

	}

}
