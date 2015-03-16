package com.powerall.wxfxtools.util;

import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.powerall.wxfxtools.model.bean.FansBean;
import com.powerall.wxfxtools.model.bean.FansGroupBean;
import com.powerall.wxfxtools.model.holder.FansResultHolder;
import com.powerall.wxfxtools.model.bean.UserBean;
import com.powerall.wxfxtools.util.net.HttpUtil;
import com.powerall.wxfxtools.util.net.UploadHelper;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析服务器返回的html数据
 * <p/>
 * Created by larson on 11/02/15.
 */
public class DataParser {
    public static final int PARSE_SUCCESS = 1;
    public static final int PARSE_FAILED = 2;
    public static final int PARSE_SPECIFIC_ERROR = 3;
    public static final int PARSE_SPECIFIC_STUATION = 4;
    public static final int PARSE_ACC_PWD_ERR = 5;

    public static final int GET_USER_PROFILE_SUCCESS = 1;

    public static final int GET_USER_PROFILE_FAILED = 0;
    public static final int RET_LOGIN_SUCCESS = 0;
    public static final int RET_LOGIN_ACCVPWD_ERROR = -23;//用户名或者密码错误
    public static final String LOGIN_NEEDS_VERIFY = "/cgi-bin/readtemplate?t=user/validate_phone_tmpl";
    private static final String login_timeout = "登录超时";

    public interface LoginParseCallBack {
        public void onBack(int code, UserBean userBean);
    }


