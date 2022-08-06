package com.grieex.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.grieex.R;
import com.grieex.helper.NLog;

public class TextDialog extends Dialog {
    private final String TAG = TextDialog.class.getName();

    private EditText etValue;
    String mTitle;

    private OnCustomEventListener mListener;

    public interface OnCustomEventListener {
        void onOkClicked(String str);

        void onDialogClosed();
    }

    public void setCustomEventListener(OnCustomEventListener eventListener) {
        mListener = eventListener;
    }

    public void setText(String name) {
        etValue.setText(name);
    }

    public void setInputType(int typeNumberVariationNormal) {
        etValue.setInputType(typeNumberVariationNormal);
    }

    public TextDialog(Context context) {
        super(context);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_text, null));
        try {
            etValue = findViewById(R.id.etValue);

            Button btnOk = findViewById(R.id.btnOk);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mListener.onOkClicked(etValue.getText().toString());
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

            etValue.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager keyboard = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(etValue, 0);
                }
            }, 200);
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
