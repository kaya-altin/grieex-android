package com.grieex.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.grieex.GrieeX;
import com.grieex.R;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.update.GrieeXUpdateService;
import com.grieex.update.UpdateManager;

public class GrieeXUpdateActivity extends Activity {
	private static final String TAG = GrieeXUpdateActivity.class.getName();
	private LocalBroadcastReceiver mLocalBroadcastReceiver;

	private LinearLayout llUpdating;
	private LinearLayout llError;

    @Override
	protected void onStart() {
		super.onStart();
        if (GrieeXSettings.RELEASE_MODE) {
			GrieeX.getInstance().trackScreenView(this.getClass().getName());
        }
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grieex_update);
		try {
			llUpdating = findViewById(R.id.llUpdating);
			llError = findViewById(R.id.llError);
            TextView tvError = findViewById(R.id.tvError);

			if (mLocalBroadcastReceiver == null) {
				IntentFilter statusIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
				statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
				mLocalBroadcastReceiver = new LocalBroadcastReceiver();
				LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcastReceiver, statusIntentFilter);
			}

			if (UpdateManager.NewVersionFound(this)) {
//				if (Connectivity.isConnected(this)) {
//					llError.setVisibility(View.VISIBLE);
//					llUpdating.setVisibility(View.GONE);
//					tvError.setText(getString(R.string.grieex_updating_error2));
//					return;
//				}

				if (!UpdateManager.IsRunningUpdateManagerService(this)) {
					Intent iService = new Intent(this, GrieeXUpdateService.class);
					startService(iService);
				}
			} else {
				Intent i = new Intent(GrieeXUpdateActivity.this, MainActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				finish();
			}
		} catch (Exception e) {
			NLog.e(TAG, e);
		}
	}

	@Override
	protected void onDestroy() {
		try {
			if (mLocalBroadcastReceiver != null) {
				LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadcastReceiver);
				mLocalBroadcastReceiver = null;
			}
		} catch (Exception e) {
			NLog.e(TAG, e);
		}
		super.onDestroy();
	}

	public void btnUpdate_Click(View sender) {
		try {
			if (!Connectivity.isConnected(this)) {
				Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
				return;
			}
			llError.setVisibility(View.GONE);
			llUpdating.setVisibility(View.VISIBLE);

			Intent iService = new Intent(this, GrieeXUpdateService.class);
			startService(iService);
		} catch (Exception e) {
			NLog.e(TAG, e);
		}
	}

	private class LocalBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				int iState = intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, -1);

				switch (iState) {
					case Constants.STATE_GRIEEX_UPDATE_STARTED:

						break;
					case Constants.STATE_GRIEEX_UPDATE_COMPLETED:

						Intent i = new Intent(GrieeXUpdateActivity.this, MainActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
						finish();
						break;
					case Constants.STATE_GRIEEX_UPDATE_NOT_COMPLETED:
						llError.setVisibility(View.GONE);
						llUpdating.setVisibility(View.VISIBLE);
						break;
				}
			} catch (Exception e) {
				NLog.e(TAG, e);
			}
		}
	}

}
