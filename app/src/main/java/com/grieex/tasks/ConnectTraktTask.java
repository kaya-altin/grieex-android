package com.grieex.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.grieex.enums.TraktResult;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.TraktSettings;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.AccessToken;
import com.uwetrottmann.trakt5.entities.Settings;

import retrofit2.Response;


/**
 * Created by Griee on 18.03.2016.
 */
public class ConnectTraktTask extends AsyncTask<String, Void, Integer> {
    private static final String TAG = ConnectTraktTask.class.getName();

    private final Context mContext;

    public ConnectTraktTask(Context context) {
        mContext = context;
    }

    @Override
    protected Integer doInBackground(String... params) {
        String authCode = params[0];
        TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey, GrieeXSettings.TraktClientSecret, GrieeXSettings.TraktCallbackUrl);
        try {

            // get access token
            String accessToken = null;
            String refreshToken = null;
            long expiresIn = -1;


            Response<AccessToken> response = trakt.exchangeCodeForAccessToken(authCode);
            if (response.isSuccessful()) {
                accessToken = response.body().access_token;
                refreshToken = response.body().refresh_token;
                expiresIn = response.body().expires_in;
            } else {
                accessToken = null;
            }


            // did we obtain all required data?
            if (TextUtils.isEmpty(accessToken) || TextUtils.isEmpty(refreshToken) || expiresIn < 1) {
                return TraktResult.AUTH_ERROR;
            }

            // get user name
            String username = null;
            trakt.accessToken(accessToken);
            Response<Settings> settings = trakt.users().settings().execute();

            if (settings.isSuccessful()) {
                if (settings.body().user != null) {
                    username = settings.body().user.username;
                }
            } else {
                if (response.code() == 401) {
                    return TraktResult.AUTH_ERROR;
                } else {
                    return Connectivity.isConnected(mContext) ? TraktResult.API_ERROR : TraktResult.OFFLINE;
                }
            }


            // did we obtain a username?
            if (TextUtils.isEmpty(username)) {
                return TraktResult.API_ERROR;
            }

            TraktSettings.setTraktAccessToken(mContext, accessToken);
            TraktSettings.setTraktRefreshToken(mContext, refreshToken);
            TraktSettings.setTraktExpiresIn(mContext, expiresIn);
            TraktSettings.setTraktUserName(mContext, username);
            TraktSettings.setTraktAccessTokenExpiryDate(mContext, System.currentTimeMillis() + expiresIn * DateUtils.SECOND_IN_MILLIS);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return TraktResult.SUCCESS;
    }

    @Override
    protected void onPostExecute(Integer resultCode) {
        BroadcastNotifier mBroadcaster = new BroadcastNotifier(mContext);
        mBroadcaster.broadcastIntentWithObject(Constants.STATE_TRAKT_LOGIN_STATE, resultCode);
    }
}
