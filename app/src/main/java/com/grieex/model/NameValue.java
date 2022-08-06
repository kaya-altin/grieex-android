package com.grieex.model;

/**
 * Created by durmus on 7.5.2015.
 */
public class NameValue {
    private final String mName;
    private final String mValue;

    public NameValue(String name, String value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public String getValue() {
        return mValue;
    }
}

