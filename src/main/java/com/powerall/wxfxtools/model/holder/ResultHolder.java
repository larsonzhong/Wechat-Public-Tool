package com.powerall.wxfxtools.model.holder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by larson on 04/03/15.
 */
public class ResultHolder {
    private Map<String, String> content = new HashMap<>();
    private Map<String, Object> extra = new HashMap<>();

    public void put(String key, String value) {
        content.put(key, value);
    }

    public String get(String key) {
        return content.get(key);
    }

    public void putExtra(String key, Object object) {
        extra.put(key, object);
    }

    public Object getExtra(String key) {
        return extra.get(key);
    }

}