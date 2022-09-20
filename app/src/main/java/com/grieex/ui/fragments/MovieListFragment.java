package com.grieex.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.grieex.R;
import com.grieex.adapter.CustomMenuAdapter;
import com.grieex.adapter.MovieListAdapter;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Prefs;
import com.grieex.helper.SampleRecycler;
import com.grieex.helper.Utils;
import com.grieex.model.Alphabet;
import com.grieex.model.CustomMenuItem;
import com.grieex.model.Page;
import com.grieex.model.tables.Movie;
import com.grieex.model.tables.Movie.COLUMNS;
import com.grieex.ui.FullScreenImageActivity;
import com.grieex.ui.MainActivity;
import com.grieex.ui.MovieDetailActivity;
import com.grieex.ui.SearchMovieActivity;
import com.grieex.ui.dialogs.AddToListDialog;
import com.grieex.ui.dialogs.FilterDialog;
import com.grieex.widget.SimpleDividerItemDecoration;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MovieListFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static final String TAG = MovieListFragment.class.getName();
    private static final String ARG_Filter = "ARG_Filter";
    private static final int iRecordShow = 100;
    private static final int iFirstLoadRowCount = 20;
    private static final int iAlphabetLimit = 50;
    private static float sideIndexX;
    private static float sideIndexY;
    private final ArrayList<Alphabet> FirstCharactersTemp = new ArrayList<>();
    private final Locale locale = new Locale("tr_TR");
    private ArrayList<Alphabet> FirstCharacters;
    private int sideIndexHeight;
    private Activity activity;
    private boolean isProInstalled = false;
    private ProgressBar progress;
    private int iRecordCount = 0;
    private int iRecordPage = 0;
    private boolean bIsFirstLoaded = true;
    private String mFilter = "";
    private String mFilterStatic = "";
    private boolean IsSearchViewVisible = false;
    private String mOrder = "";
    private TextView tvNoRecord;
    private TextView tvSelectedIndex;
    private DatabaseHelper dbHelper;
    private ImportServiceBroadcastReceiver mImportServiceBroadcastReceiver;
    private int iListViewType = 0;
    private MenuItem menuFilterItem;
    private MenuItem menuSearchItem;
    private MenuItem menuOkItem;
    private MenuItem menuCancelItem;
    private String harf = "";
    private MovieListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayout sideIndex;
    private volatile String RunKey;
    private int indexListSize;

    public static MovieListFragment newInstance() {
        return new MovieListFragment();
    }

    public static MovieListFragment newInstance(String filter) {
        MovieListFragment fragment = new MovieListFragment();
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
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.movielist, container, false);
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
                        if (mAdapter != null && mAdapter.getItemCount() >= GrieeXSettings.FreeRecordLimitMovie) {
                            Toast.makeText(activity, activity.getResources().getString(R.string.alert7), Toast.LENGTH_LONG).show();
                            return;
                        }
                    }


                    Intent i = new Intent(getActivity(), SearchMovieActivity.class);
                    i.putExtra(Constants.EXTENDED_DATA_OBJECT, Constants.ContentProviders.TMDb);
                    if (mFilterStatic.contains("ListsMovies.ListID")) {
                        int iPageID = Utils.parseInt(((MainActivity) activity).getCurrentPage().getPageID());
                        i.putExtra(Constants.EXTENDED_DATA_OBJECT3, iPageID);
                    }
                    startActivity(i);
                }
            });

            progress = v.findViewById(R.id.progress);
            tvNoRecord = v.findViewById(R.id.tvNoRecord);
            tvSelectedIndex = v.findViewById(R.id.tvSelectedIndex);

            sideIndex = v.findViewById(R.id.sideIndex);
            sideIndex.setOnTouchListener(new OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    if (action == MotionEvent.ACTION_UP) {
                        // Animation fadeOut =
                        // AnimationUtils.loadAnimation(activity,
                        // R.anim.fade_out);
                        // tvSelectedIndex.setAnimation(fadeOut);
                        tvSelectedIndex.setVisibility(View.GONE);
                    } else {
                        sideIndexX = event.getX();
                        sideIndexY = event.getY();

                        displayListItem();
                    }

                    return true;
                }
            });

            RunKey = Utils.getRandomString();
            new DataLoader().execute(RunKey);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (iRecordCount > iAlphabetLimit)
                    sideIndex.setVisibility(View.VISIBLE);
            } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                sideIndex.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        try {
            inflater.inflate(R.menu.movielist_actionbar_menu, menu);

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

            menuFilterItem = menu.findItem(R.id.action_filter);
            menuFilterItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    FilterDialog d = new FilterDialog(activity);
                    d.setCustomEventListener(new FilterDialog.OnCustomEventListener() {

                        @Override
                        public void onOkClicked(String Filter, String Order) {
                            setFilter(Filter);
                            setOrder(Order);
                            if (!TextUtils.isEmpty(Filter) | !TextUtils.isEmpty(Order)) {
                                menuFilterItem.setIcon(R.drawable.filled_filter);
                            } else {
                                menuFilterItem.setIcon(R.drawable.filter);
                            }
                        }

                        @Override
                        public void onDialogClosed() {


                        }

                        @Override
                        public void onClearClicked() {
                            setFilter("");
                            setOrder("");
                            menuFilterItem.setIcon(R.drawable.filter);
                        }
                    });
                    d.showDialog();
                    d.setFilter(mFilter, mOrder);
                    return false;
                }
            });

            menuOkItem = menu.findItem(R.id.action_ok);
            menuOkItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    try {
                        if (mAdapter.getSelecteds().size() == 0) {
                            mAdapter.setShowCheckBox(false);

                            menuOkItem.setVisible(false);
                            menuCancelItem.setVisible(false);
                            menuFilterItem.setVisible(true);
                            menuSearchItem.setVisible(true);
                        } else {
                            Page page = ((MainActivity) activity).getCurrentPage();
                            if (page.getPageType() == Page.PageTypes.MovieList) {
                                AddToListDialog d = new AddToListDialog(activity);
                                d.setListType(1);
                                d.setMovies(mAdapter.getSelecteds());
                                d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        mAdapter.setShowCheckBox(false);

                                        menuOkItem.setVisible(false);
                                        menuCancelItem.setVisible(false);
                                        menuFilterItem.setVisible(true);
                                        menuSearchItem.setVisible(true);
                                    }
                                });
                                d.showDialog();
                            } else if (page.getPageType() == Page.PageTypes.CustomListMovies) {
                                if (mAdapter.getSelecteds().size() == 0) {
                                    menuFilterItem.setVisible(true);
                                    menuSearchItem.setVisible(true);
                                    menuOkItem.setVisible(false);
                                    menuCancelItem.setVisible(false);

                                    mAdapter.setShowCheckBox(false);
                                } else {
                                    int iPageID = Utils.parseInt(((MainActivity) activity).getCurrentPage().getPageID());
                                    for (Movie movie : mAdapter.getSelecteds()) {
                                        dbHelper.ExecuteQuery("Delete From ListsMovies Where ListID=" + iPageID + " and MovieID=" + String.valueOf(movie.getID()));
                                        mAdapter.remove(movie);
                                    }

                                    BroadcastNotifier mBroadcaster = new BroadcastNotifier(activity);
                                    mBroadcaster.broadcastIntentWithState(Constants.STATE_REFRESH_SLIDE_MENU_COUNT);

                                    menuFilterItem.setVisible(true);
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
            menuCancelItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    mAdapter.setShowCheckBox(false);

                    menuOkItem.setVisible(false);
                    menuCancelItem.setVisible(false);
                    menuFilterItem.setVisible(true);
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
            //newText = DatabaseUtils.sqlEscapeString(newText);
            String stFilter = "";
            stFilter += "OrginalName Like '%" + newText + "%' ";
            stFilter += "or ImdbNumber Like '%" + newText + "%' ";
            stFilter += "or OtherName Like '%" + newText + "%' ";
            stFilter += "or Director Like '%" + newText + "%' ";
            stFilter += "or Writer Like '%" + newText + "%' ";
            stFilter += "or Year Like '%" + newText + "%' ";
            stFilter += "or ArchivesNumber Like '%" + newText + "%' ";
            stFilter += "or UserColumn1 Like '%" + newText + "%' ";
            stFilter += "or UserColumn2 Like '%" + newText + "%' ";
            stFilter += "or UserColumn3 Like '%" + newText + "%' ";
            stFilter += "or UserColumn4 Like '%" + newText + "%' ";
            stFilter += "or  Movies._id in (SELECT ObjectID FROM Casts Where Casts.Name like '%" + newText + "%')";

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

            if (FirstCharacters != null)
                FirstCharacters.clear();

            if (FirstCharactersTemp != null)
                FirstCharactersTemp.clear();

            iRecordCount = 0;
            iRecordPage = 0;
            bIsFirstLoaded = true;
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void setFilter(String str) {
        Boolean IsFiltered = !mFilter.isEmpty();

        if (IsFiltered | !str.isEmpty()) {
            ResetData();
            mFilter = str;
            RunKey = Utils.getRandomString();
            new DataLoader().execute(RunKey);
        }
    }

    private void setOrder(String str) {
        Boolean IsOrdered = !mOrder.isEmpty();

        if (IsOrdered | !str.isEmpty()) {
            ResetData();
            mOrder = str;
            RunKey = Utils.getRandomString();
            new DataLoader().execute(RunKey);
        }
    }

    private void initFirstCharacters() {
        try {
            if (iListViewType != 0)
                return;

            Boolean IsFiltered = !mFilter.isEmpty();

            if (IsFiltered)
                return;

            int iOrientation = getResources().getConfiguration().orientation;
            if (iOrientation == 2) {
                sideIndex.setVisibility(View.GONE);
                // return;
            }

            sideIndex.removeAllViews();

            FirstCharacters = new ArrayList<>();
            FirstCharacters.add(new Alphabet(-1, "#"));
            FirstCharacters.add(new Alphabet(-1, "A"));
            FirstCharacters.add(new Alphabet(-1, "B"));
            FirstCharacters.add(new Alphabet(-1, "C"));
            FirstCharacters.add(new Alphabet(-1, "D"));
            FirstCharacters.add(new Alphabet(-1, "E"));
            FirstCharacters.add(new Alphabet(-1, "F"));
            FirstCharacters.add(new Alphabet(-1, "G"));
            FirstCharacters.add(new Alphabet(-1, "H"));
            FirstCharacters.add(new Alphabet(-1, "I"));
            FirstCharacters.add(new Alphabet(-1, "J"));
            FirstCharacters.add(new Alphabet(-1, "K"));
            FirstCharacters.add(new Alphabet(-1, "L"));
            FirstCharacters.add(new Alphabet(-1, "M"));
            FirstCharacters.add(new Alphabet(-1, "N"));
            FirstCharacters.add(new Alphabet(-1, "O"));
            FirstCharacters.add(new Alphabet(-1, "P"));
            FirstCharacters.add(new Alphabet(-1, "Q"));
            FirstCharacters.add(new Alphabet(-1, "R"));
            FirstCharacters.add(new Alphabet(-1, "S"));
            FirstCharacters.add(new Alphabet(-1, "T"));
            FirstCharacters.add(new Alphabet(-1, "U"));
            FirstCharacters.add(new Alphabet(-1, "V"));
            FirstCharacters.add(new Alphabet(-1, "W"));
            FirstCharacters.add(new Alphabet(-1, "X"));
            FirstCharacters.add(new Alphabet(-1, "Y"));
            FirstCharacters.add(new Alphabet(-1, "Z"));

            sideIndexHeight = sideIndex.getHeight();
            if (sideIndexHeight == 0) {
                DisplayMetrics displaymetrics = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                sideIndexHeight = displaymetrics.heightPixels;
                // Navigation Bar + Tab space comes approximately 80dip
            }

            indexListSize = FirstCharacters.size();

            for (Alphabet a : FirstCharacters) {
                for (Alphabet b : FirstCharactersTemp) {
                    if (a.getCharacter().equals(b.getCharacter())) {
                        a.setIndex(b.getIndex());
                        break;
                    }

                }
            }

            for (int i = 0; i < indexListSize; i++) {
                Alphabet character = FirstCharacters.get(i);

                TextView tmpTV = new TextView(activity);
                tmpTV.setText(character.getCharacter());
                tmpTV.setTag(i);
                tmpTV.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                tmpTV.setLayoutParams(params);
                tmpTV.setBackgroundColor(ContextCompat.getColor(activity, R.color.transparent));

                if (character.getIndex() == -1) {
                    tmpTV.setTextColor(Color.LTGRAY);
                    character.setIsActive(false);
                }
                sideIndex.addView(tmpTV);
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void displayListItem() {
        try {
            if (FirstCharacters == null)
                return;

            // compute number of pixels for every side index item
            double pixelPerIndexItem = (double) sideIndexHeight / indexListSize;

            // compute the item index for given event position belongs to
            int itemPosition = (int) (sideIndexY / pixelPerIndexItem);

            if (itemPosition < 0) {
                itemPosition = 0;
            } else if (itemPosition >= FirstCharacters.size()) {
                itemPosition = FirstCharacters.size() - 1;
            }

            // int listLocation = dealList.get(itemPosition) + itemPosition;
            //
            // if (listLocation > totalListSize) {
            // listLocation = totalListSize;
            // }

            if (itemPosition > FirstCharacters.size() || itemPosition < 0)
                return;

            Alphabet a = FirstCharacters.get(itemPosition);
            if (a.getIsActive()) {
                tvSelectedIndex.setVisibility(View.VISIBLE);
                tvSelectedIndex.setText(a.getCharacter());
                mRecyclerView.scrollToPosition(a.getIndex());
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    private void showMovie(Movie m) {
        try {
            Intent it = new Intent(getActivity(), MovieDetailActivity.class);
            it.putExtra(Constants.Movie, m);
            startActivity(it);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void updateMovie(Movie m) {
        try {
            if (mAdapter == null || mAdapter.getItemCount() == 0)
                return;

            for (int position = 0; position < mAdapter.getItemCount(); position++) {
                Movie movie = mAdapter.getItem(position);
                if (movie.getID() == m.getID()) {
                    movie.LoadWithWhereColumn(activity, COLUMNS._ID, String.valueOf(m.getID()));
                    mAdapter.notifyItemChanged(position);
                    break;
                }
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void deleteMovie(Movie m) {
        try {
            for (int position = 0; position < mAdapter.getItemCount(); position++) {
                Movie movie = mAdapter.getItem(position);
                if (movie.getID() == m.getID()) {
                    dbHelper.deleteMovie(movie);
                    mAdapter.remove(movie);

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

    private void share(final Movie m) {
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
                            Movie movie = dbHelper.getMovie(m.getID());

                            CustomMenuItem menuItem = items.get(item);

                            switch (menuItem.getId()) {
                                case 1: {
                                    ShareDialog shareDialog = new ShareDialog(activity);
                                    if (ShareDialog.canShow(ShareLinkContent.class)) {
                                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                                .setQuote(m.getOriginalName() + " / " + m.getEnglishPlot())
                                                .setContentUrl(Uri.parse("https://www.imdb.com/title/" + movie.getImdbNumber()))
                                                .build();

                                        shareDialog.show(linkContent);
                                    }
                                    break;
                                }
                                case 2: {
                                    ShareDialog shareDialog = new ShareDialog(activity);
                                    if (ShareDialog.canShow(ShareLinkContent.class)) {
                                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                                .setQuote(m.getOriginalName() + " / " + m.getEnglishPlot())
                                                .setContentUrl(Uri.parse("https://www.themoviedb.org/movie/" + movie.getTmdbNumber()))
                                                .build();

                                        shareDialog.show(linkContent);
                                    }
                                    break;
                                }
                                case 3: {
                                    TweetComposer.Builder builder = new TweetComposer.Builder(activity);
                                    builder.text("http://www.imdb.com/title/" + movie.getImdbNumber());

                                    if (!TextUtils.isEmpty(m.getPoster())) {
                                        Glide.with(MovieListFragment.this)
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
                                    builder.text("https://www.themoviedb.org/movie/" + movie.getTmdbNumber());

                                    if (!TextUtils.isEmpty(m.getPoster())) {
                                        Glide.with(MovieListFragment.this)
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
                }

        );
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    class DataLoader extends AsyncTask<String, Void, ArrayList<Movie>> {
        private volatile boolean running = true;
        private volatile String _RunKey;

        @Override
        protected void onCancelled() {
            running = false;
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            _RunKey = params[0];

            ArrayList<Movie> MovieList = new ArrayList<>();

            if (!RunKey.equals(_RunKey) | !running)
                return MovieList;

            try {
                String strSQL = "Select Movies._id, Movies.OrginalName, Movies.OtherName, Movies.Director, Movies.Year, Movies.RunningTime, Movies.Poster, Movies.ArchivesNumber, Movies.ImdbUserRating, Movies.TmdbUserRating, Movies.UpdateDate, Movies.InsertDate FROM Movies ";
                String strJoin = " LEFT JOIN ListsMovies ON (Movies._id = ListsMovies.MovieID) ";
                String strWhere = "";
                String strGroup = " GROUP BY Movies._id ";
                String strLimit = " Limit " + (iRecordPage * iRecordShow) + "," + iRecordShow;
                String strOrderBy = " Order By OrginalName COLLATE LOCALIZED ";

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

                // if (!Filter.isEmpty())
                // strWhere += " Where OriginalName Like '%" + Filter + "%'";

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

                int idxID = cursor.getColumnIndex(Movie.COLUMNS._ID);
                int idxOriginalName = cursor.getColumnIndex(Movie.COLUMNS.OriginalName);
                int idxOtherName = cursor.getColumnIndex(Movie.COLUMNS.OtherName);
                int idxDirector = cursor.getColumnIndex(Movie.COLUMNS.Director);
                int idxYear = cursor.getColumnIndex(Movie.COLUMNS.Year);
                int idxRunningTime = cursor.getColumnIndex(Movie.COLUMNS.RunningTime);
                int idxPoster = cursor.getColumnIndex(Movie.COLUMNS.Poster);
                int idxArchivesNumber = cursor.getColumnIndex(COLUMNS.ArchivesNumber);
                int idxImdbUserRating = cursor.getColumnIndex(COLUMNS.ImdbUserRating);
                int idxTmdbUserRating = cursor.getColumnIndex(COLUMNS.TmdbUserRating);
                int idxUpdateDate = cursor.getColumnIndex(COLUMNS.UpdateDate);
                int idxInsertDate = cursor.getColumnIndex(COLUMNS.InsertDate);


                if (cursor.moveToFirst()) {
                    do {
                        Movie m = new Movie();
                        m.setID(cursor.getInt(idxID));
                        m.setOriginalName(cursor.getString(idxOriginalName));
                        m.setOtherName(cursor.getString(idxOtherName));
                        m.setDirector(cursor.getString(idxDirector));
                        m.setYear(cursor.getString(idxYear));
                        m.setRunningTime(cursor.getString(idxRunningTime));
                        m.setPoster(cursor.getString(idxPoster));
                        m.setArchivesNumber(cursor.getString(idxArchivesNumber));
                        m.setImdbUserRating(cursor.getString(idxImdbUserRating));
                        m.setTmdbUserRating(cursor.getString(idxTmdbUserRating));
                        m.setUpdateDate(DateUtils.ConvertDateToString(Constants.DATE_FORMAT4, cursor.getString(idxUpdateDate)));
                        m.setInsertDate(DateUtils.ConvertDateToString(Constants.DATE_FORMAT4, cursor.getString(idxInsertDate)));

                        if (iListViewType == 0) {
                            String FirstCharacter = "#";
                            if (!TextUtils.isEmpty(m.getOriginalName())) {
                                FirstCharacter = m.getOriginalName().substring(0, 1).toLowerCase();// cursor.getString(cursor.getColumnIndex("FirstCharacter"));
                                FirstCharacter = Utils.reverseCharactersUpper(FirstCharacter.toUpperCase(locale));
                                m.setFirstCharacter(FirstCharacter);
                            }

                            if (iRecordCount > iAlphabetLimit && !harf.equals(FirstCharacter)) {
                                if (Utils.isNumeric(FirstCharacter))
                                    FirstCharacter = "#";

                                harf = FirstCharacter;
                                int iIndex = MovieList.size();

                                if (mAdapter != null)
                                    iIndex += mAdapter.getItemCount();

                                FirstCharactersTemp.add(new Alphabet(iIndex, harf));
                            }
                        }

                        MovieList.add(m);
                    } while (cursor.moveToNext());
                }

                cursor.close();

            } catch (Exception e) {
                NLog.e(TAG, e);
            }

            return MovieList;
        }

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> res) {
            try {
                if (!isAdded() | !RunKey.equals(_RunKey) | !running) {
                    return;
                }

                if (mAdapter == null) {
                    mAdapter = new MovieListAdapter(res, activity);
                    mAdapter.setViewType(iListViewType);
                    mRecyclerView.setAdapter(mAdapter);

                    if (mOrder.contains(COLUMNS.ArchivesNumber))
                        mAdapter.setShowOrderText(COLUMNS.ArchivesNumber);
                    else if (mOrder.contains(COLUMNS.InsertDate))
                        mAdapter.setShowOrderText(COLUMNS.InsertDate);
                    else if (mOrder.contains(COLUMNS.UpdateDate))
                        mAdapter.setShowOrderText(COLUMNS.UpdateDate);
                    else if (mOrder.contains(COLUMNS.ImdbUserRating))
                        mAdapter.setShowOrderText(COLUMNS.ImdbUserRating);
                    else if (mOrder.contains(COLUMNS.TmdbUserRating))
                        mAdapter.setShowOrderText(COLUMNS.TmdbUserRating);


                    mAdapter.setOnItemClickListener(new MovieListAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View itemView, int position) {
                            Movie m = mAdapter.getItem(position);
                            showMovie(m);
                        }
                    });

                    mAdapter.setOnItemLongClickListener(new MovieListAdapter.OnItemLongClickListener() {
                        @Override
                        public void onItemLongClick(final View itemView, final int position) {
                            try {
                                final Movie m = mAdapter.getItem(position);

                                final ArrayList<CustomMenuItem> items = new ArrayList<>();
                                items.add(new CustomMenuItem(1, -1, getString(R.string.show)));
                                items.add(new CustomMenuItem(2, -1, getString(R.string.delete)));

                                Page page = ((MainActivity) activity).getCurrentPage();
                                if (page.getPageType() == Page.PageTypes.CustomListMovies) {
                                    items.add(new CustomMenuItem(5, -1, getString(R.string.remove_list)));
                                    items.add(new CustomMenuItem(6, -1, getString(R.string.remove_list_multiple)));
                                } else if (page.getPageType() == Page.PageTypes.MovieList) {
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
                                                    showMovie(m);
                                                    break;
                                                case 2:
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    // builder.setTitle("Confirm");
                                                    builder.setMessage(getString(R.string.alert1));
                                                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            deleteMovie(m);
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
                                                    d.setListType(1);
                                                    d.setMovie(m);
                                                    d.showDialog();
                                                    break;
                                                case 4:
                                                    m.setIsSelected(true);
                                                    mAdapter.setShowCheckBox(true);

                                                    menuFilterItem.setVisible(false);
                                                    menuSearchItem.setVisible(false);
                                                    menuOkItem.setVisible(true);
                                                    menuCancelItem.setVisible(true);
                                                    break;
                                                case 5:
                                                    int iPageID = Utils.parseInt(((MainActivity) activity).getCurrentPage().getPageID());
                                                    dbHelper.ExecuteQuery("Delete From ListsMovies Where ListID=" + iPageID + " and MovieID=" + String.valueOf(m.getID()));
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

                                                    menuFilterItem.setVisible(false);
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

                    mAdapter.setOnImageViewClickListener(new MovieListAdapter.OnImageViewClickListener() {
                        @Override
                        public void onImageViewClick(View itemView, int position) {
                            Movie movie = mAdapter.getItem(position);
                            Intent it = new Intent(activity, FullScreenImageActivity.class);
                            it.putExtra(Constants.ImageLink, movie.getPoster());
                            activity.startActivity(it);
                        }
                    });

                    mAdapter.setOnItemSelectedListener(new MovieListAdapter.OnItemSelectedListener() {
                        @Override
                        public void onSelectedChanged(View itemView, int position) {
                            Movie movie = mAdapter.getItem(position);
                            movie.setIsSelected(!movie.getIsSelected());
                        }
                    });

                    if (mAdapter.getItemCount() > 0) {
                        if (bIsFirstLoaded) {
                            if (iRecordCount < iAlphabetLimit) {
                                sideIndex.setVisibility(View.GONE);
                            }
                            bIsFirstLoaded = false;
                        }

                        tvNoRecord.setVisibility(View.GONE);

                        if (mAdapter.getItemCount() == iFirstLoadRowCount) {
                            new DataLoader().execute(_RunKey);
                        }

                    } else {
                        tvNoRecord.setVisibility(View.VISIBLE);
                        sideIndex.setVisibility(View.GONE);
                    }

                } else {
                    if (res != null) {
                        mAdapter.addAllEnd(res);
                        //mAdapter.notifyDataSetChanged();

                        if (!isProInstalled) {
                            if (mAdapter.getItemCount() >= GrieeXSettings.FreeRecordLimitMovie) {
                                if (iRecordCount > iAlphabetLimit) {
                                    sideIndex.setVisibility(View.VISIBLE);
                                    initFirstCharacters();
                                } else {
                                    sideIndex.setVisibility(View.GONE);
                                }

                                return;
                            }
                        }

                        if (mAdapter.getItemCount() < iRecordCount) {
                            if (iRecordCount >= iRecordShow)
                                iRecordPage++;

                            new DataLoader().execute(_RunKey);
                        } else {
                            if (iRecordCount > iAlphabetLimit) {
                                sideIndex.setVisibility(View.VISIBLE);
                                initFirstCharacters();
                            } else {
                                sideIndex.setVisibility(View.GONE);
                            }
                        }
                    }
                }

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
                    case Constants.STATE_INSERT_MOVIE: {
                        Object o = intent.getExtras().getSerializable(Constants.EXTENDED_DATA_OBJECT);
                        if (o != null && o instanceof Movie) {
                            mAdapter.add((Movie) o);
                            tvNoRecord.setVisibility(View.GONE);
                        }
                        break;
                    }
                    case Constants.STATE_UPDATE_MOVIE: {
                        Object o = intent.getExtras().getSerializable(Constants.EXTENDED_DATA_OBJECT);
                        if (o != null && o instanceof Movie) {
                            updateMovie((Movie) o);
                        }
                        break;
                    }
                    case Constants.STATE_DELETE_MOVIE: {
                        Object o = intent.getExtras().getSerializable(Constants.EXTENDED_DATA_OBJECT);
                        if (o != null && o instanceof Movie) {
                            deleteMovie((Movie) o);
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
