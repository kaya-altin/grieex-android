package com.grieex.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.adapter.SearchSeriesAdapter;
import com.grieex.core.ImportQueues;
import com.grieex.core.TraktTv;
import com.grieex.core.listener.OnTraktTvEventListener;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.DbUtils;
import com.grieex.helper.EndlessRecyclerOnScrollListener;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.SampleRecycler;
import com.grieex.helper.Utils;
import com.grieex.model.tables.Movie;
import com.grieex.model.tables.Series;
import com.grieex.service.ServiceManager;

import java.util.ArrayList;

public class SearchSeriesActivity extends BaseActivity {
    private static final String TAG = SearchSeriesActivity.class.getName();
    private boolean isProInstalled = false;
    private ProgressBar progressBar;
    private EditText etSearch;

    private RecyclerView mRecyclerView;
    private SearchSeriesAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TraktTv tt;
    private BroadcastNotifier mBroadcaster;
    private DatabaseHelper dbHelper;

    private int iPageID = -1;

    private boolean AddedMessageDisplayed = false;
    private ImportServiceBroadcastReceiver mImportServiceBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_series);
        try {
            Toolbar toolbar = findViewById(R.id.myToolbar);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.setDisplayHomeAsUpEnabled(true);

            isProInstalled = Utils.isProInstalled(this);

            dbHelper = DatabaseHelper.getInstance(this);
            mBroadcaster = new BroadcastNotifier(this);

            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                iPageID = bundle.getInt(Constants.EXTENDED_DATA_OBJECT3, -1);

            }

            tt = new TraktTv();
            tt.setTraktEventListener(new OnTraktTvEventListener() {
                @Override
                public void onCompleted(Object m) {
                    if (m != null) {
                        final ArrayList<Series> myDataset = (ArrayList<Series>) m;
//                        for (Series s : myDataset) {
//                            s.setIsExisting(dbHelper.getSeriesExistFromtTraktTvId(s.getTraktId()));
//                        }

                        setAdaper(myDataset);
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });

            progressBar = findViewById(R.id.progressBar);

            mRecyclerView = findViewById(R.id.list);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.recylerviewColumnCountSearch));
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(new SampleRecycler());

            mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener((GridLayoutManager) mLayoutManager) {
                @Override
                public void onLoadMore(int page) {
                    if (!Connectivity.isConnected(SearchSeriesActivity.this)) {
                        Toast.makeText(SearchSeriesActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (mAdapter != null && mAdapter.getItemCount() == 0)
                        return;

                    getTrending(page);
                }
            });


            etSearch = findViewById(R.id.etSearch);
            etSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        Search();
                        return true;
                    }
                    return false;
                }
            });

            ImageButton btnSearch = findViewById(R.id.btnSearch);
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Search();
                }
            });


            getTrending(0);

            if (mImportServiceBroadcastReceiver == null) {
                IntentFilter statusIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
                statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
                mImportServiceBroadcastReceiver = new ImportServiceBroadcastReceiver();
                LocalBroadcastManager.getInstance(this).registerReceiver(mImportServiceBroadcastReceiver, statusIntentFilter);
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (mImportServiceBroadcastReceiver != null) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mImportServiceBroadcastReceiver);
                mImportServiceBroadcastReceiver = null;
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        super.onDestroy();
    }

    private void setAdaper(ArrayList<Series> myDataset) {
        for (Series s : myDataset) {
            s.setIsExisting(dbHelper.getSeriesExistFromtTraktTvId(s.getTraktId()));
        }

        if (mAdapter == null) {
            mAdapter = new SearchSeriesAdapter(myDataset);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(new SearchSeriesAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View itemView, int position) {
                    Series series = mAdapter.getItem(position);

                    if (!series.getIsExisting() && !Connectivity.isConnected(SearchSeriesActivity.this)) {
                        Toast.makeText(SearchSeriesActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                        return;
                    }


                    Intent it = new Intent(SearchSeriesActivity.this, SeriesDetailActivity.class);
                    it.putExtra(Constants.ImdbOrTraktID, Utils.parseString(series.getTraktId()));
                    it.putExtra(Constants.IsExistDatabase, series.getIsExisting());
                    startActivity(it);
                }
            });
            mAdapter.setOnAddClickListener(new SearchSeriesAdapter.OnAddClickListener() {
                @Override
                public void onAddClick(final View itemView, final int position) {
                    if (!Connectivity.isConnected(SearchSeriesActivity.this)) {
                        Toast.makeText(SearchSeriesActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!isProInstalled) {
                        if (DbUtils.getSeriesCount(SearchSeriesActivity.this) >= GrieeXSettings.FreeRecordLimitSeries) {
                            Toast.makeText(SearchSeriesActivity.this, getResources().getString(R.string.alert7), Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    itemView.setVisibility(View.GONE);

                    if (!AddedMessageDisplayed) {
                        AddedMessageDisplayed = false;
                        Toast.makeText(SearchSeriesActivity.this, getString(R.string.series_added), Toast.LENGTH_SHORT).show();
                    }

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }

                        @Override
                        protected Void doInBackground(Void... params) {
                            Series s = mAdapter.getItem(position);
                            s.setIsExisting(true);
                            s.setContentProvider(Constants.ContentProviders.TraktTv.value);
                            s.setInsertDate(DateUtils.DateTimeNowString());
                            s.setUpdateDate(DateUtils.DateTimeNowString());

                            long _id = dbHelper.addSeries(s);
                            s.setID((int) _id);

                            if (iPageID != -1) {
                                com.grieex.model.tables.ListsSeries listSeries = new com.grieex.model.tables.ListsSeries();
                                listSeries.setListID(String.valueOf(iPageID));
                                listSeries.setSeriesID(String.valueOf(_id));
                                dbHelper.addListsSeries(listSeries);
                            }

                            ImportQueues.AddQueue(SearchSeriesActivity.this, _id, String.valueOf(s.getTraktId()), Constants.ContentProviders.TraktTv);

                            mBroadcaster.broadcastIntentWithObject(Constants.STATE_INSERT_SERIES, s);

                            return null;
                        }

                        protected void onPostExecute(Void result) {
                            ServiceManager.startImportDataService(getApplicationContext());
                        }
                    }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                }
            });
        } else {
            mAdapter.addAllEnd(myDataset);
        }
    }

    private void getTrending(int page) {
        try {
            progressBar.setVisibility(View.VISIBLE);
            tt.getTrending(page);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void showMovie(Movie m) {
        try {
            Intent it = new Intent(this, MovieDetailActivity.class);
            it.putExtra(Constants.Movie, m);
            startActivity(it);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void Search() {
        try {
            if (!Connectivity.isConnected(getApplicationContext())) {
                Toast.makeText(SearchSeriesActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(etSearch.getText()))
                return;

            Utils.hideKeyboard(this);

            progressBar.setVisibility(View.VISIBLE);
            if (mAdapter != null) {
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
            }
            mRecyclerView.clearOnScrollListeners();

            tt.setTraktEventListener(new OnTraktTvEventListener() {
                @Override
                public void onCompleted(Object m) {
                    if (m != null) {
                        ArrayList<Series> myDataset = (ArrayList<Series>) m;
                        setAdaper(myDataset);
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
            tt.Search(etSearch.getText().toString());
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            NLog.e(TAG, e);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // **************
    private class ImportServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int iState = intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_NOT_COMPLETED);
                if (iState == Constants.STATE_INSERT_SERIES) {
                    Object o = intent.getExtras().getSerializable(Constants.EXTENDED_DATA_OBJECT);
                    if (o != null && o instanceof Series) {
                        Series s = (Series) o;

                        int position = mAdapter.getPositionFromTraktId(s.getTraktId());
                        if (position > -1) {
                            Series item = mAdapter.getItem(position);
                            item.setIsExisting(true);
                            mAdapter.notifyItemChanged(position);
                        }

                    }
                } else if (iState == Constants.STATE_DELETE_SERIES) {
                    Object o = intent.getExtras().getSerializable(Constants.EXTENDED_DATA_OBJECT);
                    if (o != null && o instanceof Series) {
                        Series s = (Series) o;

                        int position = mAdapter.getPositionFromTraktId(s.getTraktId());
                        if (position > -1) {
                            Series item = mAdapter.getItem(position);
                            item.setIsExisting(false);
                            mAdapter.notifyItemChanged(position);
                        }

                    }
                }
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }
    }

}
