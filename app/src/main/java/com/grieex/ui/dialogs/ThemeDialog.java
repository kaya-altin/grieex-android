package com.grieex.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.grieex.R;
import com.grieex.adapter.ThemeAdapter;
import com.grieex.helper.Constants;
import com.grieex.helper.NLog;
import com.grieex.helper.ThemeUtils;
import com.grieex.model.ThemeItem;

import java.util.ArrayList;

public class ThemeDialog extends Dialog {

    private final String TAG = ThemeDialog.class.getName();
    private final Context mContext;


    private ThemeAdapter mAdapter;

    interface OnCustomEventListener {
        void onOkClicked(Constants.ContentProviders provider);

        void onDialogClosed();
    }

    public void setCustomEventListener(OnCustomEventListener eventListener) {
    }


    public ThemeDialog(Context context) {
        super(context);
        setTitle(R.string.theme);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = context;
        setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_theme, null));

        initPopup();
    }

    private void initPopup() {
        try {
            ListView list = findViewById(R.id.list);

            ArrayList<ThemeItem> data = new ArrayList<>();
            data.add(new ThemeItem(0, R.color.primary, ThemeUtils.Theme_Default, R.style.AppTheme));
            data.add(new ThemeItem(2, R.color.primary_indigo, ThemeUtils.Theme_Indigo, R.style.AppTheme2));
            data.add(new ThemeItem(3, R.color.primary_red, ThemeUtils.Theme_Red, R.style.AppTheme3));
            data.add(new ThemeItem(4, R.color.primary_brown, ThemeUtils.Theme_Brown, R.style.AppTheme4));
            data.add(new ThemeItem(5, R.color.primary_purple, ThemeUtils.Theme_Purple, R.style.AppTheme5));
            data.add(new ThemeItem(6, R.color.primary_deep_purple, ThemeUtils.Theme_DeepPurple, R.style.AppTheme6));
            data.add(new ThemeItem(7, R.color.primary_teal, ThemeUtils.Theme_Teal, R.style.AppTheme7));
            data.add(new ThemeItem(8, R.color.primary_green, ThemeUtils.Theme_Green, R.style.AppTheme8));
            data.add(new ThemeItem(9, R.color.primary_light_green, ThemeUtils.Theme_LightGreen, R.style.AppTheme9));
            data.add(new ThemeItem(10, R.color.primary_blue_grey, ThemeUtils.Theme_BlueGrey, R.style.AppTheme10));


            mAdapter = new ThemeAdapter(mContext, data);
            list.setAdapter(mAdapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ThemeItem theme = mAdapter.getItem(position);
                    ThemeUtils.changeToTheme(mContext, theme.getId());
                }
            });
            mAdapter.notifyDataSetChanged();
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
