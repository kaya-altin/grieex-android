package com.sburba.tvdbapi.tools;

public class TvDbException extends RuntimeException {

    public TvDbException(String message) {
        super(message);
    }


    public TvDbException(String message, Throwable cause) {
        super(message, cause);
    }
}
