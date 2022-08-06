package com.grieex.core.listener;

public interface OnImdbEventListener {
    void onCompleted(Object m);

    void onNotCompleted(Throwable error, String content);
}