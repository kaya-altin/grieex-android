package com.grieex.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.grieex.R;
import com.grieex.helper.NLog;

public class FilterDialog extends Dialog {

    private final String TAG = FilterDialog.class.getName();
    private final Context mContext;

    private String mFilter = "";
    private String mOrder = "";
    private Spinner spinnerSeen;
    private Spinner spinnerGenre;
    private Spinner spinner_ranking_archives_number;
    private Spinner spinner_ranking_insert_date;
    private Spinner spinner_ranking_update_date;
    private Spinner spinner_ranking_imdb_rating;
    private Spinner spinner_ranking_tmdb_rating;

    private OnCustomEventListener mListener;

    public interface OnCustomEventListener {
        void onOkClicked(String Filter, String Order);

        void onClearClicked();

        void onDialogClosed();
    }

    public void setCustomEventListener(OnCustomEventListener eventListener) {
        mListener = eventListener;
    }

    public void setFilter(String filter, String order) {
        if (!TextUtils.isEmpty(filter)) {
            if (filter.contains("Seen=1")) {
                spinnerSeen.setSelection(1);
            } else if (filter.contains("Seen=0")) {
                spinnerSeen.setSelection(2);
            }

            CharSequence[] genres = mContext.getResources().getStringArray(R.array.genres);
            for (int i = 0; i < genres.length; i++) {
                String genre = genres[i].toString();
                if (filter.contains(genre)) {
                    spinnerGenre.setSelection(i);
                    break;
                }
            }

        }
        if (!TextUtils.isEmpty(order)) {
            if (order.contains("CAST(ArchivesNumber AS INTEGER) ASC")) {
                spinner_ranking_archives_number.setSelection(1);
            } else if (order.contains("CAST(ArchivesNumber AS INTEGER) DESC")) {
                spinner_ranking_archives_number.setSelection(2);
            }

            if (order.contains("InsertDate ASC")) {
                spinner_ranking_insert_date.setSelection(1);
            } else if (order.contains("InsertDate DESC")) {
                spinner_ranking_insert_date.setSelection(2);
            }

            if (order.contains("UpdateDate ASC")) {
                spinner_ranking_update_date.setSelection(1);
            } else if (order.contains("UpdateDate DESC")) {
                spinner_ranking_update_date.setSelection(2);
            }

            if (order.contains("ImdbUserRating ASC")) {
                spinner_ranking_imdb_rating.setSelection(1);
            } else if (order.contains("ImdbUserRating DESC")) {
                spinner_ranking_imdb_rating.setSelection(2);
            }

            if (order.contains("TmdbUserRating ASC")) {
                spinner_ranking_tmdb_rating.setSelection(1);
            } else if (order.contains("TmdbUserRating DESC")) {
                spinner_ranking_tmdb_rating.setSelection(2);
            }
        }


    }

