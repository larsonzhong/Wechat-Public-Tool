package com.powerall.wxfxtools.model.holder;

import org.apache.http.HttpResponse;

/**
 * Created by larson on 04/03/15.
 */
public  class ResponseHolder {
    public static final int RESPONSE_TYPE_OK = 2;
    public static final int RESPONSE_TYPE_ERROR_TIME_OUT = 3;
    public static final int RESPONSE_TYPE_ERROR_OTHER = 4;
    public int responseType;
    public HttpResponse response;

    public ResponseHolder(int responseType, HttpResponse response) {

        this.responseType = responseType;
        this.response = response;

    }
}