    public static void parseLogin(
            final LoginParseCallBack loginParseCallBack,
            final String source, final Header[] headers, final UserBean userBean) {
        //开启新线程登录，
        new Thread() {
            public void run() {
                Message message = new Message();
                message.arg1 = PARSE_FAILED;
                try {
                    Map<String, String> ret = JsonUtil.getRet(source);
                    int code = Integer.parseInt(ret.get("ret"));
                    if (code == RET_LOGIN_SUCCESS) {
                        String redirUrl = ret.get("redirect_url");
                        if (redirUrl.contains("token")) {
                            String tokenString = redirUrl.substring(redirUrl.lastIndexOf("=") + 1);
                            userBean.setToken(tokenString);
                            initNormalLogin();

                            message.arg1 = PARSE_SUCCESS;
                            message.obj = userBean;

                        } else {
                            HttpUtil.loginRedirectGet(redirUrl);
                            if (source.contains(LOGIN_NEEDS_VERIFY)) {

                                initVerify();
                                message.arg1 = PARSE_SPECIFIC_STUATION;
                                message.obj = userBean;
                            }

                        }
                    } else if (code == RET_LOGIN_ACCVPWD_ERROR) {
                        message.arg1 = PARSE_ACC_PWD_ERR;
                    }
                } catch (Exception e) {
                    Log.e("app list parse error", "" + e);
                }
                if (message.arg1 == PARSE_FAILED && source.contains(login_timeout)) {
                    message.arg1 = PARSE_SPECIFIC_ERROR;
                }

                switch (message.arg1) {
                    case PARSE_SUCCESS:
                        loginParseCallBack.onBack(message.arg1, (UserBean) message.obj);
                        break;
                    default:
                        loginParseCallBack.onBack(message.arg1, null);
                        break;
                }
            }

            /**
             *
             */
            private void initNormalLogin() {
                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].getName().contains(
                            "Set-Cookie")) {
                        String nowCookie = headers[i]
                                .getValue();
                        if (nowCookie.contains("slave_user")) {

                            String slaveUser = nowCookie
                                    .substring(
                                            nowCookie
                                                    .indexOf("slave_user") + 11,
                                            nowCookie.indexOf(";"));
                            userBean.setSlaveUser(slaveUser);
                        }
                        if (nowCookie.contains("slave_sid")) {

                            String slaveSid = nowCookie
                                    .substring(nowCookie
                                                    .indexOf("slave_sid") + 10,
                                            nowCookie.indexOf(";"));
                            userBean.setSlaveSid(slaveSid);
                        }

                    }

                }
            }

            private void initVerify() {

                try {
                    JSONObject resultObject = new JSONObject(source);
                    String errorMsg = resultObject.get("ErrMsg").toString();
                    String regex = "phone=(.*)";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(errorMsg);
                    while (matcher.find()) {
                        String phone = matcher.group(1);
                        if (phone != null) {
                            userBean.setPhone(phone);
                        }
                    }
                } catch (Exception e) {

                }
            }
        }.start();
    }

    private static String getToken(JSONObject resultJsonObject) {
        String tokenString = "";
        try {
            String contentString = resultJsonObject.getString("ErrMsg");
            String regex = "token=(\\d*)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(contentString);
            while (matcher.find()) {
                String getToken = matcher.group(1);
                if (getToken != null) {
                    tokenString = getToken;
                }
            }
        } catch (Exception e) {

        }
        return tokenString;
    }


    public static int parseUserProfile(String source, UserBean userBean) {

        Document document = Jsoup.parse(source);
        Elements numElements = document.getElementsByClass("number");
        for (int i = 0; i < numElements.size(); i++) {
            if (numElements.size() == 3) {

                if (i == 0) {
                    int newMessage = Integer
                            .parseInt(numElements.get(i).html());
                    userBean.setNewMessage(newMessage + "");

                }
                if (i == 1) {

                    int newPeople = Integer.parseInt(numElements.get(i).html());
                    userBean.setNewPeople(newPeople + "");
                }
                if (i == 2) {

                    int totalPeople = Integer.parseInt(numElements.get(i)
                            .html());
                    userBean.setTotalPeople(totalPeople + "");
                }
            }
        }

        Elements avataElements = document.getElementsByClass("avatar");

        for (int i = 0; i < avataElements.size(); i++) {
            String fakeId = getProfileFakeId(avataElements.get(i).attr("src"));
            if (!fakeId.equals("")) {
                userBean.setFakeId(fakeId);

            }

        }

        Elements nickNameElements = document.getElementsByClass("nickname");

        for (int i = 0; i < nickNameElements.size(); i++) {
            String nickNameString = nickNameElements.get(i).html();

            if (nickNameString != "") {

                userBean.setNickname(nickNameString);

                return GET_USER_PROFILE_SUCCESS;
            }
        }

        return GET_USER_PROFILE_FAILED;
    }

    private static String getProfileFakeId(String source) {

        String result = "";
        Pattern pattern = Pattern.compile("fakeid=(\\d*)");

        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            return matcher.group(1);
        }

        return result;
    }


    public interface FansListParseCallback {
        public void onBack(FansResultHolder fansResultHolder, int code);
    }

    public static void parseFansList(final String source, final String referer, final int currentGroupIndex,
                                     final UserBean userBean,
                                     final boolean refresh,
                                     final FansListParseCallback fansListParseCallback) {
        new Thread() {

            public void run() {
                Message nowMessage = new Message();
                nowMessage.arg1 = PARSE_FAILED;
                JSONObject fansContentObject = getFansContentObject(source);
                if (fansContentObject != null) {

                    try {

                        String fansTypeString = fansContentObject.get("fansType").toString();//粉丝分组
                        String fansContentString = fansContentObject.get("fansContent").toString();//粉丝列表
                        Gson gson = new Gson();
                        JSONArray fansTypeArray = new JSONArray(fansTypeString);
                        ArrayList<FansGroupBean> fansGroupBeans = new ArrayList<>();
                        for (int i = 0; i < fansTypeArray.length(); i++) {
                            JSONObject nowJsonObject = fansTypeArray
                                    .getJSONObject(i);
                            FansGroupBean nowGroupBean = gson
                                    .fromJson(nowJsonObject.toString(),
                                            FansGroupBean.class);
                            fansGroupBeans.add(nowGroupBean);
                        }


                        JSONArray fansArray = new JSONArray(fansContentString);

                        ArrayList<FansBean> fansBeans = new ArrayList<>();
                        for (int i = 0; i < fansArray.length(); i++) {
                            JSONObject nowJsonObject = fansArray.getJSONObject(i);
                            FansBean nowFansBean = gson.fromJson(
                                    nowJsonObject.toString(), FansBean.class);
                            nowFansBean.setReferer(referer);
                            fansBeans.add(nowFansBean);
                        }

//                        if (refresh) {
//                            //add fans data
//                            FansBean dataBean = new FansBean();
//                            dataBean.setBeanType(FansBean.BEAN_TYPE_DATA);
//                            fansBeans.add(0, dataBean);
//                        }

                        FansResultHolder fansResultHolder = new FansResultHolder(fansBeans, fansGroupBeans, currentGroupIndex,
                                refresh ? FansResultHolder.RESULT_MODE_REFRESH : FansResultHolder.RESULT_MODE_ADD);

                        fansListParseCallback.onBack(fansResultHolder, PARSE_SUCCESS);

                    } catch (Exception exception) {
                        Log.e("fans parse error", "" + exception);
                        fansListParseCallback.onBack(null, nowMessage.arg1);
                    }
                }

            }

            private JSONObject getFansContentObject(String source) {
                String regex = "groupsList\\s*:\\s*\\(\\{\"groups\":(\\[[^\\]]*\\])[^\\[]*(\\[[^\\]]*])";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(source);
                while (matcher.find()) {
                    String fansType = matcher.group(1);
                    String fansContent = matcher.group(2);
                    JSONObject fansContentObject = new JSONObject();
                    try {
                        fansContentObject.put("fansType", fansType);
                        fansContentObject.put("fansContent", fansContent);

                        return fansContentObject;

                    } catch (Exception e) {

                    }
                }

                return null;
            }

        }.start();

    }


    public interface UploadInfoParseCallBack {
        public void onBack(int code);
    }

    /**
     * 通过解析返回的内容（正则表达式）拿到ticket，保存到dataManager
     *
     * @param uploadInfoParseCallBack
     * @param source                  返回的内容
     * @param uploadHelper
     */
    public static void parseUploadInfo(final UploadInfoParseCallBack uploadInfoParseCallBack,
                                       final String source, final UploadHelper uploadHelper) {
        Looper.prepare();
        new Thread() {
            public void run() {
                try {
                    String ticket = getTickets(source);
                    Log.e("get ticket", "" + ticket);
                    if (ticket != null) {
                        uploadHelper.setTicket(ticket);
                        uploadInfoParseCallBack.onBack(PARSE_SUCCESS);
                    }
                } catch (Exception e) {
                    Log.e("upload info parse error", "" + e);
                    uploadInfoParseCallBack.onBack(PARSE_FAILED);
                }
            }

            private String getTickets(String source) {
                String result = null;
                String regx = "data:(\\{[^\\}]*)";

                Pattern pattern = Pattern.compile(regx);
                Matcher matcher = pattern.matcher(source);
                while (matcher.find()) {

                    String dataString = matcher.group(1);
                    Log.e("get data", "" + dataString);
                    if (dataString != null) {
                        regx = "ticket:\"([^\"]*)\"";
                        pattern = Pattern.compile(regx);
                        matcher = pattern.matcher(dataString);
                        while (matcher.find()) {
                            String ticket = matcher.group(1);
                            return ticket;
                        }
                    }
                }
                return result;
            }

        }.start();
    }


}
