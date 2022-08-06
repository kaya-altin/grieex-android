package com.grieex.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import 	androidx.preference.PreferenceManager;

import com.grieex.helper.Constants;

public class ServiceManager {

	public static void setImportDataServiceState(Context ctx, int iState) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = pref.edit();

		editor.putInt(Constants.STATE_IMPORT_DATA_SERVICE, iState);
		editor.apply();
	}

	public static int getImportDataServiceState(Context ctx) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
		return pref.getInt(Constants.STATE_IMPORT_DATA_SERVICE, -1);
	}

	// ******************** ImportDataService ********************

	private static boolean IsRunningImportDataService(Context ctx) {
		ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (ImportDataService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static void startImportDataService(Context ctx) {
		if (!IsRunningImportDataService(ctx)) {
			Intent serviceIntent = new Intent(ctx, ImportDataService.class);
			ctx.startService(serviceIntent);
		}
	}

	public static void stopImportDataService() {

	}

	// ******************** ImdbBulkUpdateService ********************

	public static boolean IsRunningImdbBulkUpdateService(Context ctx) {
		ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (BatchProcessingService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static void startImdbBulkUpdateService(Context ctx) {
		if (!IsRunningImdbBulkUpdateService(ctx)) {
			Intent serviceIntent = new Intent(ctx, BatchProcessingService.class);
			ctx.startService(serviceIntent);
		}
	}

	public static void stopImdbBulkUpdateService(Context ctx) {
		Intent serviceIntent = new Intent(ctx, BatchProcessingService.class);
		serviceIntent.putExtra("action", "cancel");
		ctx.startService(serviceIntent);
	}

}
