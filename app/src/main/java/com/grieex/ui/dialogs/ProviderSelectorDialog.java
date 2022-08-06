package com.grieex.ui.dialogs;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.grieex.R;
import com.grieex.helper.Constants;
import com.grieex.helper.NLog;

class ProviderSelectorDialog extends Dialog {

	private final String TAG = ProviderSelectorDialog.class.getName();


	private OnCustomEventListener mListener;

	interface OnCustomEventListener {
		void onOkClicked(Constants.ContentProviders provider);

		void onDialogClosed();
	}

	public void setCustomEventListener(OnCustomEventListener eventListener) {
		mListener = eventListener;
	}


	private ProviderSelectorDialog(Context context) {
		super(context);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_provider_selector, null));

		initPopup();
	}

	private void initPopup() {
		try {

			ImageButton ibImdb = findViewById(R.id.ibImdb);
            ibImdb.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					if (mListener != null) {
                        mListener.onOkClicked(Constants.ContentProviders.Imdb);
					}
					dismiss();
				}
			});

            ImageButton ibTmdb = findViewById(R.id.ibTmdb);
            ibTmdb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mListener != null) {
                        mListener.onOkClicked(Constants.ContentProviders.TMDb);
                    }
                    dismiss();
                }
            });
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
