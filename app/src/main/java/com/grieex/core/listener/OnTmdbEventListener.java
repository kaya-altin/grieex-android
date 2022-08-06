package com.grieex.core.listener;

public interface OnTmdbEventListener {
    void onCompleted(Object m);

    void onNotCompleted(Throwable error, String content);
}