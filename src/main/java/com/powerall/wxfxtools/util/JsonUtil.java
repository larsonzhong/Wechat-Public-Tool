package com.powerall.wxfxtools.util;

import com.google.gson.Gson;
import com.powerall.wxfxtools.util.net.UploadHelper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by larson on 10/02/15.
 */
public class JsonUtil {
    public static final int GET_RET_NONE = -1;

    public static Map<String, String> getRet(String result) {
        Map<String, String> map = new HashMap();
        try {//{"base_resp":{"ret":0,"err_msg":"ok"},"location":"bizfile","type":"image","content":"200009060"}
            JSONObject resultObject = new JSONObject(result);
            if (resultObject.has("base_resp")) {
                JSONObject retObj = resultObject.getJSONObject("base_resp");
                if (retObj != null) {
                    int ret = retObj.getInt("ret");
                    String err_msg = retObj.getString("err_msg");

                    if (ret == 0 && err_msg.equalsIgnoreCase("ok")) {
                        map.put("ret", ret + "");
                        if (resultObject.has("redirect_url")) {
                            String redirect_url = resultObject.getString("redirect_url");
                            map.put("redirect_url", redirect_url);
                        }
                        return map;
                    } else {
                        map.put("ret", ret + "");
                        map.put("err_msg", err_msg);
                        return map;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put("ret", GET_RET_NONE + "");
        return map;
    }

}
