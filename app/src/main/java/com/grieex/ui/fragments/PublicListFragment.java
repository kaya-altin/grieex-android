package com.grieex.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.adapter.CustomMenuAdapter;
import com.grieex.adapter.PublicListAdapter;
import com.grieex.core.ImportQueues;
import com.grieex.core.SearchResult;
import com.grieex.core.Tmdb;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.DbUtils;
import com.grieex.helper.EndlessRecyclerOnScrollListener;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Prefs;
import com.grieex.helper.SampleRecycler;
import com.grieex.helper.Utils;
import com.grieex.core.listener.OnTmdbEventListener;
import com.grieex.model.CustomMenuItem;
import com.grieex.model.Page;
import com.grieex.model.tables.Movie;
import com.grieex.service.ServiceManager;
import com.grieex.ui.FullScreenImageActivity;
import com.grieex.ui.MovieDetailActivity;
import com.grieex.ui.dialogs.AddToListDialog;
import com.grieex.widget.SimpleDividerItemDecoration;

import java.util.ArrayList;


public class PublicListFragment extends Fragment {
    private static final String TAG = PublicListFragment.class.getName();

    private Activity activity;

    private static final String ARG_PageTypes = "ARG_PageTypes";

    public static PublicListFragment newInstance() {
        return new PublicListFragment();
    }

