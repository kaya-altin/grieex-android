package com.grieex.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.adapter.CastListAdapter;
import com.grieex.core.Tmdb;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.helper.SampleRecycler;
import com.grieex.core.listener.OnTmdbEventListener;
import com.grieex.model.tables.Cast;
import com.grieex.model.tables.Movie;
import com.grieex.ui.FullScreenImageActivity;
import com.grieex.widget.SimpleDividerItemDecoration;

import java.util.ArrayList;

public class MovieDetailCastFragment extends Fragment {
    private static final String TAG = MovieDetailCastFragment.class.getName();

    private static final String ARG_Movie = "Movie";
    private Movie mMovie;
    private CastListAdapter mAdapter;
    private RecyclerView mRecyclerView;


    public static MovieDetailCastFragment newInstance(Movie movie) {
        MovieDetailCastFragment fragment = new MovieDetailCastFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_Movie, movie);
        fragment.setArguments(args);
        return fragment;
    }

    public MovieDetailCastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments() != null) {
                mMovie = (Movie) getArguments().getSerializable(ARG_Movie);
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_detail_cast, container, false);
        try {
            mRecyclerView = v.findViewById(R.id.list);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity(), R.drawable.divider2));
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(new SampleRecycler());

            Load();
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;
    }

    private void Load() {
        try {
//			DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
//			ArrayList<Cast> casts = db.getCast(mMovie.getID());

            if (mMovie.getCast() != null && mMovie.getCast().size() > 0) {
                ArrayList<Cast> casts = mMovie.getCast();
                mAdapter = new CastListAdapter(casts);
                mRecyclerView.setAdapter(mAdapter);

                mAdapter.setOnItemClickListener(new CastListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View itemView, int position) {
                        Cast cast = mAdapter.getItem(position);
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(cast.getUrl()));
                        startActivity(browserIntent);
                    }
                });

                mAdapter.setOnImageViewClickListener(new CastListAdapter.OnImageViewClickListener() {
                    @Override
                    public void onImageViewClick(View itemView, int position) {
                        Cast cast = mAdapter.getItem(position);
                        String poster = cast.getImageUrl();
                        if (cast.getImageUrl().lastIndexOf("._") > 0)
                            poster = poster.substring(0, poster.lastIndexOf("._")) + ".jpg";

                        Intent it = new Intent(getActivity(), FullScreenImageActivity.class);
                        it.putExtra(Constants.ImageLink, poster);
                        getActivity().startActivity(it);
                    }
                });
            } else {
                if (!Connectivity.isConnected(getActivity()))
                    return;

                getCasts();
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void getCasts() {
        try {
            if (!Connectivity.isConnected(getActivity()))
                return;

            Tmdb tmdb = new Tmdb();
            tmdb.setTmdbEventListener(new OnTmdbEventListener() {
                @Override
                public void onCompleted(Object m) {
                    try {
                        ArrayList<Cast> casts = (ArrayList<Cast>) m;
                        if (casts != null && casts.size() > 0) {
                            mMovie.setCast(casts);
                            DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
                            db.fillCast(casts, mMovie.getID(), Constants.CollectionType.Movie);
                            Load();
                        }
                    } catch (Exception e) {
                        NLog.e(TAG, e);
                    }
                }

                @Override
                public void onNotCompleted(Throwable error, String content) {

                }
            });
            tmdb.getCasts(mMovie.getImdbNumber());
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }


}
