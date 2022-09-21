package com.grieex.update;

import android.content.Context;

import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;

import java.util.ArrayList;

class DatabaseMerge {
    // SELECT * FROM dbname.sqlite_master WHERE type='table'
    // PRAGMA table_info(table_name);
    private static final String TAG = DatabaseMerge.class.getName();
    private final Context mContext;

    private DatabaseMerge(Context context) {
        mContext = context;
    }

    public void start() {
        DatabaseHelper dbHandlerCurrent = DatabaseHelper.getInstance(mContext);
        MergeDatabaseHelper dbHandlerAsset = MergeDatabaseHelper.getInstance(mContext);

        try {
            ArrayList<Table> CurrentTables = (ArrayList<Table>) dbHandlerCurrent.GetCursorWithObject("SELECT * FROM main.sqlite_master WHERE type='table'", Table.class);
            ArrayList<Table> AssetTables = (ArrayList<Table>) dbHandlerAsset.GetCursorWithObject("SELECT * FROM main.sqlite_master WHERE type='table'", Table.class);

            for (Table tableAsset : AssetTables) {
                boolean bExistTable = false; // CurrentTables.equals(table);
                for (Table tableCurrent : CurrentTables) {
                    if (tableAsset.getTblName().equals(tableCurrent.getTblName())) {
                        bExistTable = true;

                        ArrayList<Column> CurrentColumns = (ArrayList<Column>) dbHandlerCurrent.GetCursorWithObject("PRAGMA table_info(" + tableCurrent.GetTableName() + ")", Column.class);
                        ArrayList<Column> AssetColumns = (ArrayList<Column>) dbHandlerAsset.GetCursorWithObject("PRAGMA table_info(" + tableAsset.GetTableName() + ");", Column.class);

                        for (Column columnAsset : AssetColumns) {
                            boolean bExistColumn = false;
                            for (Column columnCurrent : CurrentColumns) {
                                if (columnAsset.getName().equals(columnCurrent.getName())) {
                                    bExistColumn = true;
                                    break;
                                }
                                // break;
                            }

                            if (!bExistColumn) {
                                dbHandlerCurrent.ExecuteQuery(columnAsset.getSql(tableAsset.GetTableName()));
                            }
                        }
                        break;
                    }
                }

                if (!bExistTable)
                    dbHandlerCurrent.ExecuteQuery(tableAsset.getSql());
            }

        } catch (Exception e) {
            NLog.e(TAG, e);
        } finally {
            dbHandlerAsset.close();
        }

    }
}
