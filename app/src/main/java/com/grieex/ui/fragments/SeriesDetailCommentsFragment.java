package com.grieex.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.adapter.TraktCommentsAdapter;
import com.grieex.core.TraktTv;
import com.grieex.core.listener.OnTraktTvEventListener;
import com.grieex.enums.TraktResult;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.NLog;
import com.grieex.helper.SampleRecycler;
import com.grieex.helper.TraktSettings;
import com.grieex.model.tables.Series;
import com.grieex.ui.TraktAuthActivity;
import com.grieex.ui.TraktCommentsActivity;
import com.grieex.widget.SimpleDividerItemDecoration;
import com.uwetrottmann.trakt5.entities.Comment;

import java.util.ArrayList;

public class SeriesDetailCommentsFragment extends Fragment {
    private static final String TAG = SeriesDetailCommentsFragment.class.getName();

    private static final String ARG_Series = "Series";
    private transient Series mSeries;
    private TraktCommentsAdapter commentsAdapter;
    private RecyclerView mRecyclerView;
    private Activity activity;
    private Button btnMoreComments;

    private ProgressBar progressBar;


    public static SeriesDetailCommentsFragment newInstance(Series series) {
        SeriesDetailCommentsFragment fragment = new SeriesDetailCommentsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_Series, series);
        fragment.setArguments(args);
        return fragment;
    }

    public SeriesDetailCommentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments() != null) {
                mSeries = (Series) getArguments().getSerializable(ARG_Series);
            }

            activity = getActivity();
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_series_detail_comments, container, false);
        try {
            mRecyclerView = v.findViewById(R.id.list);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(activity, R.drawable.divider2));
            mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            mRecyclerView.setAdapter(new SampleRecycler());

            progressBar = v.findViewById(R.id.progressBar);

            btnMoreComments = v.findViewById(R.id.btnMoreComments);
            btnMoreComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent it = new Intent(activity, TraktCommentsActivity.class);
                    it.putExtra(Constants.ID, mSeries.getTraktId());
                    it.putExtra(Constants.ActivityType, TraktCommentsActivity.ActivityTypes.List);
                    startActivity(it);
                }
            });

           getComments();
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;
    }

    public void Load(){
        if (mSeries == null)
            return;

        getComments();
    }

    private void getComments() {
        try {
            if (!Connectivity.isConnected(activity)) {
                return;
            }

            showProgress();

            TraktTv traktTv = new TraktTv();
            traktTv.setTraktEventListener(new OnTraktTvEventListener() {
                @Override
                public void onCompleted(Object m) {
                    if (m != null) {
                        ArrayList<Comment> comments = (ArrayList<Comment>) m;
                        mSeries.setComments(comments);
                        setCommentsAdapter();
                        btnMoreComments.setVisibility(View.VISIBLE);
                    }
                    hideProgress();
                }

            });
            traktTv.getComments(mSeries.getTraktId(), 0, 5);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void setCommentsAdapter() {
        try {
            if (mSeries.getComments() != null && mSeries.getComments().size() > 0) {
                commentsAdapter = new TraktCommentsAdapter(activity, mSeries.getComments());
                commentsAdapter.setOnItemClickListener(new TraktCommentsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View itemView, int position) {
                        Comment comment = commentsAdapter.getItem(position);
                        Intent it = new Intent(activity, TraktCommentsActivity.class);
                        it.putExtra(Constants.ID, comment.id);
                        it.putExtra(Constants.ActivityType, TraktCommentsActivity.ActivityTypes.ReplyList);
                        startActivity(it);
                    }
                });
                commentsAdapter.setOnItemLikeClickListener(new TraktCommentsAdapter.OnItemLikeClickListener() {
                    @Override
                    public void onItemLikeClick(View itemView, final int position) {
                        final Comment comment = commentsAdapter.getItem(position);
                        if (comment != null) {
                            TraktTv traktTv = new TraktTv();
                            traktTv.setTraktEventListener(new OnTraktTvEventListener() {
                                @Override
                                public void onCompleted(Object m) {
                                    if (m != null) {
                                        int result = (int) m;
                                        switch (result) {
                                            case TraktResult.SUCCESS:
                                                //mihmih
//                                            if (!comment.mylike)
//                                                comment.likes = comment.likes + 1;
//                                            else
//                                                comment.likes = comment.likes - 1;
//
//                                            comment.mylike = !comment.mylike;
                                                commentsAdapter.notifyItemChanged(position);
                                                break;
                                            case TraktResult.AUTH_ERROR:
                                                TraktSettings.logOut(activity);
                                                Intent login = new Intent(activity, TraktAuthActivity.class);
                                                startActivity(login);
                                                break;
                                            case TraktResult.ERROR:
                                                Toast.makeText(activity, R.string.crash_toast_text, Toast.LENGTH_LONG).show();
                                                break;
                                        }
                                    }
                                }
                            });
                            traktTv.likeComment(TraktSettings.getTraktAccessToken(activity), comment);
                        }
                    }
                });
                mRecyclerView.setAdapter(commentsAdapter);
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void showProgress() {
        btnMoreComments.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }


    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

}
