package com.grieex.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.grieex.R;
import com.grieex.adapter.CustomMenuAdapter;
import com.grieex.adapter.SeriesListAdapter;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.DbUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Prefs;
import com.grieex.helper.SampleRecycler;
import com.grieex.helper.Utils;
import com.grieex.model.CustomMenuItem;
import com.grieex.model.Page;
import com.grieex.model.tables.Series;
import com.grieex.ui.FullScreenImageActivity;
import com.grieex.ui.MainActivity;
import com.grieex.ui.SearchSeriesActivity;
import com.grieex.ui.SeriesDetailActivity;
import com.grieex.ui.dialogs.AddToListDialog;
import com.grieex.widget.SimpleDividerItemDecoration;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class SeriesListFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static final String TAG = SeriesListFragment.class.getName();
    private static final String ARG_Filter = "ARG_Filter";
    private static final int iRecordShow = 100;
    private static final int iFirstLoadRowCount = 20;
    private Activity activity;
    private boolean isProInstalled = false;
    private SeriesListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar progress;
    private int iRecordCount = 0;
    private int iRecordPage = 0;
    private boolean bIsFirstLoaded = true;
    private String mFilter = "";
    private String mFilterStatic = "";
    private boolean IsSearchViewVisible = false;
    private String mOrder = "";
    private TextView tvNoRecord;
    private volatile String RunKey;
    private DatabaseHelper dbHelper;
    private ImportServiceBroadcastReceiver mImportServiceBroadcastReceiver;
    private CallbackManager callbackManager;
    private int iListViewType = 0;
    private MenuItem menuSearchItem;
    private MenuItem menuOkItem;
    private MenuItem menuCancelItem;

    public static SeriesListFragment newInstance() {
        return new SeriesListFragment();
    }

    public static SeriesListFragment newInstance(String filter) {
        SeriesListFragment fragment = new SeriesListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_Filter, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        try {
            activity = getActivity();
            dbHelper = DatabaseHelper.getInstance(activity);
            isProInstalled = Utils.isProInstalled(activity);
            iListViewType = Prefs.with(activity).getInt(Constants.ListViewType, 0);

            callbackManager = CallbackManager.Factory.create();

            if (getArguments() != null) {
                mFilterStatic = getArguments().getString(ARG_Filter);
            }

            if (mImportServiceBroadcastReceiver == null) {
                IntentFilter statusIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
                statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
                mImportServiceBroadcastReceiver = new ImportServiceBroadcastReceiver();
                LocalBroadcastManager.getInstance(activity).registerReceiver(mImportServiceBroadcastReceiver, statusIntentFilter);
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (mImportServiceBroadcastReceiver != null) {
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(mImportServiceBroadcastReceiver);
                mImportServiceBroadcastReceiver = null;
            }

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.serieslist, container, false);
        try {
            mRecyclerView = v.findViewById(R.id.list);
            mRecyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager mLayoutManager;
            if (iListViewType == 0) {
                mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(activity, R.drawable.divider2));
                mLayoutManager = new LinearLayoutManager(activity);
            } else {
                mLayoutManager = new GridLayoutManager(activity, getResources().getInteger(R.integer.recylerviewColumnCount));
            }
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(new SampleRecycler());

            FloatingActionButton fab = v.findViewById(R.id.fab);
            fab.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!isProInstalled) {
                        if (mAdapter != null && mAdapter.getItemCount() >= GrieeXSettings.FreeRecordLimitSeries) {
                            Toast.makeText(activity, activity.getResources().getString(R.string.alert7), Toast.LENGTH_LONG).show();
                            return;
                        }
                    }


                    Intent i = new Intent(getActivity(), SearchSeriesActivity.class);
                    i.putExtra(Constants.EXTENDED_DATA_OBJECT, Constants.ContentProviders.TMDbTv);
                    if (mFilterStatic.contains("ListsSeries.ListID")) {
                        int iPageID = Utils.parseInt(((MainActivity) activity).getCurrentPage().getPageID());
                        i.putExtra(Constants.EXTENDED_DATA_OBJECT3, iPageID);
                    }
                    startActivity(i);
                }
            });

            progress = v.findViewById(R.id.progress);
            tvNoRecord = v.findViewById(R.id.tvNoRecord);

            RunKey = Utils.getRandomString();
            new DataLoader().execute(RunKey);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        try {
            inflater.inflate(R.menu.serieslist_actionbar_menu, menu);

            // final MenuItem menuAddItem = (MenuItem)
            // menu.findItem(R.id.action_add);
            // menuAddItem.setIntent(new Intent(getActivity(),
            // SearchMovieActivity.class));

            menuSearchItem = menu.findItem(R.id.action_search);

            final SearchView mSearchView = (SearchView) menuSearchItem.getActionView();
            menuSearchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    IsSearchViewVisible = false;
                    mSearchView.setQuery("", false);
                    return true;
                }

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    IsSearchViewVisible = true;
                    return true;
                }
            });
            mSearchView.setQueryHint(getString(R.string.search));
            mSearchView.setOnQueryTextListener(this);

            menuOkItem = menu.findItem(R.id.action_ok);
            menuOkItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    try {
                        if (mAdapter.getSelecteds().size() == 0) {
                            mAdapter.setShowCheckBox(false);

                            menuOkItem.setVisible(false);
                            menuCancelItem.setVisible(false);
                            menuSearchItem.setVisible(true);
                        } else {
                            Page page = ((MainActivity) activity).getCurrentPage();
                            if (page.getPageType() == Page.PageTypes.Series) {
                                AddToListDialog d = new AddToListDialog(activity);
                                d.setListType(2);
                                d.setSeries(mAdapter.getSelecteds());
                                d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        mAdapter.setShowCheckBox(false);

                                        menuOkItem.setVisible(false);
                                        menuCancelItem.setVisible(false);
                                        menuSearchItem.setVisible(true);
                                    }
                                });
                                d.showDialog();
                            } else if (page.getPageType() == Page.PageTypes.CustomListSeries) {
                                if (mAdapter.getSelecteds().size() == 0) {
                                    menuSearchItem.setVisible(true);
                                    menuOkItem.setVisible(false);
                                    menuCancelItem.setVisible(false);

                                    mAdapter.setShowCheckBox(false);
                                } else {
                                    int iPageID = Utils.parseInt(((MainActivity) activity).getCurrentPage().getPageID());
                                    for (Series series : mAdapter.getSelecteds()) {
                                        dbHelper.ExecuteQuery("Delete From ListsSeries Where ListID=" + iPageID + " and SeriesID=" + String.valueOf(series.getID()));
                                        mAdapter.remove(series);
                                    }

                                    BroadcastNotifier mBroadcaster = new BroadcastNotifier(activity);
                                    mBroadcaster.broadcastIntentWithState(Constants.STATE_REFRESH_SLIDE_MENU_COUNT);

                                    menuSearchItem.setVisible(true);
                                    menuOkItem.setVisible(false);
                                    menuCancelItem.setVisible(false);

                                    mAdapter.setShowCheckBox(false);

                                    if (mAdapter.getItemCount() == 0) {
                                        tvNoRecord.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        NLog.e(TAG, e);
                    }
                    return false;
                }
            });

            menuCancelItem = menu.findItem(R.id.action_cancel);
            menuCancelItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    mAdapter.setShowCheckBox(false);

                    menuOkItem.setVisible(false);
                    menuCancelItem.setVisible(false);
                    menuSearchItem.setVisible(true);
                    return false;
                }
            });
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Toast.makeText(this, "You searched for: " + query,
        // Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (IsSearchViewVisible) {
            String stFilter = "";
            stFilter += "SeriesName Like '%" + newText + "%' ";
            setFilter(stFilter);
        }
        return true;
    }

    public void RefreshList() {
        ResetData();
        RunKey = Utils.getRandomString();
        new DataLoader().execute(RunKey);
    }

    private void ResetData() {
        try {
            if (mAdapter != null)
                mAdapter = null;

            iRecordCount = 0;
            iRecordPage = 0;
            bIsFirstLoaded = true;
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void setFilter(String str) {
        boolean IsFiltered = !mFilter.isEmpty();

        if (IsFiltered | !str.isEmpty()) {
            ResetData();
            mFilter = str;
            RunKey = Utils.getRandomString();
            new DataLoader().execute(RunKey);
        }
    }

    public void setOrder(String str) {
        boolean IsOrdered = !mOrder.isEmpty();

        if (IsOrdered | !str.isEmpty()) {
            ResetData();
            mOrder = str;
            RunKey = Utils.getRandomString();
            new DataLoader().execute(RunKey);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    private void showSeries(long SeriesID, View view) {
        try {
            Intent it = new Intent(getActivity(), SeriesDetailActivity.class);
            it.putExtra(Constants.SeriesID, String.valueOf(SeriesID));
            it.putExtra(Constants.IsExistDatabase, true);
            startActivity(it);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void updateSeries(Series m) {
        try {
            if (mAdapter == null)
                return;

            for (int position = 0; position < mAdapter.getItemCount(); position++) {
                Series series = mAdapter.getItem(position);
                if (series.getID() == m.getID()) {
                    series.LoadWithWhereColumn(activity, Series.COLUMNS._ID, String.valueOf(m.getID()));
                    long nextAirDateMillis = DbUtils.getLastEpisodeMs(activity, m.getID());
                    if (nextAirDateMillis != -1) {
                        series.setDateInfo(String.format("%s | %s | %s", DateUtils.getDateFormat(nextAirDateMillis, Constants.DATE_FORMAT11), DateUtils.getDateFormat(nextAirDateMillis, Constants.DATE_FORMAT10), series.getNetwork()));
                    } else {
                        series.setDateInfo(String.format("%s | %s | %s", series.getAirDay(), series.getAirTime(), series.getNetwork()));
                    }
                    series.setEpisodeCount(DbUtils.getEpisodeCount(activity, m.getID()));
                    series.setWatchedCount(DbUtils.getEpisodeWatchedCount(activity, m.getID()));
                    series.setCollectedCount(DbUtils.getCollectedCount(activity, m.getID()));
                    mAdapter.notifyItemChanged(position);
                    break;
                }
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void deleteSeries(Series s) {
        try {
            if (mAdapter == null)
                return;

            for (int position = 0; position < mAdapter.getItemCount(); position++) {
                Series series = mAdapter.getItem(position);
                if (series.getID() == s.getID()) {
                    dbHelper.deleteSeries(series);
                    mAdapter.remove(series);

                    if (mAdapter.getItemCount() == 0) {
                        tvNoRecord.setVisibility(View.VISIBLE);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void share(final Series m) {
        final ArrayList<CustomMenuItem> items = new ArrayList<>();
        items.add(new CustomMenuItem(1, R.drawable.facebook_icon, getString(R.string.via_imdb)));
        items.add(new CustomMenuItem(2, R.drawable.facebook_icon, getString(R.string.via_tmdb)));
        items.add(new CustomMenuItem(3, R.drawable.twitter_icon, getString(R.string.via_imdb)));
        items.add(new CustomMenuItem(4, R.drawable.twitter_icon, getString(R.string.via_tmdb)));

        CustomMenuAdapter a = new CustomMenuAdapter(activity, items);
        a.setShowIcon(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.share);
        builder.setCancelable(true);
        builder.setAdapter(a, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                try {
                    Series series = dbHelper.getSeries(m.getID());

                    CustomMenuItem menuItem = items.get(item);

                    switch (menuItem.getId()) {
                        case 1:
                            if (ShareDialog.canShow(ShareLinkContent.class)) {
                                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                        .setQuote(m.getSeriesName() + " / " + m.getOverview())
                                        .setContentUrl(Uri.parse("https://www.imdb.com/title/" + series.getImdbId()))
                                        .build();

                                ShareDialog shareDialog = new ShareDialog(activity);
                                shareDialog.show(linkContent);
                            }
                            break;
                        case 2:
                            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                    .setQuote(m.getSeriesName() + " / " + m.getOverview())
                                    .setContentUrl(Uri.parse("https://www.themoviedb.org/movie/" + series.getTmdbId()))
                                    .build();

                            ShareDialog shareDialog = new ShareDialog(activity);
                            shareDialog.show(linkContent);
                            break;
                        case 3: {
                            TweetComposer.Builder builder = new TweetComposer.Builder(activity);
                            builder.text("http://www.imdb.com/title/" + series.getTmdbId());

                            if (!TextUtils.isEmpty(m.getPoster())) {
                                Glide.with(SeriesListFragment.this)
                                        .asFile()
                                        .load(m.getPoster())
                                        .into(new CustomTarget<File>() {
                                            @Override
                                            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                                Uri myImageUri = Uri.fromFile(resource);
                                                builder.image(myImageUri);
                                                builder.show();
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {

                                            }
                                        });

                            } else {
                                builder.show();
                            }
                            break;
                        }
                        case 4: {
                            TweetComposer.Builder builder = new TweetComposer.Builder(activity);
                            builder.text("https://www.themoviedb.org/show/" + series.getImdbId());

                            if (!TextUtils.isEmpty(m.getPoster())) {
                                Glide.with(SeriesListFragment.this)
                                        .asFile()
                                        .load(m.getPoster())
                                        .into(new CustomTarget<File>() {
                                            @Override
                                            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                                Uri myImageUri = Uri.fromFile(resource);
                                                builder.image(myImageUri);
                                                builder.show();
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {

                                            }
                                        });

                            } else {
                                builder.show();
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    class DataLoader extends AsyncTask<String, Void, ArrayList<Series>> {
        private volatile boolean running = true;
        private volatile String _RunKey;

        @Override
        protected void onCancelled() {
            running = false;
        }

        @Override
        protected ArrayList<Series> doInBackground(String... params) {
            _RunKey = params[0];

            ArrayList<Series> SeriesList = new ArrayList<>();

            if (!RunKey.equals(_RunKey) | !running)
                return SeriesList;

            try {
                String strSQL = "Select Series.* FROM Series ";
                String strJoin = " LEFT JOIN ListsSeries ON (Series._id = ListsSeries.SeriesID) ";
                String strWhere = "";
                String strGroup = "";
                String strLimit = " Limit " + (iRecordPage * iRecordShow) + "," + iRecordShow;
                String strOrderBy = " Order By SeriesName COLLATE LOCALIZED ";

                if (bIsFirstLoaded) {
                    strLimit = "Limit 0," + iFirstLoadRowCount;
                } else {
                    if (iRecordPage == 0) {
                        if (iRecordCount >= iRecordShow)
                            strLimit = "Limit " + (iFirstLoadRowCount) + "," + (iRecordShow - iFirstLoadRowCount);
                        else
                            strLimit = "Limit " + (iFirstLoadRowCount) + "," + (iRecordCount - iFirstLoadRowCount);
                    }
                }

                if (!mFilter.isEmpty()) {
                    if (!TextUtils.isEmpty(strWhere))
                        strWhere += " and (" + mFilter + ")";
                    else
                        strWhere += " Where " + mFilter;
                }

                if (!mFilterStatic.isEmpty()) {
                    if (!TextUtils.isEmpty(strWhere))
                        strWhere += " and (" + mFilterStatic + ")";
                    else
                        strWhere += " Where " + mFilterStatic;
                }

                if (!mOrder.isEmpty()) {
                    strOrderBy = " Order By " + mOrder;
                }

                if (iRecordCount == 0) {
                    String sqlCount = "select count(*) from (" + strSQL + strJoin + strWhere + strGroup + ")";
                    Cursor cCount = dbHelper.GetCursor(sqlCount);
                    if (cCount != null) {
                        cCount.moveToFirst();
                        iRecordCount = cCount.getInt(0);

                        cCount.close();
                    }
                }

                Cursor cursor = dbHelper.GetCursor(strSQL + strJoin + strWhere + strGroup + strOrderBy + strLimit);

                int idxID = cursor.getColumnIndex(Series.COLUMNS._ID);
                int idxSeriesName = cursor.getColumnIndex(Series.COLUMNS.SeriesName);
                int idxPoster = cursor.getColumnIndex(Series.COLUMNS.Poster);
                int idxNetwork = cursor.getColumnIndex(Series.COLUMNS.Network);
                int idxAirDay = cursor.getColumnIndex(Series.COLUMNS.AirDay);
                int idxAirTime = cursor.getColumnIndex(Series.COLUMNS.AirTime);
                int idxStatus = cursor.getColumnIndex(Series.COLUMNS.Status);

                if (cursor.moveToFirst()) {
                    do {
                        Series m = new Series();
                        m.setID(cursor.getInt(idxID));
                        m.setSeriesName(cursor.getString(idxSeriesName));
                        m.setPoster(cursor.getString(idxPoster));
                        m.setNetwork(cursor.getString(idxNetwork));
                        m.setAirDay(cursor.getString(idxAirDay));
                        m.setAirTime(cursor.getString(idxAirTime));
                        m.setStatus(cursor.getString(idxStatus));

                        long nextAirDateMillis = DbUtils.getLastEpisodeMs(activity, m.getID());
                        if (nextAirDateMillis != -1) {
                            m.setDateInfo(String.format("%s | %s | %s", DateUtils.getDateFormat(nextAirDateMillis, Constants.DATE_FORMAT11), DateUtils.getDateFormat(nextAirDateMillis, Constants.DATE_FORMAT10), m.getNetwork()));
                        } else {
                            m.setDateInfo(String.format("%s | %s | %s", m.getAirDay(), m.getAirTime(), m.getNetwork()));
                        }

                        m.setEpisodeCount(DbUtils.getEpisodeCount(activity, m.getID()));
                        m.setWatchedCount(DbUtils.getEpisodeWatchedCount(activity, m.getID()));
                        m.setCollectedCount(DbUtils.getCollectedCount(activity, m.getID()));

                        SeriesList.add(m);
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception e) {
                NLog.e(TAG, e);
            }

            return SeriesList;
        }

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList<Series> res) {
            try {
                if (!isAdded() | !RunKey.equals(_RunKey) | !running) {
                    return;
                }

                if (mAdapter == null) {
                    mAdapter = new SeriesListAdapter(activity, res);
                    mAdapter.setViewType(iListViewType);
                    mRecyclerView.setAdapter(mAdapter);
                    mAdapter.setOnItemClickListener(new SeriesListAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View itemView, int position) {
                            long SeriesID = mAdapter.getItemId(position);
                            showSeries(SeriesID, itemView);
                        }
                    });

                    mAdapter.setOnItemLongClickListener(new SeriesListAdapter.OnItemLongClickListener() {
                        @Override
                        public void onItemLongClick(final View itemView, final int position) {
                            try {
                                final Series m = mAdapter.getItem(position);

                                final ArrayList<CustomMenuItem> items = new ArrayList<>();
                                items.add(new CustomMenuItem(1, -1, getString(R.string.show)));
                                items.add(new CustomMenuItem(2, -1, getString(R.string.delete)));
                                Page page = ((MainActivity) activity).getCurrentPage();
                                if (page.getPageType() == Page.PageTypes.CustomListSeries) {
                                    items.add(new CustomMenuItem(5, -1, getString(R.string.remove_list)));
                                    items.add(new CustomMenuItem(6, -1, getString(R.string.remove_list_multiple)));
                                } else if (page.getPageType() == Page.PageTypes.Series) {
                                    items.add(new CustomMenuItem(3, -1, getString(R.string.add_to_list)));
                                    items.add(new CustomMenuItem(4, -1, getString(R.string.add_multiple_to_the_list)));
                                }
                                items.add(new CustomMenuItem(7, -1, getString(R.string.share)));

                                CustomMenuAdapter a = new CustomMenuAdapter(activity, items);

                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setCancelable(true);
                                builder.setAdapter(a, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int item) {
                                        try {
                                            CustomMenuItem menuItem = items.get(item);

                                            switch (menuItem.getId()) {
                                                case 1:
                                                    showSeries(m.getID(), itemView);
                                                    break;
                                                case 2:
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    // builder.setTitle("Confirm");
                                                    builder.setMessage(getString(R.string.delete_series));
                                                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            deleteSeries(m);
                                                            BroadcastNotifier mBroadcaster = new BroadcastNotifier(activity);
                                                            mBroadcaster.broadcastIntentWithState(Constants.STATE_REFRESH_SLIDE_MENU_COUNT);
                                                        }
                                                    });

                                                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });

                                                    AlertDialog alert = builder.create();
                                                    alert.show();
                                                    break;
                                                case 3:
                                                    AddToListDialog d = new AddToListDialog(activity);
                                                    d.setListType(2);
                                                    d.setSeries(m);
                                                    d.showDialog();
                                                    break;
                                                case 4:
                                                    m.setIsSelected(true);
                                                    mAdapter.setShowCheckBox(true);
//
                                                    menuSearchItem.setVisible(false);
                                                    menuOkItem.setVisible(true);
                                                    menuCancelItem.setVisible(true);
                                                    break;
                                                case 5:
                                                    int iPageID = Utils.parseInt(((MainActivity) activity).getCurrentPage().getPageID());
                                                    dbHelper.ExecuteQuery("Delete From ListsSeries Where ListID=" + iPageID + " and SeriesID=" + String.valueOf(m.getID()));
                                                    BroadcastNotifier mBroadcaster = new BroadcastNotifier(activity);
                                                    mBroadcaster.broadcastIntentWithState(Constants.STATE_REFRESH_SLIDE_MENU_COUNT);

                                                    mAdapter.remove(m);

                                                    if (mAdapter.getItemCount() == 0) {
                                                        tvNoRecord.setVisibility(View.VISIBLE);
                                                    }
                                                    break;
                                                case 6:
                                                    m.setIsSelected(true);
                                                    mAdapter.setShowCheckBox(true);

                                                    menuSearchItem.setVisible(false);
                                                    menuOkItem.setVisible(true);
                                                    menuCancelItem.setVisible(true);
                                                    break;
                                                case 7:
                                                    share(m);
                                                    break;
                                            }
                                        } catch (Exception e) {
                                            NLog.e(TAG, e);
                                        }
                                    }
                                });
                                AlertDialog alert = builder.create();
                                alert.setCanceledOnTouchOutside(true);
                                alert.show();
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }
                    });

                    mAdapter.setOnImageViewClickListener(new SeriesListAdapter.OnImageViewClickListener() {
                        @Override
                        public void onImageViewClick(View itemView, int position) {
                            Series series = mAdapter.getItem(position);
                            Intent it = new Intent(activity, FullScreenImageActivity.class);
                            it.putExtra(Constants.ImageLink, series.getPoster());
                            activity.startActivity(it);
                        }
                    });

                    mAdapter.setOnItemSelectedListener(new SeriesListAdapter.OnItemSelectedListener() {
                        @Override
                        public void onSelectedChanged(View itemView, int position) {
                            Series series = mAdapter.getItem(position);
                            series.setIsSelected(!series.getIsSelected());
                        }
                    });

                    if (mAdapter.getItemCount() > 0) {
                        if (bIsFirstLoaded) {
                            bIsFirstLoaded = false;
                        }

                        tvNoRecord.setVisibility(View.GONE);

                        if (mAdapter.getItemCount() == iFirstLoadRowCount) {
                            new DataLoader().execute(_RunKey);
                        }

                    } else {
                        tvNoRecord.setVisibility(View.VISIBLE);
                    }

                } else {
                    if (res != null) {
                        mAdapter.addAllEnd(res);
                        //mAdapter.notifyDataSetChanged();

                        if (!isProInstalled) {
                            if (mAdapter.getItemCount() >= GrieeXSettings.FreeRecordLimitSeries) {
                                return;
                            }
                        }

                        if (mAdapter.getItemCount() < iRecordCount) {
                            if (iRecordCount >= iRecordShow)
                                iRecordPage++;

                            new DataLoader().execute(_RunKey);
                        }
                    }
                }

                boolean bLoadingMore = false;
                progress.setVisibility(View.GONE);

                if (mAdapter.getItemCount() == iRecordCount) {

                }
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }

    }

    // **************
    private class ImportServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int iState = intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_NOT_COMPLETED);
                switch (iState) {
                    case Constants.STATE_INSERT_SERIES: {
                        Object o = Objects.requireNonNull(intent.getExtras()).getSerializable(Constants.EXTENDED_DATA_OBJECT);
                        if (o instanceof Series) {
                            mAdapter.add((Series) o);
                            tvNoRecord.setVisibility(View.GONE);
                        }
                        break;
                    }
                    case Constants.STATE_UPDATE_SERIES: {
                        Object o = Objects.requireNonNull(intent.getExtras()).getSerializable(Constants.EXTENDED_DATA_OBJECT);
                        if (o instanceof Series) {
                            updateSeries((Series) o);
                        }
                        break;
                    }
                    case Constants.STATE_DELETE_SERIES: {
                        Object o = Objects.requireNonNull(intent.getExtras()).getSerializable(Constants.EXTENDED_DATA_OBJECT);
                        if (o instanceof Series) {
                            deleteSeries((Series) o);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }
    }

}
