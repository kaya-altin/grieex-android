package com.grieex.core.listener;

public interface OnTmdbTvEventListener {
    void onCompleted(Object m);

    void onNotCompleted(Throwable error, String content);
}