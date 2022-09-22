package com.grieex.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.grieex.R;
import com.grieex.core.Imdb;
import com.grieex.core.TmdbTv;
import com.grieex.core.TraktTv;
import com.grieex.core.listener.OnImdbEventListener;
import com.grieex.core.listener.OnTmdbTvEventListener;
import com.grieex.core.listener.OnTraktTvEventListener;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.DbUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Utils;
import com.grieex.model.tables.Movie;
import com.grieex.model.tables.Series;

import java.text.DecimalFormat;

public class SeriesDetailInfoFragment extends Fragment {
    private static final String TAG = SeriesDetailInfoFragment.class.getName();

    private static final String ARG_Series = "Series";
    private final View.OnClickListener linkClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                Button btn = (Button) v;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(btn.getText().toString()));
                startActivity(browserIntent);
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }
    };
    private Series mSeries;
    private TextView tvOverview;
    private TextView tvGenres;
    private TextView tvDateInfo;
    private TextView tvRuntime;
    private TextView tvStatus;
    private TextView tvTraktRating;
    private TextView tvImdbUserRating;
    private TextView tvTmdbUserRating;
    private LinearLayout llTraktRating;
    private LinearLayout llImdbRating;
    private LinearLayout llTmdbRating;
    private LinearLayout llContent;
    private LinearLayout llLinks;
    private Button btnTrakt;
    private Button btnTvdb;
    private Button btnImdb;
    private Activity activity;
    private DatabaseHelper dbHelper;

    public SeriesDetailInfoFragment() {

    }

    public static SeriesDetailInfoFragment newInstance(Series series) {
        SeriesDetailInfoFragment fragment = new SeriesDetailInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_Series, series);
        fragment.setArguments(args);
        return fragment;
    }

    public void setSeries(Series series) {
        mSeries = series;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments() != null) {
                mSeries = (Series) getArguments().getSerializable(ARG_Series);
            }
            activity = getActivity();
            dbHelper = DatabaseHelper.getInstance(activity);

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_series_detail_info, container, false);
        try {
            tvOverview = v.findViewById(R.id.tvOverview);
            tvGenres = v.findViewById(R.id.tvGenres);
            tvDateInfo = v.findViewById(R.id.tvDateInfo);
            tvRuntime = v.findViewById(R.id.tvRuntime);
            tvTraktRating = v.findViewById(R.id.tvTraktRating);
            tvImdbUserRating = v.findViewById(R.id.tvImdbUserRating);
            tvTmdbUserRating = v.findViewById(R.id.tvTmdbUserRating);
            tvStatus = v.findViewById(R.id.tvStatus);
            llImdbRating = v.findViewById(R.id.llImdbRating);
            llTmdbRating = v.findViewById(R.id.llTmdbRating);
            llTraktRating = v.findViewById(R.id.llTraktRating);
            llContent = v.findViewById(R.id.llContent);
            llLinks = v.findViewById(R.id.llLinks);
            btnTrakt = v.findViewById(R.id.btnTrakt);
            btnTvdb = v.findViewById(R.id.btnTvdb);
            btnImdb = v.findViewById(R.id.btnImdb);


            llImdbRating.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/title/" + mSeries.getImdbId()));
                    startActivity(browserIntent);
                }
            });

            llTmdbRating.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themoviedb.org/tv/" + mSeries.getTmdbId()));
                    startActivity(browserIntent);
                }
            });

            llTraktRating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://trakt.tv/shows/" + mSeries.getTraktId()));
                    startActivity(browserIntent);
                }
            });

            Load();
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;
    }

    private void Load() {
        try {
            if (!isAdded() || mSeries == null)
                return;

            llContent.setVisibility(View.VISIBLE);

            tvOverview.setText(mSeries.getOverview());
            tvGenres.setText(mSeries.getGenres());
            tvRuntime.setText(String.format(getString(R.string.minute), mSeries.getRuntime()));

            if (mSeries.getStatus().equals("0")) {
                tvStatus.setText(getString(R.string.ended));
            } else {
                tvStatus.setText(getString(R.string.continuing));
            }


            long nextAirDateMillis = DbUtils.getLastEpisodeMs(activity, mSeries.getID());
            if (nextAirDateMillis != -1) {
                tvDateInfo.setText(String.format("%s | %s | %s", DateUtils.getDateFormat(nextAirDateMillis, Constants.DATE_FORMAT11), DateUtils.getDateFormat(nextAirDateMillis, Constants.DATE_FORMAT10), mSeries.getNetwork()));
            } else {
                tvDateInfo.setText(String.format("%s | %s | %s", mSeries.getAirDay(), mSeries.getAirTime(), mSeries.getNetwork()));
            }


            if (mSeries.getTraktId() != 0) {
                llLinks.setVisibility(View.VISIBLE);
                btnTrakt.setVisibility(View.VISIBLE);
                btnTrakt.setText("http://trakt.tv/shows/" + mSeries.getTraktId());
                btnTrakt.setOnClickListener(linkClicked);
            }

            if (mSeries.getTvdbId() != 0) {
                llLinks.setVisibility(View.VISIBLE);
                btnTvdb.setVisibility(View.VISIBLE);
                btnTvdb.setText("http://thetvdb.com/?id=" + mSeries.getTvdbId() + "&tab=series");
                btnTvdb.setOnClickListener(linkClicked);
            }

            if (!TextUtils.isEmpty(mSeries.getImdbId())) {
                llLinks.setVisibility(View.VISIBLE);
                btnImdb.setVisibility(View.VISIBLE);
                btnImdb.setText("http://www.imdb.com/title/" + mSeries.getImdbId());
                btnImdb.setOnClickListener(linkClicked);
            }

            setImdRating();
            setTmdbRating();
            setTraktRating();

            getImdbRating();
            getTmdbRating();
            getTraktRating();

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void setImdRating() {
        if (!TextUtils.isEmpty(mSeries.getImdbUserRating()) & !TextUtils.isEmpty(mSeries.getImdbVotes())) {
            if (mSeries.getImdbVotes() != null && mSeries.getImdbVotes().equals("0")) {
                tvImdbUserRating.setText(mSeries.getImdbUserRating());
            } else {
                String strImdbVotes = new DecimalFormat("###,###").format(Utils.parseInt(mSeries.getImdbVotes()));
                tvImdbUserRating.setText(mSeries.getImdbUserRating() + " / " + strImdbVotes.replace(",", "."));
            }
            llImdbRating.setVisibility(View.VISIBLE);
            //new SlideInAnimation(llImdbRating).setDirection(Animation.DIRECTION_LEFT).animate();
        } else {
            llImdbRating.setVisibility(View.GONE);
        }
    }

    private void setTraktRating() {
        if (mSeries.getRating() > 0 & mSeries.getVotes() > 0) {
            String strRating = new DecimalFormat("##.#").format(mSeries.getRating());
            String strVotes = new DecimalFormat("###,###").format(mSeries.getVotes());
            tvTraktRating.setText(strRating + " / " + strVotes.replace(",", "."));
            llTraktRating.setVisibility(View.VISIBLE);
            //new SlideInAnimation(llTraktRating).setDirection(Animation.DIRECTION_LEFT).animate();
        } else {
            llTraktRating.setVisibility(View.GONE);
        }
    }

    private void setTmdbRating() {
        if (!TextUtils.isEmpty(mSeries.getTmdbUserRating()) & !TextUtils.isEmpty(mSeries.getTmdbVotes())) {
            if (mSeries.getTmdbVotes().equals("0")) {
                tvTmdbUserRating.setText(mSeries.getTmdbUserRating());
                llTmdbRating.setVisibility(View.GONE);
            } else {
                String strTmdbVotes = new DecimalFormat("###,###").format(Utils.parseInt(mSeries.getTmdbVotes()));
                tvTmdbUserRating.setText(mSeries.getTmdbUserRating() + " / " + strTmdbVotes.replace(",", "."));
                llTmdbRating.setVisibility(View.VISIBLE);
                //new SlideInAnimation(llTmdbRating).setDirection(Animation.DIRECTION_LEFT).animate();
            }
        } else {
            llTmdbRating.setVisibility(View.GONE);
        }
    }

    private void getTraktRating() {
        if (!Connectivity.isConnected(activity))
            return;

        if (mSeries.getTraktId() < 0)
            return;

        TraktTv traktTv = new TraktTv();
        traktTv.setTraktEventListener(new OnTraktTvEventListener() {
            @Override
            public void onCompleted(Object m) {
                try {
                    if (m != null) {
                        Series ss = (Series) m;
                        mSeries.setRating(ss.getRating());
                        mSeries.setVotes(ss.getVotes());

                        dbHelper.updateSeries(mSeries);

                        setTraktRating();
                    } else {
                        //hideProgress();
                    }
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
            }
        });
        traktTv.ParseRatingAsync(String.valueOf(mSeries.getTraktId()), GrieeXSettings.getLocale(activity));

    }


    private void getImdbRating() {
        if (!Connectivity.isConnected(activity))
            return;

        if (TextUtils.isEmpty(mSeries.getImdbId()))
            return;

        Imdb imdb = new Imdb();
        imdb.setImdbEventListener(new OnImdbEventListener() {
            @Override
            public void onNotCompleted(Throwable error, String content) {

            }

            @Override
            public void onCompleted(Object m) {
                try {
                    Movie p = (Movie) m;
                    mSeries.setImdbUserRating(p.getUserRating());
                    mSeries.setImdbVotes(p.getVotes());


                    dbHelper.updateSeries(mSeries);

                    setImdRating();
                } catch (Exception e) {
                    //NLog.e(TAG, e);
                }
            }
        });
        imdb.ParseRatingAsync(mSeries.getImdbId());
    }

    private void getTmdbRating() {
        if (!Connectivity.isConnected(activity))
            return;

        if (mSeries.getTmdbId() < 0 && TextUtils.isEmpty(mSeries.getImdbId()))
            return;

        TmdbTv tmdb = new TmdbTv();
        tmdb.setTmdbTvEventListener(new OnTmdbTvEventListener() {
            @Override
            public void onNotCompleted(Throwable error, String content) {

            }

            @Override
            public void onCompleted(Object m) {
                try {
                    Movie p = (Movie) m;
                    mSeries.setTmdbUserRating(p.getUserRating());
                    mSeries.setTmdbVotes(p.getVotes());

                    dbHelper.updateSeries(mSeries);

                    setTmdbRating();
                } catch (Exception e) {
                    // NLog.e(TAG, e);
                }

            }
        });

        if (mSeries.getTmdbId() > 0)
            tmdb.ParseRatingWithTmdbNumberAsync(Utils.parseString(mSeries.getTmdbId()));
        else
            tmdb.ParseRatingWithImdbNumberAsync(mSeries.getImdbId());
    }
}
