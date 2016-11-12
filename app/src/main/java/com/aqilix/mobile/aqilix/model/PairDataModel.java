package com.aqilix.mobile.aqilix.model;

/**
 * Created by surya on 9/15/16.
 */
public class PairDataModel {
    protected String key;
    protected String value;

    public PairDataModel(String key, String value) {
        setKey(key);
        setValue(value);
    }

    public PairDataModel() {

    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
