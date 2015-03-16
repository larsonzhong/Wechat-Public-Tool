package com.powerall.wxfxtools.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.powerall.wxfxtools.activity.LoginActivity;
import com.powerall.wxfxtools.activity.MyApplication;
import com.powerall.wxfxtools.model.bean.MessageBean;
import com.powerall.wxfxtools.model.bean.UserBean;
import com.powerall.wxfxtools.model.holder.ResponseHolder;
import com.powerall.wxfxtools.model.holder.ResultHolder;
import com.powerall.wxfxtools.util.net.HttpUtil;
import com.powerall.wxfxtools.util.net.UploadFileUtil;
import com.powerall.wxfxtools.util.net.UploadHelper;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理微信事务的类
 * <p/>
 * Created by larson on 11/02/15.
 */
public class WechatLoader {
    /**
     * 微信登录结果
     */
    public static final int WECHAT_RESULT_MESSAGE_OK = 10;
    public static final int WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT = 20;
    public static final int WECHAT_RESULT_MESSAGE_ERROR_OTHER = 30;


    public interface WechatLoginCallBack {
        public void onBack(int resultCode, String result, Header[] headers);
    }

    /**
     * 用户登录
     *
     * @param loginCallBack
     * @param username
     * @param pwd
     * @param imgcode
     * @param f
     */
    public static void login(final WechatLoginCallBack loginCallBack, final String username,
                             final String pwd, final String imgcode, final String f) {
        new Thread() {
            public void run() {
                Looper.prepare();
                ArrayList<NameValuePair> headerList = new ArrayList<>();
                headerList
                        .add(new BasicNameValuePair("Referer", "https://mp.weixin.qq.com/"));
                headerList.add(new BasicNameValuePair("Content-Type:", "application/x-www-form-urlencoded; charset=UTF-8"));
                headerList.add(new BasicNameValuePair("Connection", "Keep-Alive"));
                headerList.add(new BasicNameValuePair("Host", "mp.weixin.qq.com"));
                headerList.add(new BasicNameValuePair("Origin", "https://mp.weixin.qq.com"));
                headerList.add(new BasicNameValuePair("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.104 Safari/537.36"));
                headerList.add(new BasicNameValuePair("X-Requested-With", "XMLHttpRequest"));


                ArrayList<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("pwd", pwd));
                params.add(new BasicNameValuePair("imgcode", imgcode));
                params.add(new BasicNameValuePair("f", f));

                ResponseHolder responseHolder = HttpUtil.doHttpsPost(Configs.Url.WECHAT_URL_LOGIN, headerList,
                        params);

                Message message = new Message();
                ResultHolder resultHolder = new ResultHolder();

                switch (responseHolder.responseType) {
                    case ResponseHolder.RESPONSE_TYPE_OK:

                        message.arg1 = WECHAT_RESULT_MESSAGE_OK;
                        try {
                            Header[] headers = responseHolder.response.getAllHeaders();
                            resultHolder.putExtra("headers", headers);

                            String strResult = EntityUtils.toString(responseHolder.response
                                    .getEntity());
                            resultHolder.put("result", strResult);
                            message.obj = resultHolder;

                        } catch (Exception e) {
                            message.arg1 = WECHAT_RESULT_MESSAGE_ERROR_OTHER;
                        }

                        break;
                    case ResponseHolder.RESPONSE_TYPE_ERROR_TIME_OUT:
                        message.arg1 = WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT;
                        break;
                    case ResponseHolder.RESPONSE_TYPE_ERROR_OTHER:
                        message.arg1 = WECHAT_RESULT_MESSAGE_ERROR_OTHER;
                        break;
                }

                switch (message.arg1) {
                    case WECHAT_RESULT_MESSAGE_OK:
                        // 此处可以更新UI
                        resultHolder = (ResultHolder) message.obj;
                        loginCallBack.onBack(message.arg1, resultHolder.get("result"),
                                (Header[]) resultHolder.getExtra("headers"));
                        break;
                    case WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT:
                        loginCallBack.onBack(message.arg1, null, null);
                        break;
                    case WECHAT_RESULT_MESSAGE_ERROR_OTHER:
                        loginCallBack.onBack(message.arg1, null, null);
                        break;
                }
            }

        }.start();
    }


    /**
     * 获取用户信息
     *
     * @param getProfileHandler 获取到用户信息后的handler
     * @param userBean
     */
    public static void wechatGetUserProfile(final Handler getProfileHandler, final UserBean userBean) {

        new Thread() {
            public void run() {
                Map<String, String> result = new HashMap<>();
                Looper.prepare();
                ArrayList<NameValuePair> headerList = new ArrayList<>();
                headerList.add(new BasicNameValuePair("Cookie", "slave_sid="
                        + userBean.getSlaveSid() + "; " + "slave_user="
                        + userBean.getSlaveUser()));
                headerList.add(new BasicNameValuePair("Content-Type",
                        "text/html; charset=utf-8"));
                String targetUrl = Configs.Url.WECHAT_URL_GET_USER_PROFILE;

               /* PROFILE = "https://mp.weixin.qq.com/cgi-bin/home?t=home/index&lang=zh_CN&token=";
                */

                ArrayList<NameValuePair> paramList = new ArrayList<>();
                paramList.add(new BasicNameValuePair("t", "home/index"));
                paramList.add(new BasicNameValuePair("token", userBean.getToken()));
                paramList.add(new BasicNameValuePair("lang", "zh_CN"));

                ResponseHolder responseHolder = HttpUtil.httpGet(targetUrl, paramList, headerList);

                Message message = new Message();
                ResultHolder resultHolder = new ResultHolder();
                switch (responseHolder.responseType) {
                    case ResponseHolder.RESPONSE_TYPE_OK:
                        message.what = WECHAT_RESULT_MESSAGE_OK;
                        try {
                            String strResult = EntityUtils.toString(responseHolder.response
                                    .getEntity());
                            resultHolder.put("result", strResult);
                            resultHolder.put("referer", targetUrl);

                            message.obj = resultHolder;

                        } catch (Exception exception) {
                            message.what = WECHAT_RESULT_MESSAGE_ERROR_OTHER;
                        }
                        // 此处可以更新UI
                        result.put("result", resultHolder.get("result"));
                        result.put("referer", resultHolder.get("referer"));
                        break;
                    case ResponseHolder.RESPONSE_TYPE_ERROR_TIME_OUT:
                        message.what = WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT;
                        result.put("result", resultHolder.get("timeOut"));
                        break;
                    case ResponseHolder.RESPONSE_TYPE_ERROR_OTHER:
                        Log.e("wechat loader", "get user profile error other");
                        message.what = WECHAT_RESULT_MESSAGE_ERROR_OTHER;
                        result.put("result", resultHolder.get("other"));
                        break;
                }
                getProfileHandler.sendMessage(message);
            }

        }.start();

    }


    public static interface WechatGetFansCallback {
        public void onBack(int resultCode, String strResult, String referer);
    }

    public static void wechatGetFansList(
            final WechatGetFansCallback wechatGetFansCallback, final UserBean userBean,
            final String groupId, final int page) {


        new Thread() {
            public void run() {
                Looper.prepare();
                ArrayList<NameValuePair> headerList = new ArrayList<>();
                if (MyApplication.cookies != null) {
                    headerList.add(new BasicNameValuePair("Cookie", MyApplication.cookies));
                } else {
                    headerList.add(new BasicNameValuePair("Cookie", "slave_sid="
                            + userBean.getSlaveSid() + "; " + "slave_user="
                            + userBean.getSlaveUser()));
                }

                headerList.add(new BasicNameValuePair("Content-Type",
                        "text/html; charset=utf-8"));

                String referer = "";
                if (page == 0) {
                    if (groupId.equals("-1")) {
                        referer = "https://mp.weixin.qq.com/cgi-bin/home?t=home/index&lang=zh_CN&token="
                                + userBean.getToken();
                    } else {
                        referer = "https://mp.weixin.qq.com/cgi-bin/contactmanage?t=user/index&pagesize=10&pageidx=0&type=0&token=" + userBean.getToken() + "&lang=zh_CN";
                    }

                } else {
                    referer = "https://mp.weixin.qq.com/cgi-bin/contactmanage?t=user/index&pagesize=10&pageidx=" + (page - 1)
                            + "&type=0&groupid=" + groupId
                            + "&token=" + userBean.getToken()
                            + "&lang=zh_CN";

                }
                headerList.add(new BasicNameValuePair("Referer", referer));

                String targetUrl = Configs.Url.WECHAT_URL_GET_FANS_LIST;
                ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();

                paramList.add(new BasicNameValuePair("t", "user/index"));
                paramList.add(new BasicNameValuePair("pagesize", "10"));
                paramList.add(new BasicNameValuePair("pageidx", page + ""));
                paramList.add(new BasicNameValuePair("type", "0"));
                if (groupId.equals("-1")) {

                } else {
                    paramList.add(new BasicNameValuePair("groupid", groupId));
                }

                paramList.add(new BasicNameValuePair("token", userBean.getToken()));
                paramList.add(new BasicNameValuePair("lang", "zh_CN"));

                ResponseHolder responseHolder = HttpUtil.httpGet(targetUrl, paramList, headerList);

                Message message = new Message();
                ResultHolder resultHolder = new ResultHolder();

                switch (responseHolder.responseType) {
                    case ResponseHolder.RESPONSE_TYPE_OK:


                        message.arg1 = WECHAT_RESULT_MESSAGE_OK;
                        try {
                            String strResult = EntityUtils.toString(responseHolder.response
                                    .getEntity());
                            resultHolder.put("result", strResult);
                            resultHolder.put("referer", targetUrl);
                            message.obj = resultHolder;

                        } catch (Exception exception) {
                            message.arg1 = WECHAT_RESULT_MESSAGE_ERROR_OTHER;
                        }
                        resultHolder = (ResultHolder) message.obj;
                        wechatGetFansCallback.onBack(message.arg1, resultHolder.get("result"),
                                resultHolder.get("referer"));
                        break;
                    case ResponseHolder.RESPONSE_TYPE_ERROR_TIME_OUT:
                        message.arg1 = WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT;
                        wechatGetFansCallback.onBack(message.arg1, "timeOut", null);
                        break;
                    case ResponseHolder.RESPONSE_TYPE_ERROR_OTHER:
                        message.arg1 = WECHAT_RESULT_MESSAGE_ERROR_OTHER;
                        wechatGetFansCallback.onBack(message.arg1, "other", null);
                        break;
                }

            }

        }.start();
    }


    public interface WechatChatSingleCallBack {
        public void onBack(int resultCode, String result);
    }

    public static void wechatChatSingle(
            final WechatChatSingleCallBack chatSingleCallBack,
            final UserBean userBean, final MessageBean messageBean) {
        final Handler loadHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub

                super.handleMessage(msg);
                switch (msg.arg1) {
                    case WECHAT_RESULT_MESSAGE_OK:

                        // 此处可以更新UI
                        ResultHolder resultHolder = (ResultHolder) msg.obj;
                        chatSingleCallBack.onBack(msg.arg1, resultHolder.get("result"));

                        break;
                    case WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT:
                        chatSingleCallBack.onBack(msg.arg1, "timeOut");

                        break;
                    case WECHAT_RESULT_MESSAGE_ERROR_OTHER:

                        chatSingleCallBack.onBack(msg.arg1, "other");

                        break;
                }
            }
        };

        new Thread() {
            public void run() {
                Looper.prepare();
                ArrayList<NameValuePair> headerList = new ArrayList<>();

                final String referer = "https://mp.weixin.qq.com/cgi-bin/singlesendpage?tofakeid="
                        + messageBean.getToFakeId() + "&t=message/send&action=index&token="
                        + userBean.getToken()
                        + "&lang=zh_CN";
                headerList.add(new BasicNameValuePair("Referer", referer));

                headerList.add(new BasicNameValuePair("Cookie", MyApplication.cookies));
                //1.----区别1（发送文件和发送消息）----------------
                if (TextUtils.isEmpty(messageBean.getFilePath())) {
                    headerList.add(new BasicNameValuePair("Content-Type",
                            "text/html; charset=utf-8"));
                } else {
                    headerList.add(new BasicNameValuePair("Content-Type",
                            "application/x-www-form-urlencoded; charset=UTF-8"));
                }

                ArrayList<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("type", messageBean.getType() + ""));
                params.add(new BasicNameValuePair("tofakeid", ""
                        + messageBean.getToFakeId()));
                params.add(new BasicNameValuePair("imgcode", ""));
                params.add(new BasicNameValuePair("token", ""
                        + userBean.getToken()));
                params.add(new BasicNameValuePair("lang", "zh_CN"));
                params.add(new BasicNameValuePair("random",
                        Util.getRandomFloat(18)));
                params.add(new BasicNameValuePair("f", "json"));
                params.add(new BasicNameValuePair("ajax", "1"));
                params.add(new BasicNameValuePair("t", "ajax-response"));

                if (!TextUtils.isEmpty(messageBean.getContent()) || !TextUtils.isEmpty(messageBean.getAppId()) || !TextUtils.isEmpty(messageBean.getFileId())) {
                    //不同的消息发送的表单有所不同(至少有一种方式可以到达)
                    if (!TextUtils.isEmpty(messageBean.getContent())) {
                        params.add(new BasicNameValuePair("content", messageBean.getContent()));
                    } else if (!TextUtils.isEmpty(messageBean.getAppId())) {
                        params.add(new BasicNameValuePair("app_id", messageBean.getAppId()));
                        params.add(new BasicNameValuePair("appmsgid", messageBean.getAppId()));
                    } else if (!TextUtils.isEmpty(messageBean.getFileId())) {
                        params.add(new BasicNameValuePair("file_id", messageBean.getFileId()));
                        params.add(new BasicNameValuePair("fileid", messageBean.getFileId()));
                    }

                    ResponseHolder responseHolder = httpPost(Configs.Url.WECHAT_URL_CHAT_SINGLE_SEND,
                            headerList, params, messageBean.getFilePath());

                    Message message = new Message();
                    ResultHolder resultHolder = new ResultHolder();

                    switch (responseHolder.responseType) {
                        case ResponseHolder.RESPONSE_TYPE_OK:

                            message.arg1 = WECHAT_RESULT_MESSAGE_OK;
                            try {

                                String strResult = EntityUtils.toString(responseHolder.response
                                        .getEntity());
                                resultHolder.put("result", strResult);
                                message.obj = resultHolder;

                            } catch (Exception exception) {

                                message.arg1 = WECHAT_RESULT_MESSAGE_ERROR_OTHER;
                            }
                            break;
                        case ResponseHolder.RESPONSE_TYPE_ERROR_TIME_OUT:

                            message.arg1 = WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT;
                            break;
                        case ResponseHolder.RESPONSE_TYPE_ERROR_OTHER:

                            message.arg1 = WECHAT_RESULT_MESSAGE_ERROR_OTHER;

                            break;
                    }
                    loadHandler.sendMessage(message);
                } else {//TODO 发送的俄时文件，可是文件没有上传，需要先上传文件，然后拿到文件的id或者appID,才能继续发送
                    try {
                        UploadFileUtil.getUploadInfo(new WechatManager.OnActionFinishListener() {
                            @Override
                            public void onFinish(final int code, final Object object) {
                                switch (code) {
                                    case WechatManager.ACTION_SUCCESS:
                                        UploadFileUtil.wechatUploadFile(new UploadFileUtil.WechatUploadFileCallBack() {
                                            @Override
                                            public void onBack(int resultCode, String result) {
                                                switch (resultCode) {
                                                    case WECHAT_RESULT_MESSAGE_OK:
                                                        Map<String, String> map = JsonUtil.getRet(result);
                                                        int ret = Integer.parseInt(map.get("ret"));
                                                        if (ret == 0) {
                                                            //为了方便，不在解析json的时候就拿到内容，而是先拿到ret，如果ret通过才拿上传结果返回内容
                                                            //解析后的结果设置在dataManager里面便于使用
                                                            Gson gson = new Gson();
                                                            UploadHelper.NowUploadBean nowUploadBean = gson.fromJson(result, UploadHelper.NowUploadBean.class);
                                                            DataManager.getInstance().getMessageHolders().get(0).getUploadHelper().setNowUploadBean(nowUploadBean);
                                                            //上传完成，再发送
                                                            messageBean.setFileId(nowUploadBean.getContent());
                                                            wechatChatSingle(chatSingleCallBack, userBean, messageBean);
                                                        }
                                                        break;
                                                    case WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT:
                                                        break;
                                                    case WECHAT_RESULT_MESSAGE_ERROR_OTHER:
                                                        break;

                                                }
                                            }
                                        }, messageBean, userBean, DataManager.getInstance().getMessageHolders().get(0).getUploadHelper().getTicket());

                                    case WechatManager.ACTION_TIME_OUT:
                                        break;
                                    case WechatManager.ACTION_OTHER:
                                        break;
                                    case WechatManager.ACTION_SPECIFICED_ERROR:

                                        break;
                                }

                            }
                        }, MyApplication.currentUser);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

        }.start();
    }

    /**
     * 如果不是文本信息，则三步走：
     * 1.上传文件（进度同步显示，使用handler）
     * 2.拿到文件上传后的file_id，如果是视频则是video_id
     * 3.发送消息。
     */
    private static ResponseHolder httpPost(String targetUrl,
                                           ArrayList<NameValuePair> headerArrayList,
                                           ArrayList<NameValuePair> paramsArrayList, String filePath) {
        HttpPost httpPost = new HttpPost(targetUrl);
        try {

            httpPost.setEntity(new UrlEncodedFormEntity(paramsArrayList, "UTF-8"));

            for (int i = 0; i < headerArrayList.size(); i++) {
                httpPost.addHeader(headerArrayList.get(i).getName(),
                        headerArrayList.get(i).getValue());
            }


            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);
            HttpClient httpClient = new DefaultHttpClient(httpParams);

			/* 取得HTTP response */
            HttpResponse httpResponse = httpClient
                    .execute(httpPost);

			/* 若状态码为200 ok */
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                /* 取出响应字符串 */

                return new ResponseHolder(ResponseHolder.RESPONSE_TYPE_OK, httpResponse);

            } else {
                Log.e("errorcode", httpResponse.getStatusLine().toString() + "|" + targetUrl + "|" + paramsArrayList.toString());
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException || e instanceof javax.net.ssl.SSLPeerUnverifiedException) {
                return new ResponseHolder(ResponseHolder.RESPONSE_TYPE_ERROR_TIME_OUT, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseHolder(ResponseHolder.RESPONSE_TYPE_ERROR_OTHER, null);
    }


    public interface WechatGetChatNewItems {
        public void onBack(int resultCode, String strResult);
    }

    public static void wechatGetChatNewItems(
            final WechatGetChatNewItems wechatGetChatNewItems, final UserBean userBean,
            final MessageBean messageBean,
            final String lastMsgId,
            final String createTime,
            final String toFakeId) {
        final Handler loadHandler = new Handler() {
            // 子类必须重写此方法,接受数据
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub

                super.handleMessage(msg);
                switch (msg.arg1) {
                    case WECHAT_RESULT_MESSAGE_OK:


                        // 此处可以更新UI
                        ResultHolder resultHolder = (ResultHolder) msg.obj;
                        wechatGetChatNewItems.onBack(msg.arg1, resultHolder.get("result"));

                        break;
                    case WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT:

                        wechatGetChatNewItems.onBack(msg.arg1, "timeOut");
                        break;
                    case WECHAT_RESULT_MESSAGE_ERROR_OTHER:

                        wechatGetChatNewItems.onBack(msg.arg1, "other");
                        break;
                }

            }
        };

        new Thread() {
            public void run() {
                Looper.prepare();
                ArrayList<NameValuePair> headerList = new ArrayList<NameValuePair>();
                headerList.add(new BasicNameValuePair("Cookie", "slave_sid="
                        + userBean.getSlaveSid() + "; " + "slave_user="
                        + userBean.getSlaveUser()));

                String referer = "https://mp.weixin.qq.com/cgi-bin/singlesendpage?tofakeid="
                        + messageBean.getToFakeId() + "&t=message/send&action=index&token="
                        + userBean.getToken()
                        + "&lang=zh_CN";
                headerList.add(new BasicNameValuePair("Referer", referer));
                headerList.add(new BasicNameValuePair("Content-Type",
                        "text/html; charset=utf-8"));


                ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
                paramList.add(new BasicNameValuePair("token", userBean.getToken()));
                paramList.add(new BasicNameValuePair("lang", "zh_CN"));
                paramList.add(new BasicNameValuePair("random", Util.getRandomFloat(16)));
                paramList.add(new BasicNameValuePair("f", "json"));
                paramList.add(new BasicNameValuePair("ajax", "1"));
                paramList.add(new BasicNameValuePair("tofakeid", toFakeId));
                paramList.add(new BasicNameValuePair("action", "sync"));
                paramList.add(new BasicNameValuePair("lastmsgfromfakeid", userBean.getFakeId()));
                paramList.add(new BasicNameValuePair("lastmsgid", lastMsgId));
                paramList.add(new BasicNameValuePair("createtime", createTime));
                String targetUrl = Configs.Url.WECHAT_URL_GET_CHAT_NEW_ITEM;

                ResponseHolder responseHolder = HttpUtil.httpGet(targetUrl, paramList, headerList);


                Message message = new Message();
                ResultHolder resultHolder = new ResultHolder();

                switch (responseHolder.responseType) {
                    case ResponseHolder.RESPONSE_TYPE_OK:

                        message.arg1 = WECHAT_RESULT_MESSAGE_OK;
                        try {

                            String strResult = EntityUtils.toString(responseHolder.response
                                    .getEntity());
                            resultHolder.put("result", strResult);

                            message.obj = resultHolder;

                        } catch (Exception exception) {
                            message.arg1 = WECHAT_RESULT_MESSAGE_ERROR_OTHER;
                        }
                        break;
                    case ResponseHolder.RESPONSE_TYPE_ERROR_TIME_OUT:

                        message.arg1 = WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT;
                        break;
                    case ResponseHolder.RESPONSE_TYPE_ERROR_OTHER:
                        message.arg1 = WECHAT_RESULT_MESSAGE_ERROR_OTHER;
                        break;
                }

                loadHandler.sendMessage(message);

            }

        }.start();

    }


}
