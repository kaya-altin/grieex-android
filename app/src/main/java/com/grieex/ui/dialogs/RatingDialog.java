package com.grieex.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.grieex.R;
import com.grieex.helper.NLog;

public class RatingDialog extends Dialog {
	private final String TAG = RatingDialog.class.getName();

	private TextView tvRating;
	private RatingBar ratingBarPersonalRating;

	private OnCustomEventListener mListener;

	public interface OnCustomEventListener {
		void onOkClicked(String str);

		void onDialogClosed();
	}

	public void setCustomEventListener(OnCustomEventListener eventListener) {
		mListener = eventListener;
	}

	public void setRating(float rating){
		ratingBarPersonalRating.setRating(rating);
	}

	public RatingDialog(Context context) {
		super(context);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTitle(R.string.personal_rating);
		setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_rating, null));
		try {
			tvRating= findViewById(R.id.tvRating);
			ratingBarPersonalRating= findViewById(R.id.ratingBarPersonalRating);
			ratingBarPersonalRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
				public void onRatingChanged(RatingBar ratingBar, float rating,                                            boolean fromUser) {

					tvRating.setText(String.valueOf(rating));

				}
			});

			Button btnOk = findViewById(R.id.btnOk);
			btnOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						mListener.onOkClicked(String.valueOf(ratingBarPersonalRating.getRating()));
						dismiss();
					} catch (Exception e) {
						NLog.e(TAG, e);
					}
				}
			});

			Button btnCancel = findViewById(R.id.btnCancel);
			btnCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						mListener.onDialogClosed();
						dismiss();
					} catch (Exception e) {
						NLog.e(TAG, e);
					}
				}
			});
		} catch (Exception e) {
			NLog.e(TAG, e);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
