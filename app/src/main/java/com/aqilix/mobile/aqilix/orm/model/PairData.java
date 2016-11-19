package com.aqilix.mobile.aqilix.orm.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by dolly on 11/19/16.
 */
@DatabaseTable(tableName = "pair_data")
public class PairData {
    @DatabaseField(id=true)
    protected String key;

    @DatabaseField
    protected String value;

    public PairData() {

    }

    public PairData(String key, String value) {
        this.setKey(key);
        this.setValue(value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
