package com.grieex.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.grieex.R;
import com.grieex.helper.NLog;

public class CustomProgressDialog extends Dialog {
	private final String TAG = CustomProgressDialog.class.getName();

	private TextView tvProgressText;

	private String mText;
	private boolean mCancelable = false;
	private boolean mAutoClose = false;

	public void setText(String text) {
		mText = text;
	}

	public void setCancelable(boolean Cancelable) {
		mCancelable = Cancelable;
	}

	public void setAutoClose(boolean AutoClose) {
		mAutoClose = AutoClose;
	}

	@SuppressLint("InflateParams")
	public CustomProgressDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_custom_progress, null));
		try {
			ProgressBar progressBar = findViewById(R.id.progressBar);
			tvProgressText = findViewById(R.id.tvProgressText);

		} catch (Exception e) {
			NLog.e(TAG, e);
		}
	}

	private void startProgress() {
		try {
			if (!TextUtils.isEmpty(mText)) {
				tvProgressText.setText(mText);
				tvProgressText.setVisibility(View.VISIBLE);
			} else
				tvProgressText.setVisibility(View.GONE);

			if (mAutoClose) {
				Handler h = new Handler();
				Runnable r = this::dismiss;
				h.postDelayed(r, 1000);
			}
		} catch (Exception e) {
			NLog.e(TAG, e);
		}
	}

	public void showDialog() {
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		setCancelable(mCancelable);
		setCanceledOnTouchOutside(mCancelable);
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		show();
		getWindow().setAttributes(lp);

		startProgress();
	}

}
