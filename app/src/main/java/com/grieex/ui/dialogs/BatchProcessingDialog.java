package com.grieex.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.grieex.R;
import com.grieex.helper.Constants;
import com.grieex.helper.NLog;
import com.grieex.helper.Prefs;
import com.grieex.service.ServiceManager;

public class BatchProcessingDialog extends Dialog {

	private final String TAG = BatchProcessingDialog.class.getName();
	private final Context mContext;
	private CheckBox cbMovieInfos;
    private CheckBox cbBackdrops;
    private CheckBox cbPosters;

	@SuppressLint("InflateParams")
	public BatchProcessingDialog(Context context) {
		super(context);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTitle(R.string.batch_processing);
		mContext = context;
		setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_batch_processing, null));

		initPopup();
	}

	private void initPopup() {
		try {
			cbMovieInfos = findViewById(R.id.cbMovieInfos);
			cbMovieInfos.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Prefs.with(mContext).save(Constants.BATCH_PROCESSING_MOVIE_INFOS, isChecked);
				}
			});
			cbBackdrops = findViewById(R.id.cbBackdrops);
			cbBackdrops.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Prefs.with(mContext).save(Constants.BATCH_PROCESSING_BACKDROPS, isChecked);
				}
			});
			cbPosters = findViewById(R.id.cbPosters);
			cbPosters.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Prefs.with(mContext).save(Constants.BATCH_PROCESSING_POSTERS, isChecked);
				}
			});

			cbMovieInfos.setChecked(Prefs.with(mContext).getBoolean(Constants.BATCH_PROCESSING_MOVIE_INFOS, false));
			cbBackdrops.setChecked(Prefs.with(mContext).getBoolean(Constants.BATCH_PROCESSING_BACKDROPS, false));
			cbPosters.setChecked(Prefs.with(mContext).getBoolean(Constants.BATCH_PROCESSING_POSTERS, false));

			final Button btnStart = findViewById(R.id.btnStart);
			btnStart.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!cbBackdrops.isChecked() & !cbMovieInfos.isChecked() & !cbPosters.isChecked()) {
						return;
					}

					if (!ServiceManager.IsRunningImdbBulkUpdateService(mContext)) {
						btnStart.setText(mContext.getResources().getString(R.string.stop));
						cbMovieInfos.setEnabled(false);
						cbBackdrops.setEnabled(false);
						cbPosters.setEnabled(false);
						ServiceManager.startImdbBulkUpdateService((mContext.getApplicationContext()));

						Toast.makeText(mContext, mContext.getString(R.string.batch_processing_started), Toast.LENGTH_SHORT).show();
						dismiss();
					} else {
						btnStart.setText(mContext.getResources().getString(R.string.start));
						ServiceManager.stopImdbBulkUpdateService(mContext.getApplicationContext());
						Prefs.with(mContext).save(Constants.BATCH_PROCESSING_MOVIE_INFOS, false);
						Prefs.with(mContext).save(Constants.BATCH_PROCESSING_BACKDROPS, false);
						Prefs.with(mContext).save(Constants.BATCH_PROCESSING_POSTERS, false);
					}

				}
			});

			if (ServiceManager.IsRunningImdbBulkUpdateService(mContext)) {
				btnStart.setText(mContext.getResources().getString(R.string.stop));
				cbMovieInfos.setEnabled(false);
				cbBackdrops.setEnabled(false);
				cbPosters.setEnabled(false);
			}
		} catch (Exception e) {
			NLog.e(TAG, e);
		}
	}

	public void showDialog() {
		 WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		 lp.copyFrom(getWindow().getAttributes());
		 lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		 lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

		setCancelable(true);
		setCanceledOnTouchOutside(true);
		show();
		getWindow().setAttributes(lp);
	}

}
