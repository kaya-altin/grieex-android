package com.grieex.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.adapter.UpcomingSeriesAdapter;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.NLog;
import com.grieex.helper.SampleRecycler;
import com.grieex.helper.Utils;
import com.grieex.model.Page;
import com.grieex.model.tables.Episode;
import com.grieex.model.tables.Series;
import com.grieex.ui.FullScreenImageActivity;
import com.grieex.ui.SeriesDetailActivity;
import com.grieex.widget.SimpleDividerItemDecoration;

import java.util.ArrayList;

public class UpcomingSeriesFragment extends Fragment {
    private static final String TAG = UpcomingSeriesFragment.class.getName();
    private static final String ARG_PageTypes = "ARG_PageTypes";
    private static final int iRecordShow = 100;
    private static final int iFirstLoadRowCount = 20;
    private final int iListViewType = 0;
    private Activity activity;
    private UpcomingSeriesAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar progress;
    private int iRecordCount = 0;
    private int iRecordPage = 0;
    private boolean bIsFirstLoaded = true;
    private TextView tvNoRecord;
    private volatile String RunKey;
    private DatabaseHelper dbHelper;

    public static UpcomingSeriesFragment newInstance() {
        return new UpcomingSeriesFragment();
    }

    public static UpcomingSeriesFragment newInstance(Page.PageTypes pageTypes) {
        UpcomingSeriesFragment fragment = new UpcomingSeriesFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PageTypes, pageTypes);
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
            //iListViewType = Prefs.with(activity).getInt(Constants.ListViewType, 0);

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.upcoming_series, container, false);
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

            progress = v.findViewById(R.id.progress);
            tvNoRecord = v.findViewById(R.id.tvNoRecord);

            RunKey = Utils.getRandomString();
            new DataLoader(activity).execute(RunKey);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;
    }

    private void showSeries(long SeriesID, View view) {
        try {
            Intent it = new Intent(getActivity(), SeriesDetailActivity.class);
            it.putExtra(Constants.SeriesID, String.valueOf(SeriesID));
            startActivity(it);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    class DataLoader extends AsyncTask<String, Void, ArrayList<Series>> {
        private Activity activity;
        private volatile boolean running = true;
        private volatile String _RunKey;

        public DataLoader(Activity _activity) {
            activity = _activity;
        }

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
                String strSQL = "Select Series._id, Series.SeriesName, Series.Poster, Series.Network, Episodes.EpisodeName, Episodes.SeasonNumber, Episodes.EpisodeNumber, Episodes.FirstAiredMs FROM Series ";
                String strJoin = " INNER JOIN Episodes ON (Series._id = Episodes.SeriesId) ";
                String strWhere = " Where FirstAiredMs > " + DateUtils.DateTimeNow().getTime();
                String strGroup = "";
                String strLimit = " Limit " + (iRecordPage * iRecordShow) + "," + iRecordShow;
                String strOrderBy = " Order By FirstAiredMs asc ";

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
                int idxFirstAiredMs = cursor.getColumnIndex(Episode.COLUMNS.FirstAiredMs);
                int idxEpisodeName = cursor.getColumnIndex(Episode.COLUMNS.EpisodeName);
                int idxSeasonNumber = cursor.getColumnIndex(Episode.COLUMNS.SeasonNumber);
                int idxEpisodeNumber = cursor.getColumnIndex(Episode.COLUMNS.EpisodeNumber);

                if (cursor.moveToFirst()) {
                    do {
                        Series m = new Series();
                        m.setID(cursor.getInt(idxID));
                        m.setSeriesName(cursor.getString(idxSeriesName));
                        m.setPoster(cursor.getString(idxPoster));
                        m.setNetwork(cursor.getString(idxNetwork));

                        String episodeName = cursor.getString(idxEpisodeName);
                        String seasonNumber = String.format("%02d", cursor.getInt(idxSeasonNumber));
                        String episodeNumber = String.format("%02d", cursor.getInt(idxEpisodeNumber));

                        m.setEpisodeName(String.format("S%sxE%s: %s", seasonNumber, episodeNumber, episodeName));

                        long nextAirDateMillis = cursor.getLong(idxFirstAiredMs);
                        m.setDateInfo(DateUtils.getDateFormat(nextAirDateMillis, Constants.DATE_FORMAT12));

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
                    mAdapter = new UpcomingSeriesAdapter(res, activity);
                    mAdapter.setViewType(iListViewType);
                    mRecyclerView.setAdapter(mAdapter);
                    mAdapter.setOnItemClickListener(new UpcomingSeriesAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View itemView, int position) {
                            long SeriesID = mAdapter.getItemId(position);
                            showSeries(SeriesID, itemView);
                        }
                    });


                    mAdapter.setOnImageViewClickListener(new UpcomingSeriesAdapter.OnImageViewClickListener() {
                        @Override
                        public void onImageViewClick(View itemView, int position) {
                            Series series = mAdapter.getItem(position);
                            Intent it = new Intent(activity, FullScreenImageActivity.class);
                            it.putExtra(Constants.ImageLink, series.getPoster());
                            activity.startActivity(it);
                        }
                    });

                    if (mAdapter.getItemCount() > 0) {
                        if (bIsFirstLoaded) {
                            bIsFirstLoaded = false;
                        }

                        tvNoRecord.setVisibility(View.GONE);

                        if (mAdapter.getItemCount() == iFirstLoadRowCount) {
                            new DataLoader(activity).execute(_RunKey);
                        }

                    } else {
                        tvNoRecord.setVisibility(View.VISIBLE);
                    }

                } else {
                    if (res != null) {
                        mAdapter.addAllEnd(res);
                        // mAdapter.notifyDataSetChanged();

                        if (mAdapter.getItemCount() < iRecordCount) {
                            if (iRecordCount >= iRecordShow)
                                iRecordPage++;

                            new DataLoader(activity).execute(_RunKey);
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


}
