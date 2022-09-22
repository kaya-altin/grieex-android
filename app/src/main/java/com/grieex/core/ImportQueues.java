package com.grieex.core;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.grieex.helper.Constants.ContentProviders;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.model.tables.Queue;

import java.util.ArrayList;

public class ImportQueues {
    private static final String TAG = ImportQueues.class.getName();

    private static final String SQL_QUEUES_COUNT = "SELECT COUNT(*) FROM Queues";

    public static int GetQueuesCount(Context ctx) {
        int Count = 0;
        try {
            DatabaseHelper dbHandler = DatabaseHelper.getInstance(ctx);
            String strResult = dbHandler.GetOneField(SQL_QUEUES_COUNT);
            Count = Integer.parseInt(strResult);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return Count;
    }

    public static Queue GetQueue(Context ctx) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);

        ArrayList<Queue> queues;
        try {
            queues = (ArrayList<Queue>) dbHelper.GetCursorWithObject("Select * From Queues LIMIT 0,1", Queue.class);
            if (queues != null && queues.size() > 0)
                return queues.get(0);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    public static void AddQueue(Context ctx, long ObjectID, SearchResult sr, ContentProviders resultType) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);

        try {
            int iCount = Integer.parseInt(dbHelper.GetOneField("Select Count(*) from Queues Where ObjectID=" + ObjectID + " and Type=" + resultType.value));
            if (iCount > 0)
                return;

            ContentValues values = new ContentValues();
            if (resultType == ContentProviders.TMDb || resultType == ContentProviders.TMDbTv) {
                values.put("Url", sr.getKey());
            } else {
                values.put("Url", sr.getUrl());
            }
            values.put("ObjectID", ObjectID);
            values.put("Type", resultType.value);

            dbHelper.ExecuteQueryWithContentValues("Queues", values);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public static void AddQueue(Context ctx, long _ObjectID, String _Url, ContentProviders resultType) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);

        try {
            int iCount = Integer.parseInt(dbHelper.GetOneField("Select Count(*) from Queues Where ObjectID=" + _ObjectID + " and Type=" + resultType.value));
            if (iCount > 0)
                return;

            ContentValues values = new ContentValues();
            values.put("Url", _Url);
            values.put("ObjectID", _ObjectID);
            values.put("Type", resultType.value);

            dbHelper.ExecuteQueryWithContentValues("Queues", values);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public static void RemoveQueue(Context ctx, Queue q) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);
        try {
            dbHelper.ExecuteQuery("Delete From Queues Where _id=" + q.getID());
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }
}
