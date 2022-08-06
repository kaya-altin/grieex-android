package com.grieex.helper;

import android.os.Handler;
import android.os.Looper;

final class MainHandler {

  private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

  private MainHandler() {
  }

  public static void post(Runnable r) {
    MAIN_HANDLER.post(r);
  }

  public static void postDelayed(Runnable r, long delayMillis) {
    MAIN_HANDLER.postDelayed(r, delayMillis);
  }

  public static void removeCallbacks(Runnable r) {
    MAIN_HANDLER.removeCallbacks(r);
  }
}
