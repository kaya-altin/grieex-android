package com.grieex.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.grieex.R;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;

import java.util.ArrayList;

public class AutoTextDialog extends Dialog {

	private final String TAG = AutoTextDialog.class.getName();
	private final Context mContext;
	private EditText etValue;
	private ListView lv;
	private ArrayList<String> mData;
	private ArrayAdapter mAdapter;
	private String mColumn="";

	public void setColumn(String Column){
		mColumn = Column;
	}
	public void setTitle(String Title){
	}

	private OnCustomEventListener mListener;

	public interface OnCustomEventListener {
		void onOkClicked(String str);

		void onDialogClosed();
	}

	public void setCustomEventListener(OnCustomEventListener eventListener) {
		mListener = eventListener;
	}

	@SuppressLint("InflateParams")
	public AutoTextDialog(Context context) {
		super(context);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTitle(R.string.add_to_list);
		mContext = context;
		setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_autotext, null));
		mData = new ArrayList<>();


	}

	private void initPopup() {
		try {
			DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);

			etValue = findViewById(R.id.etValue);
			lv = findViewById(R.id.lv);

			mData = dbHelper.GetOneFieldStringList("Select DISTINCT " + mColumn + " From Movies Where " + mColumn + "<>'' Order By " + mColumn + " asc");
			mAdapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_multiple_choice, mData);

			lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			lv.setAdapter(mAdapter);

			ImageButton btnNewItem = findViewById(R.id.btnNewItem);
			btnNewItem.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (TextUtils.isEmpty(etValue.getText().toString())) {
						etValue.setError(mContext.getString(R.string.can_not_be_empty));
						return;
					}

					mData.add(etValue.getText().toString());
					mAdapter.notifyDataSetChanged();
					etValue.setText("");
					etValue.setError(null);
				}
			});

			Button btnOk = findViewById(R.id.btnOk);
			btnOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					SparseBooleanArray a = lv.getCheckedItemPositions();
					if (a.size() == 0) {
						Toast.makeText(mContext, mContext.getString(R.string.alert9), Toast.LENGTH_SHORT).show();
						return;
					}

					for (int i = 0; i < a.size(); i++) {
						if (a.valueAt(i)) {
							String value = (String)mAdapter.getItem(a.keyAt(i));

							if (mListener != null)
								mListener.onOkClicked(value);
						}
					}

					dismiss();
				}
			});

			Button btnCancel = findViewById(R.id.btnCancel);
			btnCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});

//			etValue.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					InputMethodManager keyboard = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//					keyboard.showSoftInput(etValue, 0);
//				}
//			}, 200);
		} catch (Exception e) {
			NLog.e(TAG, e);
		}
	}

	public void showDialog() {
		initPopup();

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
