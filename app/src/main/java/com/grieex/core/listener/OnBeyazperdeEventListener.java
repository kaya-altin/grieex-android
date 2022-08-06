package com.grieex.core.listener;

public interface OnBeyazperdeEventListener {
    void onCompleted(Object m);

    void onNotCompleted(Throwable error, String content);
}
