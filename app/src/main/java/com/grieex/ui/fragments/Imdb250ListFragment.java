package com.grieex.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grieex.GrieeX;
import com.grieex.R;
import com.grieex.adapter.Imdb250Adapter;
import com.grieex.core.listener.OnImdb250EventListener;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.SampleRecycler;
import com.grieex.helper.Utils;
import com.grieex.model.tables.Imdb250;
import com.grieex.model.tables.Movie;
import com.grieex.model.tables.Series;
import com.grieex.ui.FullScreenImageActivity;
import com.grieex.ui.MovieDetailActivity;
import com.grieex.ui.SeriesDetailActivity;
import com.grieex.widget.SimpleDividerItemDecoration;

import java.util.ArrayList;

public class Imdb250ListFragment extends Fragment {
    private static final String TAG = Imdb250ListFragment.class.getName();

    private Activity activity;
    private Imdb250Adapter mAdapter;
    private ProgressBar progressBarMovieList;
    private RecyclerView mRecyclerView;

    private boolean firstRun = true;

    private TextView tvNoRecord;

    private volatile String RunKey;

    private DatabaseHelper dbHelper;

    private Constants.Imdb250Type iImdb250Type;

    private static final String ARG_Imdb250Type = "Imdb250Type";

    private ImportServiceBroadcastReceiver mImportServiceBroadcastReceiver;

