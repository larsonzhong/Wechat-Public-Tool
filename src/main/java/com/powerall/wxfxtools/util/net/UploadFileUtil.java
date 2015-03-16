package com.powerall.wxfxtools.util.net;

import android.os.Looper;
import android.util.Log;

import com.powerall.wxfxtools.model.bean.MessageBean;
import com.powerall.wxfxtools.model.bean.UserBean;
import com.powerall.wxfxtools.model.holder.ResponseHolder;
import com.powerall.wxfxtools.model.holder.ResultHolder;
import com.powerall.wxfxtools.util.Configs;
import com.powerall.wxfxtools.util.DataManager;
import com.powerall.wxfxtools.util.DataParser;
import com.powerall.wxfxtools.util.WechatLoader;
import com.powerall.wxfxtools.util.WechatManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * 文件上传处理的类
 * Created by larson on 03/03/15.
 */
public class UploadFileUtil {

    public interface WechatUploadFileCallBack {
        public void onBack(int resultCode, String result);
    }

    public static void wechatUploadFile(
            final WechatUploadFileCallBack wechatUploadFileCallBack,
            final MessageBean msg, final UserBean userBean, final String ticket) {
        //1.获取文件上传信息


        new Thread() {
            public void run() {
                Looper.prepare();
                ArrayList<NameValuePair> headerList = new ArrayList<>();
                String referer = "https://mp.weixin.qq.com/cgi-bin/filepage?type=" + msg.getType() + "&begin=0&count=10&t=media/list&token=" + userBean.getToken() + "&lang=zh_CN";

                headerList
                        .add(new BasicNameValuePair("Referer",
                                referer));

                headerList.add(new BasicNameValuePair("Cookie", "slave_sid="
                        + userBean.getSlaveSid() + "; " + "slave_user="
                        + userBean.getSlaveUser()));

                ArrayList<NameValuePair> params = new ArrayList<>();

                params.add(new BasicNameValuePair("action", "upload_material"));
                params.add(new BasicNameValuePair("f", "json"));

                params.add(new BasicNameValuePair("writetype", "doublewrite"));
                params.add(new BasicNameValuePair("groupid", "1"));

                params.add(new BasicNameValuePair("ticket_id", userBean.getSlaveUser()));
                params.add(new BasicNameValuePair("ticket", ticket));
                params.add(new BasicNameValuePair("token", userBean.getToken()));
                params.add(new BasicNameValuePair("lang", "zh_CN"));

                ResponseHolder responseHolder = httpUploadFile(Configs.Url.WECHAT_URL_UPLOAD_FILE, msg.getFilePath(), headerList,
                        params);
                ResultHolder resultHolder = new ResultHolder();

                switch (responseHolder.responseType) {
                    case ResponseHolder.RESPONSE_TYPE_OK:
//这里resultHolder.get("result")其实没用的，因为是一个页面，然后经过datapaser解析出了ticket
                        try {
                            String strResult = EntityUtils.toString(responseHolder.response
                                    .getEntity());
                            resultHolder.put("result", strResult);
                            wechatUploadFileCallBack.onBack(WechatLoader.WECHAT_RESULT_MESSAGE_OK, resultHolder.get("result"));
                        } catch (Exception exception) {
                            wechatUploadFileCallBack.onBack(WechatLoader.WECHAT_RESULT_MESSAGE_ERROR_OTHER, "other");
                        }
                        break;
                    case ResponseHolder.RESPONSE_TYPE_ERROR_TIME_OUT:
                        wechatUploadFileCallBack.onBack(WechatLoader.WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT, "timeOut");
                        break;
                    case ResponseHolder.RESPONSE_TYPE_ERROR_OTHER:

                        wechatUploadFileCallBack.onBack(WechatLoader.WECHAT_RESULT_MESSAGE_ERROR_OTHER, "other");
                        break;
                }


            }

        }.start();

    }


