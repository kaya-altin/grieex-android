package com.grieex.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.format.DateUtils;


import com.grieex.enums.TraktResult;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.TraktSettings;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.AccessToken;

import retrofit2.Response;

public class RefreshAccessTokenTask extends AsyncTask<String, Void, Integer> {
    private final String TAG = RefreshAccessTokenTask.class.getName();

    private final Context mContext;

    private OnCustomEventListener mListener;

    public interface OnCustomEventListener {
        void onCompleted(Integer result);
    }

    public void setCustomEventListener(OnCustomEventListener eventListener) {
        mListener = eventListener;
    }

    public RefreshAccessTokenTask(Context context) {
        mContext = context;
    }

    @Override
    protected Integer doInBackground(String... params) {
        String authCode = TraktSettings.getTraktRefreshToken(mContext);

        TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey, GrieeXSettings.TraktClientSecret, GrieeXSettings.TraktCallbackUrl).refreshToken(authCode);
        try {
            // get access token
            String accessToken = null;
            String refreshToken = null;
            long expiresIn = -1;

            Response<AccessToken> response = trakt.refreshAccessToken(refreshToken);

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


            TraktSettings.setTraktAccessToken(mContext, accessToken);
            TraktSettings.setTraktRefreshToken(mContext, refreshToken);
            TraktSettings.setTraktExpiresIn(mContext, expiresIn);
            //TraktSettings.setTraktUserName(mContext, username);
            TraktSettings.setTraktAccessTokenExpiryDate(mContext, System.currentTimeMillis() + expiresIn * DateUtils.SECOND_IN_MILLIS);

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return TraktResult.SUCCESS;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (mListener != null)
            mListener.onCompleted(result);
    }
}