    public FilterDialog(Context context) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setTitle(R.string.filter);
        mContext = context;
        setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_filter, null));

        initPopup();
    }

    private void initPopup() {
        try {
            spinnerSeen = findViewById(R.id.spinnerSeen);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.array2, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSeen.setAdapter(adapter);
            spinnerSeen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView selectedText = (TextView) parent.getChildAt(0);
                    if (position > 0 && selectedText != null) {
                        selectedText.setTextColor(Color.RED);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinnerGenre = findViewById(R.id.spinnerGenre);
            ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(mContext, R.array.genres, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerGenre.setAdapter(adapter2);
            spinnerGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView selectedText = (TextView) parent.getChildAt(0);
                    if (position > 0 && selectedText != null) {
                        selectedText.setTextColor(Color.RED);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinner_ranking_archives_number = findViewById(R.id.spinner_ranking_archives_number);
            ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(mContext, R.array.ranking_values, android.R.layout.simple_spinner_item);
            adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_ranking_archives_number.setAdapter(adapter3);
            spinner_ranking_archives_number.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView selectedText = (TextView) parent.getChildAt(0);
                    if (position > 0 && selectedText != null) {
                        selectedText.setTextColor(Color.RED);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinner_ranking_insert_date = findViewById(R.id.spinner_ranking_insert_date);
            ArrayAdapter<CharSequence> adapter5 = ArrayAdapter.createFromResource(mContext, R.array.ranking_values, android.R.layout.simple_spinner_item);
            adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_ranking_insert_date.setAdapter(adapter5);
            spinner_ranking_insert_date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView selectedText = (TextView) parent.getChildAt(0);
                    if (position > 0 && selectedText != null) {
                        selectedText.setTextColor(Color.RED);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinner_ranking_update_date = findViewById(R.id.spinner_ranking_update_date);
            ArrayAdapter<CharSequence> adapter8 = ArrayAdapter.createFromResource(mContext, R.array.ranking_values, android.R.layout.simple_spinner_item);
            adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_ranking_update_date.setAdapter(adapter5);
            spinner_ranking_update_date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView selectedText = (TextView) parent.getChildAt(0);
                    if (position > 0 && selectedText != null) {
                        selectedText.setTextColor(Color.RED);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            spinner_ranking_imdb_rating = findViewById(R.id.spinner_ranking_imdb_rating);
            ArrayAdapter<CharSequence> adapter6 = ArrayAdapter.createFromResource(mContext, R.array.ranking_values, android.R.layout.simple_spinner_item);
            adapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_ranking_imdb_rating.setAdapter(adapter6);
            spinner_ranking_imdb_rating.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView selectedText = (TextView) parent.getChildAt(0);
                    if (position > 0 && selectedText != null) {
                        selectedText.setTextColor(Color.RED);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinner_ranking_tmdb_rating = findViewById(R.id.spinner_ranking_tmdb_rating);
            ArrayAdapter<CharSequence> adapter7 = ArrayAdapter.createFromResource(mContext, R.array.ranking_values, android.R.layout.simple_spinner_item);
            adapter7.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_ranking_tmdb_rating.setAdapter(adapter7);
            spinner_ranking_tmdb_rating.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView selectedText = (TextView) parent.getChildAt(0);
                    if (position > 0 && selectedText != null) {
                        selectedText.setTextColor(Color.RED);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            Button btnOk = findViewById(R.id.btnOk);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFilter = "";
                    mOrder = "";

                    int iSeen = spinnerSeen.getSelectedItemPosition();
                    if (iSeen == 1) {
                        mFilter = " Seen=1 ";
                    } else if (iSeen == 2) {
                        mFilter = " Seen=0 ";
                    }
                    if (spinnerGenre.getSelectedItemPosition() > 0) {
                        if (!TextUtils.isEmpty(mFilter)) {
                            mFilter += " and ";
                        }

                        String strGenre = spinnerGenre.getSelectedItem().toString();
                        mFilter += " Genre Like '%" + strGenre + "%' ";

                    }

                    if (spinner_ranking_archives_number.getSelectedItemPosition() > 0) {
                        if (!TextUtils.isEmpty(mOrder)) {
                            mOrder += ",";
                        }

                        String strValue = "ASC";
                        if (spinner_ranking_archives_number.getSelectedItemPosition()==2)
                            strValue = "DESC";
                        mOrder += " CAST(ArchivesNumber AS INTEGER) " + strValue + " ";
                    }

                    if (spinner_ranking_insert_date.getSelectedItemPosition() > 0) {
                        if (!TextUtils.isEmpty(mOrder)) {
                            mOrder += ",";
                        }

                        String strValue = "ASC";
                        if (spinner_ranking_insert_date.getSelectedItemPosition()==2)
                            strValue = "DESC";
                        mOrder += " InsertDate " + strValue + " ";
                    }

                    if (spinner_ranking_update_date.getSelectedItemPosition() > 0) {
                        if (!TextUtils.isEmpty(mOrder)) {
                            mOrder += ",";
                        }

                        String strValue = "ASC";
                        if (spinner_ranking_update_date.getSelectedItemPosition()==2)
                            strValue = "DESC";
                        mOrder += " UpdateDate " + strValue + " ";
                    }

                    if (spinner_ranking_imdb_rating.getSelectedItemPosition() > 0) {
                        if (!TextUtils.isEmpty(mOrder)) {
                            mOrder += ",";
                        }

                        String strValue = "ASC";
                        if (spinner_ranking_imdb_rating.getSelectedItemPosition()==2)
                            strValue = "DESC";
                        mOrder += " ImdbUserRating " + strValue + " ";
                    }

                    if (spinner_ranking_tmdb_rating.getSelectedItemPosition() > 0) {
                        if (!TextUtils.isEmpty(mOrder)) {
                            mOrder += ",";
                        }

                        String strValue = "ASC";
                        if (spinner_ranking_tmdb_rating.getSelectedItemPosition()==2)
                            strValue = "DESC";
                        mOrder += " TmdbUserRating " + strValue + " ";
                    }

                    if (mListener != null) {
                        mListener.onOkClicked(mFilter,mOrder);
                    }
                    dismiss();
                }
            });

            Button btnClear = findViewById(R.id.btnClear);
            btnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClearClicked();
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
