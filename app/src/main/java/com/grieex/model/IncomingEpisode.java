package com.grieex.model;

import java.io.Serializable;

/**
 * Created by Griee on 23.01.2016.
 */
public class IncomingEpisode implements Serializable{
    private int mId;
    private String mSeriesName;
    private String mEpisodeName;
    private Long mFirstAiredMs;
    private String mPoster;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getSeriesName() {
        return mSeriesName;
    }

    public void setSeriesName(String seriesName) {
        mSeriesName = seriesName;
    }

    public String getEpisodeName() {
        return mEpisodeName;
    }

    public void setEpisodeName(String episodeName) {
        mEpisodeName = episodeName;
    }

    public Long getFirstAiredMs() {
        return mFirstAiredMs;
    }

    public void setFirstAiredMs(Long episodeName) {
        mFirstAiredMs = episodeName;
    }

    public String getPoster() {
        return mPoster;
    }

    public void setPoster(String poster) {
        mPoster = poster;
    }
}