    public static PublicListFragment newInstance(Page.PageTypes pageTypes) {
        PublicListFragment fragment = new PublicListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PageTypes, pageTypes);
        fragment.setArguments(args);
        return fragment;
    }

    private PublicListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    private ProgressBar progressBarMovieList;
    private Page.PageTypes mPageType;
    private Tmdb tmdb;

    private int iListViewType = 0;
    private String locale = "en";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        try {
            activity = getActivity();
            if (getArguments() != null) {
                mPageType = (Page.PageTypes) getArguments().getSerializable(ARG_PageTypes);
            }
            iListViewType = Prefs.with(activity).getInt(Constants.ListViewType, 0);
            locale = GrieeXSettings.getLocale(activity);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.publiclist, container, false);
        try {
            mRecyclerView = v.findViewById(R.id.list);
            mRecyclerView.setHasFixedSize(true);
            if (iListViewType == 0) {
                mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(activity, R.drawable.divider2));
                mLayoutManager = new LinearLayoutManager(activity);
            } else {
               mLayoutManager = new GridLayoutManager(activity, getResources().getInteger(R.integer.recylerviewColumnCount));
            }
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(new SampleRecycler());
            mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener((LinearLayoutManager) mLayoutManager) {
                @Override
                public void onLoadMore(int page) {
                    if (!Connectivity.isConnected(activity)) {
                        Toast.makeText(activity, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    progressBarMovieList.setVisibility(View.VISIBLE);

                    switch (mPageType) {
                        case PopularMovies:
                            tmdb.getPopularList(locale, page);
                            break;
                        case NowPlayingMovies:
                            tmdb.getNowPlayingList(locale, page);
                            break;
                        case UpcomingMovies:
                            tmdb.getUpcomingList(locale, page);
                            break;
                    }
                }
            });

            progressBarMovieList = v.findViewById(R.id.progressBarMovieList);


            tmdb = new Tmdb();
            tmdb.setTmdbEventListener(new OnTmdbEventListener() {
                @Override
                public void onCompleted(Object m) {

                    if (mAdapter == null) {
                        mAdapter = new PublicListAdapter((ArrayList<Movie>) m);
                        mAdapter.setViewType(iListViewType);
                        mRecyclerView.setAdapter(mAdapter);

                        mAdapter.setOnItemClickListener(new PublicListAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View itemView, int position) {
                                if (!Connectivity.isConnected(activity)) {
                                    Toast.makeText(activity, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Movie m = mAdapter.getItem(position);
                                showMovie(m);
                            }
                        });

                        mAdapter.setOnImageViewClickListener(new PublicListAdapter.OnImageViewClickListener() {
                            @Override
                            public void onImageViewClick(View itemView, int position) {
                                Movie movie = mAdapter.getItem(position);
                                Intent it = new Intent(activity, FullScreenImageActivity.class);
                                it.putExtra(Constants.ImageLink, movie.getPoster());
                                activity.startActivity(it);
                            }
                        });

                        mAdapter.setOnItemLongClickListener(new PublicListAdapter.OnItemLongClickListener() {
                            @Override
                            public void onItemLongClick(View itemView, int position) {
                                try {
                                    final Movie m = mAdapter.getItem(position);

                                    final ArrayList<CustomMenuItem> items = new ArrayList<>();
                                    items.add(new CustomMenuItem(1, -1, getString(R.string.add_to_collection)));
                                    items.add(new CustomMenuItem(2, -1, getString(R.string.add_to_list)));

                                    CustomMenuAdapter a = new CustomMenuAdapter(activity, items);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                    builder.setCancelable(true);
                                    builder.setAdapter(a, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int item) {
                                            try {
                                                if (!Utils.isProInstalled(activity)) {
                                                    if (DbUtils.getMoviesCount(activity) >= GrieeXSettings.FreeRecordLimitMovie) {
                                                        Toast.makeText(activity, getResources().getString(R.string.alert7), Toast.LENGTH_LONG).show();
                                                        return;
                                                    }
                                                }

                                                CustomMenuItem menuItem = items.get(item);

                                                if (menuItem.getId() == 1) {
                                                    Movie movie = new Movie();
                                                    movie.setContentProvider(Constants.ContentProviders.TMDb.value);
                                                    movie.setOriginalName(m.getOriginalName());
                                                    movie.setSeen("0");
                                                    movie.setInsertDate(DateUtils.DateTimeNowString());
                                                    movie.setUpdateDate(DateUtils.DateTimeNowString());
                                                    DatabaseHelper db = DatabaseHelper.getInstance(activity.getApplicationContext());
                                                    long _id = db.addMovie(movie);
                                                    movie.setID((int) _id);

                                                    SearchResult sr = new SearchResult(String.valueOf(m.getTmdbNumber()), m.getOriginalName(), m.getPoster(), m.getYear());
                                                    ImportQueues.AddQueue(activity, _id, sr, Constants.ContentProviders.TMDb);

                                                    BroadcastNotifier mBroadcaster = new BroadcastNotifier(activity);
                                                    mBroadcaster.broadcastIntentWithObject(Constants.STATE_INSERT_MOVIE, movie);
                                                    Toast.makeText(activity, getString(R.string.movie_added), Toast.LENGTH_SHORT).show();

                                                    ServiceManager.startImportDataService(activity.getApplicationContext());
                                                } else if (menuItem.getId() == 2) {
                                                    Movie movie = new Movie();
                                                    movie.setContentProvider(Constants.ContentProviders.TMDb.value);
                                                    movie.setOriginalName(m.getOriginalName());
                                                    movie.setSeen("0");
                                                    movie.setInsertDate(DateUtils.DateTimeNowString());
                                                    movie.setUpdateDate(DateUtils.DateTimeNowString());
                                                    DatabaseHelper db = DatabaseHelper.getInstance(activity.getApplicationContext());
                                                    long _id = db.addMovie(movie);
                                                    movie.setID((int) _id);

                                                    SearchResult sr = new SearchResult(String.valueOf(m.getTmdbNumber()), m.getOriginalName(), m.getPoster(), m.getYear());
                                                    ImportQueues.AddQueue(activity, _id, sr, Constants.ContentProviders.TMDb);

                                                    BroadcastNotifier mBroadcaster = new BroadcastNotifier(activity);
                                                    mBroadcaster.broadcastIntentWithObject(Constants.STATE_INSERT_MOVIE, movie);

                                                    ServiceManager.startImportDataService(activity.getApplicationContext());

                                                    //*******************************

                                                    AddToListDialog d = new AddToListDialog(activity);
                                                    d.setListType(1);
                                                    d.setMovie(movie);

                                                    d.showDialog();
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
                    } else {
                        mAdapter.addAllEnd((ArrayList<Movie>) m);
                        //mAdapter.notifyDataSetChanged();
                    }

                    progressBarMovieList.setVisibility(View.GONE);
                }

                @Override
                public void onNotCompleted(Throwable error, String content) {

                }
            });
            switch (mPageType) {
                case PopularMovies:
                    tmdb.getPopularList(locale, 0);
                    break;
                case NowPlayingMovies:
                    tmdb.getNowPlayingList(locale, 0);
                    break;
                case UpcomingMovies:
                    tmdb.getUpcomingList(locale, 0);
                    break;
            }


        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;
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
}
