package com.grieex.update;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.grieex.helper.NLog;
import com.grieex.interfaces.IDataModelObject;

import java.io.Serializable;

public class Table implements IDataModelObject, Serializable {
    private String mType;
    private String mName;
    private String mTblName;
    private String mSql;
    public Table() {

    }
    public Table(String Type, String Name, String TblName, String Sql) {
        this.mType = Type;
        this.mName = Name;
        this.mTblName = TblName;
        this.mSql = Sql;
    }

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

    public String getTblName() {
        return mTblName;
    }

    private void setTblName(String TblName) {
        this.mTblName = TblName;
    }

    public String getSql() {
        return mSql;
    }

    private void setSql(String Sql) {
        this.mSql = Sql;
    }

    public ContentValues GetContentValuesForDB() {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.TYPE, mType);
        values.put(COLUMNS.NAME, mName);
        values.put(COLUMNS.TBL_NAME, mTblName);
        values.put(COLUMNS.SQL, mSql);

        return values;
    }

    public String GetTableName() {
        return mTblName;
    }

    public String[] GetColumnMapping() {
        return new String[]{COLUMNS.TYPE, COLUMNS.NAME, COLUMNS.TBL_NAME, COLUMNS.SQL};
    }

    @Override
    public void LoadWithCursorRow(Cursor cursor) {
        try {
            if (cursor != null) {
                setType(cursor.getString(cursor.getColumnIndex(COLUMNS.TYPE)));
                setName(cursor.getString(cursor.getColumnIndex(COLUMNS.NAME)));
                setTblName(cursor.getString(cursor.getColumnIndex(COLUMNS.TBL_NAME)));
                setSql(cursor.getString(cursor.getColumnIndex(COLUMNS.SQL)));
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

    static class COLUMNS {
        static final String TYPE = "type";
        static final String NAME = "name";
        static final String TBL_NAME = "tbl_name";
        static final String SQL = "sql";
    }

}