    public static Imdb250ListFragment newInstance(Constants.Imdb250Type imdb250Type) {
        Imdb250ListFragment fragment = new Imdb250ListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_Imdb250Type, imdb250Type.value);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        //
        if (GrieeXSettings.RELEASE_MODE) {
            GrieeX.getInstance().trackScreenView(this.getClass().getName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        try {
            activity = getActivity();
            dbHelper = DatabaseHelper.getInstance(activity);

            if (getArguments() != null) {
                iImdb250Type = Constants.Imdb250Type.fromValue(getArguments().getInt(ARG_Imdb250Type));
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

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.imdb250list, container, false);
        try {
            mRecyclerView = v.findViewById(R.id.list);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity(), R.drawable.divider2));
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(new SampleRecycler());

            progressBarMovieList = v.findViewById(R.id.progressBarMovieList);
            progressBarMovieList.setVisibility(View.VISIBLE);
            tvNoRecord = v.findViewById(R.id.tvNoRecord);

            RunKey = Utils.getRandomString();
            new MoviesLoader().execute(RunKey);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.imdb250_actionbar_menu, menu);
        final MenuItem menuRefresh = menu.findItem(R.id.action_refresh);
        menuRefresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                getImdb250();
                return false;
            }
        });

    }

    private void getImdb250() {
        progressBarMovieList.setVisibility(View.VISIBLE);

        com.grieex.core.Imdb250 i = new com.grieex.core.Imdb250(getActivity());
        i.setImdb250EventListener(new OnImdb250EventListener() {
            @Override
            public void onCompleted() {
                ResetData();
                progressBarMovieList.setVisibility(View.GONE);
                RunKey = Utils.getRandomString();
                new MoviesLoader().execute(RunKey);
            }

            @Override
            public void onNotCompleted() {
                progressBarMovieList.setVisibility(View.GONE);
            }
        });

        if (iImdb250Type == Constants.Imdb250Type.Movie)
            i.getMovies();
        else
            i.getTvShows();
    }

    private void ResetData() {
        try {
            if (mAdapter != null)
                mAdapter = null;
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void showMovie(Movie m, boolean isExistDatabase) {
        try {
            if (!isExistDatabase && !Connectivity.isConnected(activity)) {
                Toast.makeText(activity, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                return;
            }

            Intent it = new Intent(getActivity(), MovieDetailActivity.class);
            it.putExtra(Constants.Movie, m);
            startActivity(it);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void showSeries(String ImdbID, boolean isExistDatabase) {
        try {
            if (!isExistDatabase && !Connectivity.isConnected(activity)) {
                Toast.makeText(activity, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                return;
            }

            Intent it = new Intent(getActivity(), SeriesDetailActivity.class);
            it.putExtra(Constants.ImdbOrTraktID, ImdbID);
            it.putExtra(Constants.IsExistDatabase, isExistDatabase);
            startActivity(it);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }


    class MoviesLoader extends AsyncTask<String, Void, ArrayList<Imdb250>> {
        private volatile boolean running = true;
        private volatile String _RunKey;

        @Override
        protected void onCancelled() {
            running = false;
        }

        @Override
        protected ArrayList<Imdb250> doInBackground(String... params) {
            _RunKey = params[0];

            ArrayList<Imdb250> MovieList = new ArrayList<>();

            if (!RunKey.equals(_RunKey) | !running)
                return MovieList;

            Cursor cursor = null;
            try {
                String strSQL;

                if (iImdb250Type == Constants.Imdb250Type.Movie)
                    strSQL = "Select *,(Select Count(*) From Movies Where Movies.ImdbNumber=Imdb250.ImdbNumber) as MovieCount FROM Imdb250 Where Type=" + Constants.Imdb250Type.Movie.value;
                else
                    strSQL = "Select *,(Select Count(*) From Series Where Series.ImdbId=Imdb250.ImdbNumber) as MovieCount FROM Imdb250 Where Type=" + Constants.Imdb250Type.Series.value;

                String strOrderBy = " Order By Rank ASC ";

                cursor = dbHelper.GetCursor(strSQL + strOrderBy);

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        Imdb250 m = new Imdb250();
                        m.LoadWithCursorRow(cursor);
                        m.setIsExisting(!cursor.getString(cursor.getColumnIndex("MovieCount")).equals("0"));

                        MovieList.add(m);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                NLog.e(TAG, e);
            } finally {
                if (cursor != null)
                    cursor.close();
            }

            return MovieList;
        }

        @Override
        protected void onPreExecute() {
            tvNoRecord.setVisibility(View.GONE);
            progressBarMovieList.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList<Imdb250> res) {
            if (!isAdded() | !RunKey.equals(_RunKey) | !running) {
                return;
            }
            try {
                if (mAdapter == null) {
                    mAdapter = new Imdb250Adapter(res);
                    mRecyclerView.setAdapter(mAdapter);

                    mAdapter.setOnItemClickListener(new Imdb250Adapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View itemView, int position) {
                            try {
                                //boolean isExistDatabase;
                                if (iImdb250Type == Constants.Imdb250Type.Movie) {
                                    Imdb250 item = mAdapter.getItem(position);
                                    Movie m = new Movie();

                                    m.setImdbNumber(item.getImdbNumber());
                                    // showMovie(m, item.getIsExisting());
                                    showMovie(m, false);
                                } else {
                                    Imdb250 m = mAdapter.getItem(position);

                                    showSeries(m.getImdbNumber(), m.getIsExisting());

//                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/title/" + m.getImdbNumber()));
//                                    startActivity(browserIntent);
                                }
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }
                    });

                    mAdapter.setOnImageViewClickListener(new Imdb250Adapter.OnImageViewClickListener() {
                        @Override
                        public void onImageViewClick(View itemView, int position) {
                            Imdb250 item = mAdapter.getItem(position);
                            Intent it = new Intent(activity, FullScreenImageActivity.class);
                            it.putExtra(Constants.ImageLink, item.getImageLink());
                            activity.startActivity(it);
                        }
                    });


                    if (mAdapter.getItemCount() > 0) {
                        tvNoRecord.setVisibility(View.GONE);
                        progressBarMovieList.setVisibility(View.GONE);
                    } else {
                        if (firstRun) {
                            getImdb250();

                            firstRun = false;
                        }
                    }

                } else {
                    if (res != null) {
                        mAdapter.addAllEnd(res);
                        progressBarMovieList.setVisibility(View.GONE);
                    }
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
                        Object o = intent.getExtras().getSerializable(Constants.EXTENDED_DATA_OBJECT);
                        if (o != null && o instanceof Series) {
                            Series s = (Series) o;

                            int position = mAdapter.getPositionFromImdbId(s.getImdbId());
                            if (position > -1) {
                                Imdb250 item = mAdapter.getItem(position);
                                item.setIsExisting(true);
                                mAdapter.notifyItemChanged(position);
                            }

                        }
                        break;
                    }
                    case Constants.STATE_DELETE_SERIES: {
                        Object o = intent.getExtras().getSerializable(Constants.EXTENDED_DATA_OBJECT);
                        if (o != null && o instanceof Series) {
                            Series s = (Series) o;

                            int position = mAdapter.getPositionFromImdbId(s.getImdbId());
                            if (position > -1) {
                                Imdb250 item = mAdapter.getItem(position);
                                item.setIsExisting(false);
                                mAdapter.notifyItemChanged(position);
                            }

                        }
                        break;
                    }
                    case Constants.STATE_INSERT_MOVIE: {
                        Object o = intent.getExtras().getSerializable(Constants.EXTENDED_DATA_OBJECT);
                        if (o != null && o instanceof Movie) {
                            Movie s = (Movie) o;

                            int position = mAdapter.getPositionFromImdbId(s.getImdbNumber());
                            if (position > -1) {
                                Imdb250 item = mAdapter.getItem(position);
                                item.setIsExisting(true);
                                mAdapter.notifyItemChanged(position);
                            }

                        }
                        break;
                    }
                    case Constants.STATE_DELETE_MOVIE: {
                        Object o = intent.getExtras().getSerializable(Constants.EXTENDED_DATA_OBJECT);
                        if (o != null && o instanceof Movie) {
                            Movie s = (Movie) o;

                            int position = mAdapter.getPositionFromImdbId(s.getImdbNumber());
                            if (position > -1) {
                                Imdb250 item = mAdapter.getItem(position);
                                item.setIsExisting(false);
                                mAdapter.notifyItemChanged(position);
                            }

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
