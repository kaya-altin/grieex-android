package com.grieex.core.listener;

public interface OnSinemalarEventListener {
    void onCompleted(Object m);

    void onNotCompleted(Throwable error, String content);
}