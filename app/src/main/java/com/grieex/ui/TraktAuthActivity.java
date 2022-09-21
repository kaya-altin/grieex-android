package com.grieex.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.grieex.R;
import com.grieex.enums.TraktResult;
import com.grieex.helper.Constants;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.tasks.ConnectTraktTask;
import com.uwetrottmann.trakt5.TraktV2;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Griee on 18.03.2016.
 */
public class TraktAuthActivity extends AppCompatActivity {

    static final String QUERY_CODE = "code";
    private static final String TAG = TraktAuthActivity.class.getName();
    private static final int REQUEST_OAUTH = 1;

    private Button btnLogin;
    private LinearLayout container;
    private LinearLayout progressContainer;
    private TextView description;
    private TextView success_message;
    private TextView error_message;

    private CustomBroadcastReceiver mCustomBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trakt_auth);

        container = findViewById(R.id.container);
        progressContainer = findViewById(R.id.progressContainer);
        description = findViewById(R.id.description);
        success_message = findViewById(R.id.success_message);
        error_message = findViewById(R.id.error_message);

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent authorize = new Intent(TraktAuthActivity.this, OauthWebViewActivity.class);
                    authorize.putExtra(OauthWebViewActivity.AUTHORIZATION_URL, getAuthorizationUrl());
                    startActivityForResult(authorize, REQUEST_OAUTH);
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
            }
        });

        if (mCustomBroadcastReceiver == null) {
            IntentFilter statusIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
            statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
            mCustomBroadcastReceiver = new CustomBroadcastReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(mCustomBroadcastReceiver, statusIntentFilter);
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (mCustomBroadcastReceiver != null) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mCustomBroadcastReceiver);
                mCustomBroadcastReceiver = null;
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_OAUTH && resultCode == RESULT_OK) {
            description.setVisibility(View.GONE);
            container.setVisibility(View.GONE);
            progressContainer.setVisibility(View.VISIBLE);

            final String authCode = data.getStringExtra(QUERY_CODE);
            new ConnectTraktTask(this).execute(authCode);
        } else {

        }
    }


    private String getAuthorizationUrl() {
        String state = new BigInteger(130, new SecureRandom()).toString(32);
        try {
            TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey, GrieeXSettings.TraktClientSecret, GrieeXSettings.TraktCallbackUrl);

            return trakt.buildAuthorizationUrl(state);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }

        return null;
    }

    // **************
    private class CustomBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int iState = intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_NO_STATUS);
                if (iState == Constants.STATE_TRAKT_LOGIN_STATE) {
                    int result = intent.getExtras().getInt(Constants.EXTENDED_DATA_OBJECT);

                    switch (result) {
                        case TraktResult.SUCCESS:
                            description.setVisibility(View.GONE);
                            btnLogin.setVisibility(View.GONE);
                            container.setVisibility(View.VISIBLE);
                            success_message.setVisibility(View.VISIBLE);

                            progressContainer.setVisibility(View.GONE);
                            new Handler().postDelayed(TraktAuthActivity.this::finish, 3000);
                            break;
                        case TraktResult.ERROR:
                        case TraktResult.API_ERROR:
                        case TraktResult.AUTH_ERROR:
                            container.setVisibility(View.VISIBLE);
                            progressContainer.setVisibility(View.GONE);
                            btnLogin.setVisibility(View.VISIBLE);
                            error_message.setVisibility(View.VISIBLE);
                            error_message.setText(R.string.login_failed);
                            break;
                        case TraktResult.OFFLINE:
                            container.setVisibility(View.VISIBLE);
                            progressContainer.setVisibility(View.GONE);
                            btnLogin.setVisibility(View.VISIBLE);
                            error_message.setVisibility(View.VISIBLE);
                            error_message.setText(R.string.no_connection);
                            break;
                    }
                }
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }
    }
}
