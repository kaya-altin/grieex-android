package com.grieex.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.adapter.MovieFilesAdapter;
import com.grieex.helper.DateUtils;
import com.grieex.helper.NLog;
import com.grieex.helper.SampleRecycler;
import com.grieex.helper.Utils;
import com.grieex.model.FileItem;
import com.grieex.model.tables.File;
import com.grieex.model.tables.Movie;
import com.grieex.widget.SimpleDividerItemDecoration;

import java.util.ArrayList;

public class MovieDetailFilesFragment extends Fragment {
    private static final String TAG = MovieDetailFilesFragment.class.getName();
    private static final String ARG_Movie = "Movie";

    private Movie mMovie;
    private RecyclerView mRecyclerView;


    public static MovieDetailFilesFragment newInstance(Movie Movie) {
        MovieDetailFilesFragment fragment = new MovieDetailFilesFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_Movie, Movie);
        fragment.setArguments(args);
        return fragment;
    }

    public MovieDetailFilesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovie = (Movie) getArguments().getSerializable(ARG_Movie);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_detail_files, container, false);
        try {
            mRecyclerView = v.findViewById(R.id.list);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity(), R.drawable.divider2));
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(new SampleRecycler());


            prepareListData();


        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    private void prepareListData() {
        try {
            ArrayList<File> files = mMovie.getFiles();
            ArrayList<FileItem> fileItems = new ArrayList<>();

            for (int i = 0; i < files.size(); i++) {
                try {
                    File f = files.get(i);
                    fileItems.add(new FileItem(f.getFileName(), true));

                    fileItems.add(new FileItem("------- " + getString(R.string.video) + " -------"));
                    fileItems.add(new FileItem(getString(R.string.screen_resolution) + ":  " + f.getResolution()));
                    fileItems.add(new FileItem(getString(R.string.video_codec) + ":  " + f.getVideoCodec()));
                    fileItems.add(new FileItem(getString(R.string.bitrate) + ":  " + f.getVideoBitrate()));
                    fileItems.add(new FileItem(getString(R.string.fps) + ":  " + f.getFps()));
                    fileItems.add(new FileItem(getString(R.string.video_aspect_ratio) + ":  " + f.getVideoAspectRatio()));
                    fileItems.add(new FileItem(getString(R.string.runningtime) + ":  " + DateUtils.getDuration(Utils.parseLong(f.getLenght()))));
                    fileItems.add(new FileItem(getString(R.string.total_frames) + ":  " + f.getTotalFrames()));
                    fileItems.add(new FileItem(getString(R.string.video_size) + ":  " + Utils.getFileSize(Utils.parseLong(f.getVideoSize()))));
                    fileItems.add(new FileItem(getString(R.string.file_size) + ":  " + Utils.getFileSize(Utils.parseLong(f.getFileSize()))));

                    if (!TextUtils.isEmpty(f.getAudioCodec1())) {
                        fileItems.add(new FileItem(""));
                        fileItems.add(new FileItem("------- " + getString(R.string.audio) + "1 -------"));
                        fileItems.add(new FileItem(getString(R.string.audio_codec) + ":  " + f.getAudioCodec1()));
                        fileItems.add(new FileItem(getString(R.string.channels) + ":  " + f.getAudioChannels1()));
                        fileItems.add(new FileItem(getString(R.string.bitrate) + ":  " + f.getAudioBitrate1()));
                        fileItems.add(new FileItem(getString(R.string.sample_rate) + ":  " + f.getAudioSampleRate1()));
                        fileItems.add(new FileItem(getString(R.string.audio_size) + ":  " + Utils.getFileSize(Utils.parseLong(f.getAudioSize1()))));
                    }

                    if (!TextUtils.isEmpty(f.getAudioCodec2())) {
                        fileItems.add(new FileItem(""));
                        fileItems.add(new FileItem("------- " + getString(R.string.audio) + "2 -------"));
                        fileItems.add(new FileItem(getString(R.string.audio_codec) + ":  " + f.getAudioCodec2()));
                        fileItems.add(new FileItem(getString(R.string.channels) + ":  " + f.getAudioChannels2()));
                        fileItems.add(new FileItem(getString(R.string.bitrate) + ":  " + f.getAudioBitrate2()));
                        fileItems.add(new FileItem(getString(R.string.sample_rate) + ":  " + f.getAudioSampleRate2()));
                        fileItems.add(new FileItem(getString(R.string.audio_size) + ":  " + Utils.getFileSize(Utils.parseLong(f.getAudioSize2()))));
                    }

                } catch (Exception e) {
                }
            }

            MovieFilesAdapter mAdapter = new MovieFilesAdapter(fileItems);
            mRecyclerView.setAdapter(mAdapter);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }
}