    private static ResponseHolder httpUploadFile(String targetUrl, String filePath,
                                                 ArrayList<NameValuePair> headerArrayList,
                                                 ArrayList<NameValuePair> paramsArrayList) {
        //about url
        for (int i = 0; i < paramsArrayList.size(); i++) {
            NameValuePair nowPair = paramsArrayList.get(i);
            if (i == 0) {
                targetUrl += ("?" + nowPair.getName() + "=" + nowPair.getValue());
            } else {
                targetUrl += ("&" + nowPair.getName() + "=" + nowPair.getValue());
            }
        }

        /* 建立HTTP Post联机 */
        HttpPost httpRequest = new HttpPost(targetUrl);
        /*
         * Post运作传送变量必须用NameValuePair[]数组储存
		 */

        try {
            //set entity
            MultipartEntity multipartEntity = new MultipartEntity();

            File uploadFile = new File(filePath);
            FileBody fileBody = new FileBody(uploadFile);

            String fileName = uploadFile.getName();
            Log.e("upload file name", "" + fileName);
            multipartEntity.addPart("Filename", new StringBody(fileName));
            multipartEntity.addPart("folder", new StringBody("/cgi-bin/uploads"));
            multipartEntity.addPart("file", fileBody);
            multipartEntity.addPart("Upload", new StringBody("Submit Query"));
            httpRequest.setEntity(multipartEntity);


            for (int i = 0; i < headerArrayList.size(); i++) {

                httpRequest.addHeader(headerArrayList.get(i).getName(),
                        headerArrayList.get(i).getValue());

            }

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 100000);
            HttpConnectionParams.setSoTimeout(httpParams, 30000);
            HttpClient httpClient = new DefaultHttpClient(httpParams);

			/* 取得HTTP response */
            HttpResponse httpResponse = httpClient
                    .execute(httpRequest);

			/* 若状态码为200 ok */
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                /* 取出响应字符串 */

                return new ResponseHolder(ResponseHolder.RESPONSE_TYPE_OK, httpResponse);

            } else {
                Log.e("errorcode", httpResponse.getStatusLine().toString());
            }
        } catch (ClientProtocolException e) {
            Log.e("upload client pro", "" + e);

            e.printStackTrace();
        } catch (IOException e) {

            Log.e("upload io", "" + e);
            e.printStackTrace();
            if (e instanceof SocketTimeoutException || e instanceof javax.net.ssl.SSLPeerUnverifiedException) {

                return new ResponseHolder(ResponseHolder.RESPONSE_TYPE_ERROR_TIME_OUT, null);
            }
        } catch (Exception e) {

            Log.e("upload ex", "" + e);
            e.printStackTrace();
        }

        return new ResponseHolder(ResponseHolder.RESPONSE_TYPE_ERROR_OTHER, null);

    }


    public static void getUploadInfo(
            final WechatManager.OnActionFinishListener finishListener, final UserBean userBean) {

        new Thread() {
            public void run() {
                ArrayList<NameValuePair> headerList = new ArrayList<>();
                headerList.add(new BasicNameValuePair("Cookie", "slave_sid="
                        + userBean.getSlaveSid() + "; " + "slave_user="
                        + userBean.getSlaveUser()));

                String referer = "https://mp.weixin.qq.com/cgi-bin/appmsg?begin=0&count=10&t=media/appmsg_list&type=10&action=list&token="
                        + userBean.getToken() + "&lang=zh_CN";
                headerList.add(new BasicNameValuePair("Referer", referer));
                headerList.add(new BasicNameValuePair("Content-Type",
                        "text/html; charset=utf-8"));


                ArrayList<NameValuePair> paramList = new ArrayList<>();
                paramList.add(new BasicNameValuePair("token", userBean.getToken()));
                paramList.add(new BasicNameValuePair("lang", "zh_CN"));
                paramList.add(new BasicNameValuePair("begin", "0"));
                paramList.add(new BasicNameValuePair("count", "10"));
                paramList.add(new BasicNameValuePair("t", "media/list"));
                paramList.add(new BasicNameValuePair("type", "2"));
                String targetUrl = Configs.Url.WECHAT_URL_GET_UPLOAD_INFO;
                ResponseHolder responseHolder = HttpUtil.httpGet(targetUrl, paramList, headerList);

                ResultHolder resultHolder = new ResultHolder();

                switch (responseHolder.responseType) {
                    case ResponseHolder.RESPONSE_TYPE_OK:

                        try {
                            String strResult = EntityUtils.toString(responseHolder.response
                                    .getEntity());
                            resultHolder.put("result", strResult);

                            DataParser.parseUploadInfo(new DataParser.UploadInfoParseCallBack() {
                                @Override
                                public void onBack(int code) {
                                    if (code == DataParser.PARSE_SUCCESS) {
                                        finishListener.onFinish(WechatManager.ACTION_SUCCESS, null);
                                        Log.e("parse upload info success", "success");
                                    }
                                }
                            }, strResult, DataManager.getInstance().getMessageHolders().get(0).getUploadHelper());

//                            finishListener.onFinish(WechatManager.ACTION_OTHER, resultHolder.get("result"));
                        } catch (Exception exception) {
                            finishListener.onFinish(WechatManager.ACTION_SPECIFICED_ERROR, "other");
                        }
                        break;
                    case ResponseHolder.RESPONSE_TYPE_ERROR_TIME_OUT:
                        finishListener.onFinish(WechatManager.ACTION_TIME_OUT, "timeOut");
                        break;
                    case ResponseHolder.RESPONSE_TYPE_ERROR_OTHER:
                        finishListener.onFinish(WechatManager.ACTION_OTHER, "other");
                        break;
                }

            }

        }.start();

    }


}
