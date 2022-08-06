package com.grieex.ui;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.grieex.GrieeX;
import com.grieex.helper.GrieeXSettings;

import java.util.Locale;


public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle arg) {
        setCustomTheme();
        super.onCreate(arg);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateBaseContextLocale(base));
    }

    private Context updateBaseContextLocale(Context context) {
        String language = GrieeXSettings.getLocale(context);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResourcesLocale(context, locale);
        }

        return updateResourcesLocaleLegacy(context, locale);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Context updateResourcesLocale(Context context, Locale locale) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

    private Context updateResourcesLocaleLegacy(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }

    private void setCustomTheme() {
        setTheme(GrieeXSettings.getTheme(this));
    }



    @Override
    protected void onStart() {
        super.onStart();

        if (GrieeXSettings.RELEASE_MODE) {
            GrieeX.getInstance().trackScreenView(this.getClass().getName());
        }
    }

}
