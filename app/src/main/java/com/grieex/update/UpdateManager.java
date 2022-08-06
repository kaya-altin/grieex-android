package com.grieex.update;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;

import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.GrieeXSettings;
import com.grieex.ui.GrieeXUpdateActivity;

public class UpdateManager {

	public static void Start(Context ctx) {
		Intent i = new Intent(ctx, GrieeXUpdateActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ctx.startActivity(i);
	}

	public static boolean NewVersionFound(Context ctx) {
		int version = GetDatabaseVersion(ctx);

        return version < GrieeXSettings.DB_VERSION;
    }

	public static boolean IsRunningUpdateManagerService(Context ctx) {
		ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (GrieeXUpdateService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static int GetDatabaseVersion(Context ctx) {
		int iVersion = GrieeXSettings.DB_VERSION;

		try {
			DatabaseHelper dbHandler = DatabaseHelper.getInstance(ctx);
			iVersion = Integer.parseInt(dbHandler.GetOneField("Select version From android_database_version"));
		} catch (Exception e) {
			// e.printStackTrace();
			iVersion = 1;
			CreateDatabaseVersion(ctx, iVersion);
		}

		return iVersion;
	}

	private static void CreateDatabaseVersion(Context ctx, int iVersion) {
		try {
			DatabaseHelper dbHandler = DatabaseHelper.getInstance(ctx);
			dbHandler.ExecuteQueries(new String[] { "DROP TABLE IF EXISTS android_database_version", "CREATE TABLE \"android_database_version\" (\"version\" INTEGER DEFAULT 1)", "INSERT INTO android_database_version (version) VALUES (" + iVersion + ")" });
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
