package com.grieex.helper;

import android.util.Log;


import java.util.logging.Level;
import java.util.logging.Logger;

public class NLog {
	private static final Level LOGGING_LEVEL = Level.ALL;
	private static final String TAG = NLog.class.getName();

	public static void setLoggingLevel() {
		Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);
	}

	private static int e(String tag, String msg, Throwable tr) {
	//	acraReport(Level.SEVERE, tag, msg, tr);
		//Crashlytics.logException(tr);

		return Log.e(tag, msg, tr);
	}

	public static int e(String msg, Throwable e) {
		return e(TAG, msg, e);
	}

	public static int e(Throwable tr) {
		return e(TAG, tr.getMessage(), tr);
	}

	private static int w(String tag, String msg, Throwable tr) {
		//acraReport(Level.WARNING, tag, msg, tr);

		return Log.w(tag, msg, tr);
	}

	public static int w(String msg, Throwable tr) {
		return w(TAG, msg, tr);
	}

	private static int w(String tag, String msg) {
		//acraReport(Level.WARNING, tag, msg);

		return Log.w(tag, msg);
	}

	public static int w(String msg) {
		return w(TAG, msg);
	}

	private static int i(String tag, String msg, Throwable tr) {
		//acraReport(Level.INFO, tag, msg, tr);

		return Log.i(tag, msg, tr);
	}

	public static int i(String msg, Throwable tr) {
		return i(TAG, msg, tr);
	}

	private static int i(String tag, String msg) {
		//acraReport(Level.INFO, tag, msg);

		return Log.i(tag, msg);
	}

	public static int i(String msg) {
		return i(TAG, msg);
	}

	private static int d(String tag, String msg) {
		//acraReport(Level.FINEST, tag, msg);

		return Log.d(tag, msg);
	}

	public static int d(String msg) {
		return d(TAG, msg);
	}

	private static int v(String tag, String msg, Throwable tr) {
		//acraReport(Level.FINE, tag, msg, tr);

		return Log.v(tag, msg, tr);
	}

	public static int v(String msg, Throwable tr) {
		return v(TAG, msg, tr);
	}

	private static int v(String tag, String msg) {
		//acraReport(Level.FINE, tag, msg);

		return Log.v(tag, msg);
	}

	public static int v(String msg) {
		return v(TAG, msg);
	}

//	private static void acraReport(Level level, String tag, String msg, Throwable tr) {
//		if (GrieeXSettings.DEBUG_MODE)
//			return;
//
//		if (level.intValue() >= LOGGING_LEVEL.intValue()) {
//			acraReport(level, tag, msg);
//			// report exception to acra silently
//			ErrorReporter.getInstance().handleSilentException(tr);
//		}
//	}
//
//	private static void acraReport(Level level, String tag, String msg) {
//		if (GrieeXSettings.DEBUG_MODE)
//			return;
//
//		if (level.intValue() >= LOGGING_LEVEL.intValue()) {
//			ErrorReporter.getInstance().putCustomData(level.getName() + ": " + tag, msg);
//		}
//	}
}
