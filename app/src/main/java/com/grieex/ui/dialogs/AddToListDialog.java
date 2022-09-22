package com.grieex.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DatabaseUtils;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.grieex.R;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.NLog;
import com.grieex.model.tables.Movie;
import com.grieex.model.tables.Series;

import java.util.ArrayList;

public class AddToListDialog extends Dialog {

    private final String TAG = AddToListDialog.class.getName();
    private final Context mContext;
    private final ArrayList<Movie> mMovies = new ArrayList<>();
    private final ArrayList<Series> mSeries = new ArrayList<>();
    private int mListType = 1;
    private EditText etListName;
    private ListView lvList;
    private DatabaseHelper dbHelper;
    private ArrayAdapter mAdapter;
    private ArrayList<com.grieex.model.tables.Lists> mData;

    @SuppressLint("InflateParams")
    public AddToListDialog(Context context) {
        super(context);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(R.string.add_to_list);

        mContext = context;
        setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_add_to_list, null));
        mData = new ArrayList<>();
    }

    public void setListType(int ListType) {
        mListType = ListType;
    }

    public void setMovie(Movie movie) {
        mMovies.add(movie);
    }

    public void setMovies(ArrayList<Movie> movies) {
        mMovies.addAll(movies);
    }

    public void setSeries(Series series) {
        mSeries.add(series);
    }

    public void setSeries(ArrayList<Series> series) {
        mSeries.addAll(series);
    }

    private void initPopup() {
        try {
            dbHelper = DatabaseHelper.getInstance(mContext);

            etListName = findViewById(R.id.etListName);
            lvList = findViewById(R.id.lvList);
            lvList.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> adapter, final View v, final int position, long id) {
                    final com.grieex.model.tables.Lists p = (com.grieex.model.tables.Lists) mAdapter.getItem(position);
                    final CharSequence[] items = mContext.getResources().getStringArray(R.array.array1);

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setCancelable(true);
                    builder.setItems(items, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if (item == 0) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                                alertDialog.setTitle(mContext.getString(R.string.list_name));
                                // alertDialog.setMessage("Enter Password");

                                final EditText input = new EditText(mContext);
                                input.setText(p.getListName());
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                                input.setLayoutParams(lp);
                                alertDialog.setView(input);

                                alertDialog.setPositiveButton(mContext.getString(R.string.yes), new OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            if (TextUtils.isEmpty(input.getText().toString())) {
                                                input.setError(mContext.getString(R.string.can_not_be_empty));
                                                return;
                                            }

                                            DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
                                            dbHelper.ExecuteQuery("Update Lists Set ListName=" + DatabaseUtils.sqlEscapeString(input.getText().toString()) + " Where _id=" + p.getID());

                                            p.setListName(input.getText().toString());
                                            mAdapter.notifyDataSetChanged();

                                            BroadcastNotifier mBroadcaster = new BroadcastNotifier(mContext);
                                            mBroadcaster.broadcastIntentWithState(Constants.STATE_REFRESH_SLIDE_MENU);
                                            dialog.dismiss();
                                        } catch (Exception e) {
                                            NLog.e(TAG, e);
                                        }
                                    }
                                });

                                alertDialog.setNegativeButton(mContext.getString(R.string.no), new OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                alertDialog.show();
                            } else if (item == 1) {
                                try {
                                    com.grieex.model.tables.Lists o = (com.grieex.model.tables.Lists) mAdapter.getItem(position);

                                    DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
                                    dbHelper.ExecuteQuery("Delete From Lists Where _id=" + o.getID());
                                    dbHelper.ExecuteQuery("Delete From ListsMovies Where ListID=" + o.getID());
                                    mData.remove(o);
                                    mAdapter.notifyDataSetChanged();

                                    BroadcastNotifier mBroadcaster = new BroadcastNotifier(mContext);
                                    mBroadcaster.broadcastIntentWithState(Constants.STATE_REFRESH_SLIDE_MENU);
                                } catch (Exception e) {
                                    NLog.e(TAG, e);
                                }
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.setCanceledOnTouchOutside(true);
                    alert.show();

                    return false;
                }
            });

            mData = (ArrayList<com.grieex.model.tables.Lists>) dbHelper.GetCursorWithObject("Select * From Lists Where ListType=" + mListType + " Order By ListName asc", com.grieex.model.tables.Lists.class);
            mAdapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_multiple_choice, mData);

            lvList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            lvList.setAdapter(mAdapter);

            ImageButton btnNewItem = findViewById(R.id.btnNewItem);
            btnNewItem.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(etListName.getText().toString())) {
                        etListName.setError(mContext.getString(R.string.can_not_be_empty));
                        return;
                    }

                    com.grieex.model.tables.Lists list = new com.grieex.model.tables.Lists();
                    list.setListName(etListName.getText().toString());
                    list.setUpdateDate(DateUtils.DateTimeNowString());
                    list.setListType(mListType);
                    long iListID = dbHelper.addLists(list);
                    list.setID((int) iListID);

                    mData.add(list);
                    mAdapter.notifyDataSetChanged();
                    etListName.setText("");
                    etListName.setError(null);

                }
            });

            Button btnAdd = findViewById(R.id.btnAdd);
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SparseBooleanArray a = lvList.getCheckedItemPositions();
                    if (a.size() == 0) {
                        Toast.makeText(mContext, mContext.getString(R.string.alert3), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (mListType == 1) {
                        for (int i = 0; i < a.size(); i++) {
                            if (a.valueAt(i)) {
                                com.grieex.model.tables.Lists item = (com.grieex.model.tables.Lists) mAdapter.getItem(a.keyAt(i));
                                item.setUpdateDate(DateUtils.DateTimeNowString());
                                dbHelper.updateLists(item);

                                for (Movie movie : mMovies) {
                                    com.grieex.model.tables.ListsMovie listMovies = new com.grieex.model.tables.ListsMovie();
                                    listMovies.setListID(String.valueOf(item.getID()));
                                    listMovies.setMovieID(String.valueOf(movie.getID()));
                                    dbHelper.addListsMovies(listMovies);
                                }
                            }
                        }
                    } else if (mListType == 2) {
                        for (int i = 0; i < a.size(); i++) {
                            if (a.valueAt(i)) {
                                com.grieex.model.tables.Lists item = (com.grieex.model.tables.Lists) mAdapter.getItem(a.keyAt(i));
                                item.setUpdateDate(DateUtils.DateTimeNowString());
                                dbHelper.updateLists(item);

                                for (Series series : mSeries) {
                                    com.grieex.model.tables.ListsSeries listSeries = new com.grieex.model.tables.ListsSeries();
                                    listSeries.setListID(String.valueOf(item.getID()));
                                    listSeries.setSeriesID(String.valueOf(series.getID()));
                                    dbHelper.addListsSeries(listSeries);
                                }
                            }
                        }
                    }

                    Toast.makeText(mContext, mContext.getString(R.string.added_to_the_list), Toast.LENGTH_SHORT).show();

                    BroadcastNotifier mBroadcaster = new BroadcastNotifier(mContext);
                    mBroadcaster.broadcastIntentWithState(Constants.STATE_REFRESH_SLIDE_MENU);

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
