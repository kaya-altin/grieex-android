package com.grieex.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.grieex.R;
import com.grieex.helper.NLog;

public class TraktCommentDialog extends Dialog {
	private final String TAG = TraktCommentDialog.class.getName();
	private final Context mContext;

	private EditText etValue;
	private CheckBox chkSpoiler;
	private TextView tvWordCount;

	private OnCustomEventListener mListener;

	private final int MAX_WORDS = 200;

	public interface OnCustomEventListener {
		void onOkClicked(String comment, boolean isSpoiler);

		void onDialogClosed();
	}

	public void setCustomEventListener(OnCustomEventListener eventListener) {
		mListener = eventListener;
	}


	public TraktCommentDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		mContext = context;
		setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_trakt_comment, null));
		try {
			etValue = findViewById(R.id.etValue);
			etValue.addTextChangedListener(new TextWatcher() {
				public void afterTextChanged(Editable s) {
					// Nothing
				}

				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// Nothing
				}

				public void onTextChanged(CharSequence s, int start, int before, int count) {
					int wordsLength = countWords(s.toString());// words.length;
					// count == 0 means a new word is going to start
					if (count == 0 && wordsLength >= MAX_WORDS) {
						setCharLimit(etValue, etValue.getText().length());
					} else {
						removeFilter(etValue);
					}
					tvWordCount.setText(String.valueOf(wordsLength) + "/" + MAX_WORDS);
				}
			});

			chkSpoiler = findViewById(R.id.chkSpoiler);
			tvWordCount= findViewById(R.id.tvWordCount);

			Button btnOk = findViewById(R.id.btnOk);
			btnOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						String[] words = etValue.getText().toString().split(" ");

						if (etValue.getText().toString().isEmpty() || words.length < 5) {
							etValue.setError(mContext.getString(R.string.comment_min_alert));
							return;
						}

						mListener.onOkClicked(etValue.getText().toString(), chkSpoiler.isChecked());
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

	private int countWords(String s) {
		String trim = s.trim();
		if (trim.isEmpty())
			return 0;
		return trim.split("\\s+").length; // separate string around spaces
	}

	private InputFilter filter;

	private void setCharLimit(EditText et, int max) {
		filter = new InputFilter.LengthFilter(max);
		et.setFilters(new InputFilter[]{filter});
	}

	private void removeFilter(EditText et) {
		if (filter != null) {
			et.setFilters(new InputFilter[0]);
			filter = null;
		}
	}

}
