package com.grieex.interfaces;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public interface IDataModelObject {

	String GetTableName();

	ContentValues GetContentValuesForDB();

	String[] GetColumnMapping();

	void LoadWithCursorRow(Cursor cursor);

	void LoadWithWhereColumn(Context ctx, String WhereColumn, String id);

	void LoadWithWhere(Context ctx, String Where);

}
