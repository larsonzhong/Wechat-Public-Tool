package com.powerall.wxfxtools.util.net;

import android.text.TextUtils;
import android.util.Log;

import com.powerall.wxfxtools.activity.MyApplication;
import com.powerall.wxfxtools.model.holder.ResponseHolder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 负责http的get和post请求
 * Created by larson on 12/02/15.
 */
public class HttpUtil {

    public static void loginRedirectGet(String url) {
        try {
            url = url.replace("/cgi-bin", "https://mp.weixin.qq.com/cgi-bin");
            // 参数
            HttpParams httpParameters = new BasicHttpParams();
            // 设置连接超时
            HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
            // 设置socket超时
            HttpConnectionParams.setSoTimeout(httpParameters, 3000);
            // 获取HttpClient对象 （认证）
            DefaultHttpClient hc = initHttpClient(httpParameters);
            HttpGet get = new HttpGet(url);
            //设置请求头
            get = setHttpRequestHeader(get);
            HttpResponse response = hc.execute(get);
            int code = response.getStatusLine().getStatusCode();
            if (response.getStatusLine().getStatusCode() == 200) {
                /**第二次登录（跳转）--写入------cookie-------*/
                List<Cookie> cookies = hc.getCookieStore().getCookies();
                setCookies(cookies);
                String result = EntityUtils.toString(response.getEntity());
                Log.d(code + "---larson---get ", result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HttpGet setHttpRequestHeader(HttpGet get) {
        get.setHeader("Referer", "https://mp.weixin.qq.com/");
        get.setHeader("Content-Type:", "application/x-www-form-urlencoded; charset=UTF-8");
        get.setHeader("Connection", "Keep-Alive");
        get.setHeader("Host", "mp.weixin.qq.com");
        get.setHeader("Origin", "https://mp.weixin.qq.com");
        get.setHeader("X-Requested-With", "XMLHttpRequest");
        get.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.104 Safari/537.36");
        get.setHeader("Cookie", MyApplication.cookies);
        return get;
    }

    public static ResponseHolder httpGet(String targetUrl, ArrayList<NameValuePair> paramList,
                                         ArrayList<NameValuePair> headerArrayList) {

        for (int i = 0; i < paramList.size(); i++) {
            NameValuePair nowPair = paramList.get(i);
            String value = nowPair.getValue();
            try {

                value = URLEncoder.encode(value, "UTF-8");
            } catch (Exception e) {

            }

            if (i == 0) {
                targetUrl += ("?" + nowPair.getName() + "=" + value);
            } else {
                targetUrl += ("&" + nowPair.getName() + "=" + value);
            }
        }


        /* 声明网址字符串 */
        /* 建立HTTP Post联机 */
        HttpGet httpRequest = new HttpGet(targetUrl);
        /*
         * Post运作传送变量必须用NameValuePair[]数组储存
		 */

        try {

            /* 发出HTTP request */

            for (int i = 0; i < headerArrayList.size(); i++) {

                httpRequest.addHeader(headerArrayList.get(i).getName(),
                        headerArrayList.get(i).getValue());

            }
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);
            /* 取得HTTP response */
            HttpClient httpClient = new DefaultHttpClient(httpParams);

            HttpResponse httpResponse = httpClient.execute(httpRequest);

			/* 若状态码为200 ok */
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                /* 取出响应字符串 */

                return new ResponseHolder(ResponseHolder.RESPONSE_TYPE_OK, httpResponse);
            } else {

                Log.e("errorcode", httpResponse.getStatusLine().toString() + "|" + targetUrl + "|" + paramList.toString());
            }
        } catch (ClientProtocolException e) {
            Log.e("wechat loader error", "" + e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException || e instanceof javax.net.ssl.SSLPeerUnverifiedException) {
                return new ResponseHolder(ResponseHolder.RESPONSE_TYPE_ERROR_TIME_OUT, null);
            }

            Log.e("wechat loader error", "" + e);
        } catch (Exception e) {

            Log.e("wechat loader error", "" + e);
            e.printStackTrace();
        }
        return new ResponseHolder(ResponseHolder.RESPONSE_TYPE_ERROR_OTHER, null);

    }


    /**
     * Post请求连接Https服务
     *
     * @return
     * @throws Exception
     */
    public static synchronized ResponseHolder doHttpsPost(String targetUrl, ArrayList<NameValuePair> headerArrayList,
                                                                       ArrayList<NameValuePair> paramsArrayList) {
        try {
            // 参数
            HttpParams httpParameters = new BasicHttpParams();
            // 设置连接超时
            HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
            // 设置socket超时
            HttpConnectionParams.setSoTimeout(httpParameters, 3000);
            // 获取HttpClient对象 （认证）
            DefaultHttpClient httpClient = initHttpClient(httpParameters);
            HttpPost post = new HttpPost(targetUrl);

            post.setEntity(new UrlEncodedFormEntity(paramsArrayList,
                    HTTP.UTF_8));

            // 发送数据类型
            for (int i = 0; i < headerArrayList.size(); i++) {
                post.addHeader(headerArrayList.get(i).getName(),
                        headerArrayList.get(i).getValue());
            }
            // 请求报文
            post.setParams(httpParameters);

            //配置cookie
            HttpContext context = new BasicHttpContext();
            BasicCookieStore cookieStore = new BasicCookieStore();
            context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            HttpResponse httpResponse = httpClient.execute(post);

        /* 若状态码为200 ok */
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                /**第一次登录--写入------cookie-------*/
                List<Cookie> cookies = httpClient.getCookieStore().getCookies();
                setCookies(cookies);

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

    /**
     * 保存cookie
     *
     * @param cookies
     */
    private static void setCookies(List<Cookie> cookies) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cookies.size(); i++) {
            Cookie cookie = cookies.get(i);
            String cookieName = cookie.getName();
            String cookieValue = cookie.getValue();
            if (!TextUtils.isEmpty(cookieName)
                    && !TextUtils.isEmpty(cookieValue)) {
                sb.append(cookieName + "=");
                sb.append(cookieValue + ";");
            }
        }
        MyApplication.cookies = sb.toString();
    }

    private static DefaultHttpClient client = null;

    /**
     * 初始化HttpClient对象
     *
     * @param params
     * @return
     */
    public static synchronized DefaultHttpClient initHttpClient(HttpParams params) {
        if (client == null) {
            try {
                //设置信任机制
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);

                SSLSocketFactory sf = new SSLSocketFactoryImp(trustStore);
                //允许所有主机的验证
                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
                // 设置http和https支持
                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                registry.register(new Scheme("https", sf, 443));

                ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

                client = new DefaultHttpClient(ccm, params);
            } catch (Exception e) {
                e.printStackTrace();
                client = new DefaultHttpClient(params);
            }
        }
        return client;
    }

    private static class SSLSocketFactoryImp extends SSLSocketFactory {
        final SSLContext sslContext = SSLContext.getInstance("TLS");

        public SSLSocketFactoryImp(KeyStore truststore)
                throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType)
                        throws java.security.cert.CertificateException {
                }
                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType)
                        throws java.security.cert.CertificateException {
                }
            };
            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port,
                                   boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host,
                    port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

}
