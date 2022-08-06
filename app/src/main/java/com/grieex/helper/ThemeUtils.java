/*
 * Copyright 2015 Uwe Trottmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grieex.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Helper methods to support SeriesGuide's different themes.
 */
public class ThemeUtils {

    public static final String Theme_Default = "Default";
    public static final String Theme_Indigo = "Indigo";
    public static final String Theme_Red = "Red";
    public static final String Theme_Brown = "Brown";
    public static final String Theme_Purple = "Purple";
    public static final String Theme_DeepPurple = "Deep Purple";
    public static final String Theme_Teal = "Teal";
    public static final String Theme_Green = "Green";
    public static final String Theme_LightGreen = "Light Green";
    public static final String Theme_BlueGrey = "Blue Grey";

    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void changeToTheme(Context ctx, int themeId) {
        GrieeXSettings.setTheme(ctx, themeId);

        Intent i = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(i);
    }

    /**
     * Sets the global app theme variable. Applied by all activities once they are created.
     */
    public static synchronized void updateTheme(String themeIndex) {
        //int theme = Integer.valueOf(themeIndex);
//        switch (theme) {
//            case 1:
//                SeriesGuidePreferences.THEME = R.style.Theme_SeriesGuide_DarkBlue;
//                break;
//            case 2:
//                SeriesGuidePreferences.THEME = R.style.Theme_SeriesGuide_Light;
//                break;
//            default:
//                SeriesGuidePreferences.THEME = R.style.Theme_SeriesGuide;
//                break;
//        }
    }

    /**
     * Applies an immersive theme (translucent status bar) to the given activity.
     */
    public static void setImmersiveTheme(Activity activity) {
//        if (SeriesGuidePreferences.THEME == R.style.Theme_SeriesGuide_Light) {
//            activity.setTheme(R.style.Theme_SeriesGuide_Light_Immersive);
//        } else if (SeriesGuidePreferences.THEME == R.style.Theme_SeriesGuide_DarkBlue) {
//            activity.setTheme(R.style.Theme_SeriesGuide_DarkBlue_Immersive);
//        } else {
//            activity.setTheme(R.style.Theme_SeriesGuide_Immersive);
//        }
    }
}
