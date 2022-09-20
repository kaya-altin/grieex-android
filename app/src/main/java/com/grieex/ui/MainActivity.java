package com.grieex.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.grieex.R;
import com.grieex.adapter.SlideMenuAdapter;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Prefs;
import com.grieex.helper.RateThisApp;
import com.grieex.helper.Utils;
import com.grieex.model.Page;
import com.grieex.model.Page.PageTypes;
import com.grieex.sync.AccountUtils;
import com.grieex.ui.fragments.AboutFragment;
import com.grieex.ui.fragments.BatchProcessingFragment;
import com.grieex.ui.fragments.Imdb250ListFragment;
import com.grieex.ui.fragments.MovieListFragment;
import com.grieex.ui.fragments.PublicListFragment;
import com.grieex.ui.fragments.SeriesListFragment;
import com.grieex.ui.fragments.SettingsFragment;
import com.grieex.ui.fragments.UpcomingSeriesFragment;
import com.grieex.update.UpdateManager;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getName();

    private Fragment mContentFragment;
    private ActionBar actionBar;

    private DrawerLayout mDrawerLayout;
    private ListView lvMenuList;
    private LinearLayout left_drawer;
    private SlideMenuAdapter mAdapter;
    private LinearLayout llBottom;
    private int iLastPage = 1;
    private ProgressBar progress_top;

    private CustomBroadcastReceiver mCustomBroadcastReceiver;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            Toolbar toolbar = findViewById(R.id.myToolbar);
            setSupportActionBar(toolbar);

            if (UpdateManager.NewVersionFound(this)) {
                UpdateManager.Start(getApplicationContext());
                finish();
                return;
            }

            actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(R.string.app_name);
            }

            progress_top = findViewById(R.id.progress_top);

            mDrawerLayout = findViewById(R.id.drawer_layout);
            mDrawerLayout.setFocusable(false);
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

            left_drawer = findViewById(R.id.left_drawer);
            lvMenuList = findViewById(R.id.lvMenuList);
            llBottom = findViewById(R.id.llBottom);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            mDrawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            lvMenuList.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SetPage(position);
                }
            });

            lvMenuList.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> adapter, final View v, final int position, long id) {
                    try {
                        final Page p = mAdapter.getItem(position);
                        if (p.getPageType() == PageTypes.CustomListMovies) {
                            final CharSequence[] items = getResources().getStringArray(R.array.array1);

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setCancelable(true);
                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (item == 0) {
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                        alertDialog.setTitle(getString(R.string.list_name));
                                        // alertDialog.setMessage("Enter Password");

                                        final EditText input = new EditText(MainActivity.this);
                                        input.setText(p.getPageName());
                                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                                        input.setLayoutParams(lp);
                                        alertDialog.setView(input);

                                        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    if (TextUtils.isEmpty(input.getText().toString())) {
                                                        input.setError(getString(R.string.can_not_be_empty));
                                                        return;
                                                    }

                                                    DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
                                                    dbHelper.ExecuteQuery("Update Lists Set ListName=" + DatabaseUtils.sqlEscapeString(input.getText().toString()) + " Where _id=" + p.getPageID());

                                                    LoadMenu();
                                                    dialog.dismiss();
                                                } catch (Exception e) {
                                                    NLog.e(TAG, e);
                                                }
                                            }
                                        });

                                        alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });

                                        alertDialog.show();
                                    } else if (item == 1) {
                                        try {
                                            com.grieex.model.tables.Lists o = (com.grieex.model.tables.Lists) p.getObject();
                                            DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
                                            dbHelper.ExecuteQuery("Delete From Lists Where _id=" + String.valueOf(o.getID()));
                                            dbHelper.ExecuteQuery("Delete From ListsMovies Where ListID=" + String.valueOf(o.getID()));

                                            LoadMenu();
                                        } catch (Exception e) {
                                            NLog.e(TAG, e);
                                        }
                                    }
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.setCanceledOnTouchOutside(true);
                            alert.show();
                        } else if (p.getPageType() == PageTypes.CustomListSeries) {
                            final CharSequence[] items = getResources().getStringArray(R.array.array1);

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setCancelable(true);
                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (item == 0) {
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                        alertDialog.setTitle(getString(R.string.list_name));
                                        // alertDialog.setMessage("Enter Password");

                                        final EditText input = new EditText(MainActivity.this);
                                        input.setText(p.getPageName());
                                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                                        input.setLayoutParams(lp);
                                        alertDialog.setView(input);

                                        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    if (TextUtils.isEmpty(input.getText().toString())) {
                                                        input.setError(getString(R.string.can_not_be_empty));
                                                        return;
                                                    }

                                                    DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
                                                    dbHelper.ExecuteQuery("Update Lists Set ListName=" + DatabaseUtils.sqlEscapeString(input.getText().toString()) + " Where _id=" + p.getPageID());

                                                    LoadMenu();
                                                    dialog.dismiss();
                                                } catch (Exception e) {
                                                    NLog.e(TAG, e);
                                                }
                                            }
                                        });

                                        alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });

                                        alertDialog.show();
                                    } else if (item == 1) {
                                        try {
                                            com.grieex.model.tables.Lists o = (com.grieex.model.tables.Lists) p.getObject();
                                            DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
                                            dbHelper.ExecuteQuery("Delete From Lists Where _id=" + String.valueOf(o.getID()));
                                            dbHelper.ExecuteQuery("Delete From ListsSeries Where ListID=" + String.valueOf(o.getID()));

                                            LoadMenu();
                                        } catch (Exception e) {
                                            NLog.e(TAG, e);
                                        }
                                    }
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.setCanceledOnTouchOutside(true);
                            alert.show();
                        }
                    } catch (Exception e) {
                        NLog.e(TAG, e);
                    }
                    return true;
                }
            });

            final RadioGroup radioGroup1 = findViewById(R.id.radioGroup1);
            RadioButton view = (RadioButton) radioGroup1.getChildAt(Prefs.with(MainActivity.this).getInt(Constants.ListViewType, 0));
            view.setChecked(true);

            radioGroup1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    switch (checkedId) {
                        case R.id.radio0:
                            Prefs.with(MainActivity.this).save(Constants.ListViewType, 0);
                            break;
                        case R.id.radio1:
                            Prefs.with(MainActivity.this).save(Constants.ListViewType, 1);
                            break;
                    }


                    SetPage(iLastPage);
                }
            });

            LoadMenu();

            setPageFromPageId(GrieeXSettings.getLastPage(MainActivity.this));

            //SyncControl();

            //AdView mAdView = (AdView) findViewById(R.id.ad_view);

            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    if (!Utils.isProInstalled(MainActivity.this)) {
                        LinearLayout llContent = findViewById(R.id.llContent);

                        AdView mAdView = new AdView(MainActivity.this);
                        mAdView.setAdUnitId(getString(R.string.banner_ad_unit_id));
                        if (Utils.isTablet(MainActivity.this)) {
                            mAdView.setAdSize(AdSize.FULL_BANNER);
                        } else {
                            mAdView.setAdSize(AdSize.BANNER);
                        }
                        llContent.addView(mAdView);
                        AdRequest adRequest = new AdRequest.Builder().build();
                        mAdView.loadAd(adRequest);
                    }
                }
            });


            if (mCustomBroadcastReceiver == null) {
                IntentFilter statusIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
                statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
                mCustomBroadcastReceiver = new CustomBroadcastReceiver();
                LocalBroadcastManager.getInstance(this).registerReceiver(mCustomBroadcastReceiver, statusIntentFilter);
            }

            // Monitor launch times and interval from installation
            RateThisApp.onStart(this);
            // Show a dialog if criteria is satisfied
            RateThisApp.showRateDialogIfNeeded(this);

            if (!AccountUtils.isAccountExists(this)) {
                AccountUtils.createAccount(this);
            }

            //Kaya
            //AccountUtils.startSync(this);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.twice_back), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        try {
            if (mCustomBroadcastReceiver != null) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mCustomBroadcastReceiver);
                mCustomBroadcastReceiver = null;
            }

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(left_drawer)) {
                mDrawerLayout.closeDrawer(left_drawer);
            } else {
                mDrawerLayout.openDrawer(left_drawer);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Page getCurrentPage() {
        return mAdapter.getItem(iLastPage);
    }

    private void switchContent(Page page) {
        try {
            // if (CurrentPage == page) {
            // toggle();
            // return;
            // }

            if (page.getPageType().getValue() < 50)
                GrieeXSettings.setLastPage(MainActivity.this, page.getPageType().getValue());


            if (page.getPageType() == PageTypes.Search) {
                Intent it = new Intent(MainActivity.this, SearchMovieActivity.class);
                startActivity(it);
            } else {

                switch (page.getPageType()) {
                    case MovieList:
                        mContentFragment = MovieListFragment.newInstance();
                        break;
                    case Imdb250:
                        mContentFragment = Imdb250ListFragment.newInstance(Constants.Imdb250Type.Movie);
                        break;
                    case Settings:
                        mContentFragment = new SettingsFragment();
                        break;
                    case BatchProcessing:
                        mContentFragment = new BatchProcessingFragment();
                        break;
                    case About:
                        mContentFragment = new AboutFragment();
                        break;
                    case CustomListMovies:
                        mContentFragment = MovieListFragment.newInstance("ListsMovies.ListID=" + page.getPageID());
                        break;
                    case CustomListSeries:
                        mContentFragment = SeriesListFragment.newInstance("ListsSeries.ListID=" + page.getPageID());
                        break;
                    case PopularMovies:
                        mContentFragment = PublicListFragment.newInstance(PageTypes.PopularMovies);
                        break;
                    case NowPlayingMovies:
                        mContentFragment = PublicListFragment.newInstance(PageTypes.NowPlayingMovies);
                        break;
                    case UpcomingMovies:
                        mContentFragment = PublicListFragment.newInstance(PageTypes.UpcomingMovies);
                        break;
                    case Series:
                        mContentFragment = SeriesListFragment.newInstance();
                        break;
                    case UpcomingSeries:
                        mContentFragment = UpcomingSeriesFragment.newInstance();
                        break;
                    case Imdb250Series:
                        mContentFragment = Imdb250ListFragment.newInstance(Constants.Imdb250Type.Series);
                        break;
                }

                if (TextUtils.isEmpty(page.getPageTitle())) {
                    actionBar.setTitle(page.getPageName());
                } else {
                    actionBar.setTitle(page.getPageTitle());
                }

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, mContentFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                // ft.addToBackStack(null); // geri tuşu için
                ft.commit();

                mDrawerLayout.closeDrawer(left_drawer);
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void LoadCount() {
        try {

            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            for (int i = 0; i < mAdapter.getCount(); i++) {
                Page p = mAdapter.getItem(i);
                switch (p.getPageType()) {
                    case MovieList: {
                        String strCount = dbHelper.GetOneField("Select Count(*) From Movies");
                        p.setCount(Utils.parseInt(strCount));
                        break;
                    }
                    case CustomListMovies: {
                        String strCount = dbHelper.GetOneField("SELECT Count(*) FROM Movies INNER JOIN ListsMovies ON (Movies._id = ListsMovies.MovieID) Where ListsMovies.ListID = " + p.getPageID());
                        p.setCount(Utils.parseInt(strCount));
                        break;
                    }
                    case CustomListSeries: {
                        String strCount = dbHelper.GetOneField("SELECT Count(*) FROM Series INNER JOIN ListsSeries ON (Series._id = ListsSeries.SeriesID) Where ListsSeries.ListID = " + p.getPageID());
                        p.setCount(Utils.parseInt(strCount));
                        break;
                    }
                    case Series: {
                        String strCount = dbHelper.GetOneField("SELECT Count(*) FROM Series");
                        p.setCount(Utils.parseInt(strCount));
                        break;
                    }
                }
            }

            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void LoadMenu() {
        try {
            mAdapter = new SlideMenuAdapter(this);
            mAdapter.add(new Page(PageTypes.Separator, "", getString(R.string.movies), "", R.drawable.video_multi, false));
            mAdapter.add(new Page(PageTypes.MovieList, "0", getString(R.string.collection), getString(R.string.app_name), 0, true));
            mAdapter.add(new Page(PageTypes.NowPlayingMovies, "", getString(R.string.now_playing), "", 0, true));
            mAdapter.add(new Page(PageTypes.PopularMovies, "", getString(R.string.popular), "", 0, true));
            mAdapter.add(new Page(PageTypes.UpcomingMovies, "", getString(R.string.upcoming), "", 0, true));
            mAdapter.add(new Page(PageTypes.Imdb250, "", getString(R.string.imdb_250), "", 0, true));

            mAdapter.add(new Page(PageTypes.Separator, "", getString(R.string.series), "", R.drawable.tv, false));
            mAdapter.add(new Page(PageTypes.Series, "0", getString(R.string.collection), getString(R.string.app_name), 0, true));
            mAdapter.add(new Page(PageTypes.UpcomingSeries, "", getString(R.string.upcoming), "", 0, true));
            mAdapter.add(new Page(PageTypes.Imdb250Series, "", getString(R.string.imdb_250), "", 0, true));
            //mAdapter.add(new Page(PageTypes.PopularSeries, "", getString(R.string.popular), "", 0, true));

            ArrayList<com.grieex.model.tables.Lists> objects = (ArrayList<com.grieex.model.tables.Lists>) DatabaseHelper.getInstance(this).GetCursorWithObject("SELECT distinct Lists.* FROM Movies INNER JOIN ListsMovies ON (Movies._id = ListsMovies.MovieID) INNER JOIN Lists ON (Lists._id = ListsMovies.ListID) union all SELECT distinct Lists.* FROM Series INNER JOIN ListsSeries ON (Series._id = ListsSeries.SeriesID) INNER JOIN Lists ON (Lists._id = ListsSeries.ListID) Order By Lists.ListType,Lists.ListName", com.grieex.model.tables.Lists.class);

            if (objects != null && objects.size() > 0) {
                mAdapter.add(new Page(PageTypes.Separator, "", getString(R.string.lists), "", R.drawable.list_view, false));

                for (com.grieex.model.tables.Lists object : objects) {
                    Page p = new Page();
                    //p.setIcon(R.drawable.dot);
                    if (object.getListType() == 1) {
                        p.setIcon(R.drawable.video_multi);
                        p.setPageType(PageTypes.CustomListMovies);
                    } else {
                        p.setIcon(R.drawable.tv);
                        p.setPageType(PageTypes.CustomListSeries);
                    }
                    p.setPageID(String.valueOf(object.getID()));
                    p.setPageName(object.getListName());
                    p.setObject(object);
                    mAdapter.add(p);
                }
            }

            mAdapter.add(new Page(PageTypes.Separator, "", "", "", 0, false));
            mAdapter.add(new Page(PageTypes.Settings, "", getString(R.string.settings), "", R.drawable.settings, true));
            if (!Utils.isProInstalled(this)) {
                mAdapter.add(new Page(PageTypes.GoProVersion, "", getString(R.string.go_pro_version), "", R.drawable.lock, false));
            }
            mAdapter.add(new Page(PageTypes.About, "", getString(R.string.about), "", R.drawable.about, true));

            lvMenuList.setAdapter(mAdapter);

            LoadCount();
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void setPageFromPageId(int pageId) {
        for (int position = 0; position < mAdapter.getCount(); position++) {
            Page page = mAdapter.getItem(position);
            if (page.getPageType().getValue() == pageId) {
                if (page.getPageType() == PageTypes.PopularMovies || page.getPageType() == PageTypes.NowPlayingMovies || page.getPageType() == PageTypes.UpcomingMovies || page.getPageType() == PageTypes.UpcomingSeries) {
                    if (!Connectivity.isConnected(this)) {
                        SetPage(1);
                        return;
                    }
                }

                SetPage(position);
                break;
            }
        }
    }

    private void SetPage(int iPosition) {
        try {
            Page page = mAdapter.getItem(iPosition);

            if (page.getPageType() == PageTypes.GoProVersion) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + GrieeXSettings.GrieeXPro)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + GrieeXSettings.GrieeXPro)));
                }
            }

            if (!page.getSelectable()) {
                lvMenuList.setItemChecked(iLastPage, true);
                return;
            }

            if (page.getPageType() == PageTypes.PopularMovies || page.getPageType() == PageTypes.NowPlayingMovies || page.getPageType() == PageTypes.UpcomingMovies || page.getPageType() == PageTypes.UpcomingSeries) {
                if (!Connectivity.isConnected(this)) {
                    lvMenuList.setItemChecked(iLastPage, true);
                    Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            iLastPage = iPosition;

            if (iPosition <= mAdapter.getCount()) {
                if (page.getSelectable())
                    lvMenuList.setItemChecked(iPosition, true);
            } else
                lvMenuList.setItemChecked(2, true);

            switchContent(page);

            if (page.getPageType() == PageTypes.Imdb250 || page.getPageType() == PageTypes.Imdb250Series || page.getPageType() == PageTypes.UpcomingSeries || page.getPageType() == PageTypes.Settings || page.getPageType() == PageTypes.About) {
                llBottom.setVisibility(View.GONE);
            } else {
                llBottom.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    // **************
    private class CustomBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int iState = intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_NO_STATUS);
                if (iState == Constants.STATE_REFRESH_SLIDE_MENU) {
                    LoadMenu();
                } else if (iState == Constants.STATE_DROPBOX_SYNC_COMLETED) {
                    if (mContentFragment instanceof SettingsFragment) {
                        // ((SettingsFragment)
                        // mContentFragment).SyncCompleted();
                    }
                } else if (iState == Constants.STATE_INSERT_MOVIE |
                        iState == Constants.STATE_DELETE_MOVIE |
                        iState == Constants.STATE_REFRESH_SLIDE_MENU_COUNT |
                        iState == Constants.STATE_INSERT_SERIES |
                        iState == Constants.STATE_DELETE_SERIES) {
                    LoadCount();
                } else if (iState == Constants.STATE_SYNC_STARTED) {
                    progress_top.setVisibility(View.VISIBLE);
                } else if (iState == Constants.STATE_SYNC_ENDED) {
                    progress_top.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }
    }
}
