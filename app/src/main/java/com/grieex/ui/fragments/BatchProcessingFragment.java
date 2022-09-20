package com.grieex.ui.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.grieex.GrieeX;
import com.grieex.R;
import com.grieex.helper.Constants;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Prefs;
import com.grieex.model.tables.Movie;
import com.grieex.service.ServiceManager;

public class BatchProcessingFragment extends Fragment {
    private static final String TAG = BatchProcessingFragment.class.getName();
    private Context mContext;
    private TextView tvState;
    private ProgressBar progress;
    private BatchProcessingServiceBroadcastReceiver mBatchProcessingServiceBroadcastReceiver;

    public BatchProcessingFragment() {
        // Required empty public constructor
    }

    public static BatchProcessingFragment newInstance() {
        return new BatchProcessingFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (GrieeXSettings.RELEASE_MODE) {
            GrieeX.getInstance().trackScreenView(this.getClass().getName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mContext = getActivity();
            if (mBatchProcessingServiceBroadcastReceiver == null) {
                IntentFilter statusIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
                statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
                mBatchProcessingServiceBroadcastReceiver = new BatchProcessingServiceBroadcastReceiver();
                LocalBroadcastManager.getInstance(mContext).registerReceiver(mBatchProcessingServiceBroadcastReceiver, statusIntentFilter);
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_batch_processing, container, false);
        try {
            CheckBox cbMovieInfos = v.findViewById(R.id.cbMovieInfos);
            cbMovieInfos.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Prefs.with(mContext).save(Constants.BATCH_PROCESSING_MOVIE_INFOS, isChecked);
                }
            });
            CheckBox cbBackdrops = v.findViewById(R.id.cbBackdrops);
            cbBackdrops.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Prefs.with(mContext).save(Constants.BATCH_PROCESSING_BACKDROPS, isChecked);
                }
            });

            cbMovieInfos.setChecked(Prefs.with(mContext).getBoolean(Constants.BATCH_PROCESSING_MOVIE_INFOS, false));
            cbBackdrops.setChecked(Prefs.with(mContext).getBoolean(Constants.BATCH_PROCESSING_BACKDROPS, false));

            progress = v.findViewById(R.id.progress);
            progress.setMax(0);
            tvState = v.findViewById(R.id.tvState);

            final Button btnStart = v.findViewById(R.id.btnStart);
            btnStart.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!ServiceManager.IsRunningImdbBulkUpdateService(mContext)) {
                        btnStart.setText(getResources().getString(R.string.stop));
                        ServiceManager.startImdbBulkUpdateService((mContext.getApplicationContext()));
                        tvState.setText(getResources().getString(R.string.please_wait));
                    } else {
                        btnStart.setText(getResources().getString(R.string.start));
                        ServiceManager.stopImdbBulkUpdateService(mContext.getApplicationContext());
                        Prefs.with(mContext).save(Constants.BATCH_PROCESSING_MOVIE_INFOS, false);
                        Prefs.with(mContext).save(Constants.BATCH_PROCESSING_BACKDROPS, false);
                    }

                }
            });

            if (ServiceManager.IsRunningImdbBulkUpdateService(mContext)) {
                btnStart.setText(getResources().getString(R.string.stop));
                tvState.setText(getResources().getString(R.string.please_wait));
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }

        return v;
    }

    @Override
    public void onDestroy() {
        try {
            if (mBatchProcessingServiceBroadcastReceiver != null) {
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBatchProcessingServiceBroadcastReceiver);
                mBatchProcessingServiceBroadcastReceiver = null;
            }

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        super.onDestroy();
    }

    // **************
    private class BatchProcessingServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int iState = intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_NOT_COMPLETED);
                switch (iState) {
                    case Constants.STATE_BATCH_PROCESSING_STARTED:

                        break;
                    case Constants.STATE_BATCH_PROCESSING_COMPLETED:
                        tvState.setText(R.string.completed);
                        break;
                    case Constants.STATE_BATCH_PROCESSING_CANCELLED:
                        tvState.setText(R.string.not_completed);
                        break;
                    case Constants.STATE_BATCH_PROCESSING_NOT_COMPLETED:

                        break;
                    case Constants.STATE_BATCH_PROCESSING_UPDATE_MOVIE:
                        Object o = intent.getExtras().getSerializable(Constants.EXTENDED_DATA_OBJECT);
                        if (o != null && o instanceof Movie) {
                            if (progress.getMax() == 0) {
                                int pMax = intent.getExtras().getInt(Constants.EXTENDED_DATA_OBJECT2);
                                progress.setMax(pMax);
                            }

                            int p = intent.getExtras().getInt(Constants.EXTENDED_DATA_OBJECT3);
                            Movie m = (Movie) o;

                            tvState.setText(m.getOriginalName());
                            progress.setProgress(p);
                        }
                        break;
                }
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }
    }

}
