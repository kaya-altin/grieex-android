package com.grieex.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.grieex.R;
import com.grieex.helper.DbUtils;
import com.grieex.helper.NLog;
import com.grieex.helper.Utils;
import com.grieex.model.tables.Episode;
import com.grieex.widget.AspectRatioImageView;

public class EpisodeDetailFragment extends DialogFragment {
    private static final String TAG = EpisodeDetailFragment.class.getName();

    private static final String ARG_EPISODE = "episode";
    private static final String ARG_IS_EXIST_DATABASE = "isExistDatabase";
    private int EpisodeID;
    private Episode mEpisode;

    private CheckBox chkWatched;
    private CheckBox chkCollected;
    private CheckBox chkFavorite;

    // private boolean isExistDatabase = false;

    private boolean IsChanged = false;

    private OnCustomEventListener mListener;

    public static EpisodeDetailFragment newInstance(Episode episode) {
        EpisodeDetailFragment fragment = new EpisodeDetailFragment();
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        Bundle args = new Bundle();
        args.putSerializable(ARG_EPISODE, episode);
        fragment.setArguments(args);
        return fragment;
    }

    public void setCustomEventListener(OnCustomEventListener eventListener) {
        mListener = eventListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments() != null) {
                mEpisode = (Episode) getArguments().getSerializable(ARG_EPISODE);
                //isExistDatabase = getArguments().getBoolean(ARG_IS_EXIST_DATABASE,false);
//                mEpisode = new Episode();
//                mEpisode.LoadWithWhereColumn(getActivity(), Episode.COLUMNS._ID, String.valueOf(EpisodeID));
            }

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_episode_detail, container, false);
        try {
            AspectRatioImageView ivPoster = v.findViewById(R.id.ivPoster);
            TextView tvOverview = v.findViewById(R.id.tvOverview);
            TextView tvEpisodeName = v.findViewById(R.id.tvEpisodeName);
            chkWatched = v.findViewById(R.id.chkWatched);
            chkCollected = v.findViewById(R.id.chkCollected);
            chkFavorite = v.findViewById(R.id.chkFavorite);

            if (mEpisode.getID() > 0) {
                chkWatched.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IsChanged = true;
                        DbUtils.SetEpisodeWatched(getActivity(), mEpisode.getID(), chkWatched.isChecked());
                        mEpisode.setWatched(Utils.parseBooleanToInt(chkWatched.isChecked()));
                    }
                });

                chkCollected.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IsChanged = true;
                        DbUtils.SetEpisodeCollected(getActivity(), mEpisode.getID(), chkCollected.isChecked());
                        mEpisode.setCollected(Utils.parseBooleanToInt(chkCollected.isChecked()));
                    }
                });

                chkFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IsChanged = true;
                        DbUtils.SetEpisodeFavorite(getActivity(), mEpisode.getID(), chkFavorite.isChecked());
                        mEpisode.setFavorite(Utils.parseBooleanToInt(chkFavorite.isChecked()));
                    }
                });

                chkCollected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            chkCollected.setText(getString(R.string.collected));
                        else
                            chkCollected.setText(getString(R.string.uncollected));
                    }
                });

                chkFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            chkFavorite.setText(getString(R.string.favorite));
                        else
                            chkFavorite.setText(getString(R.string.unfavorite));
                    }
                });

                chkWatched.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            chkWatched.setText(getString(R.string.watched));
                        else
                            chkWatched.setText(getString(R.string.unwatched));
                    }
                });

                chkWatched.setChecked(Utils.parseBoolean(mEpisode.getWatched()));
                chkCollected.setChecked(Utils.parseBoolean(mEpisode.getCollected()));
                chkFavorite.setChecked(Utils.parseBoolean(mEpisode.getFavorite()));
            } else {
                chkWatched.setVisibility(View.GONE);
                chkCollected.setVisibility(View.GONE);
                chkFavorite.setVisibility(View.GONE);
            }


            Glide.with(EpisodeDetailFragment.this)
                    .load(mEpisode.getEpisodeImage())
                    .into(ivPoster);

            if (!TextUtils.isEmpty(mEpisode.getOverview()))
                tvOverview.setText(mEpisode.getOverview());

            if (!TextUtils.isEmpty(mEpisode.getEpisodeName()))
                tvEpisodeName.setText(mEpisode.getEpisodeName());
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null)
            mListener.onDismiss(mEpisode);
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    public interface OnCustomEventListener {
        void onDismiss(Episode episode);
    }

}
