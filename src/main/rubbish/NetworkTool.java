package com.powerall.wxfxtools.util.net;

import com.powerall.wxfxtools.util.JsonUtil;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 负责进行联网实际操作的工作
 * Created by larson on 10/02/15.
 */
public class NetworkTool {



    private HttpClient createHttpClient() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);

        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

        return new DefaultHttpClient(conMgr, params);
    }

    public String getLoginResult(String urlStr, String user, String password) throws Exception {
        HttpClient client = createHttpClient();
        HttpPost post = new HttpPost(urlStr);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", user));
        params.add(new BasicNameValuePair("pwd", password));
        params.add(new BasicNameValuePair("imgcode", ""));
        params.add(new BasicNameValuePair("f", "json"));
        post.setEntity(new UrlEncodedFormEntity(params));

        post.setHeader("Content-Type:", "application/x-www-form-urlencoded; charset=UTF-8");
        post.setHeader("Connection", "Keep-Alive");

        HttpResponse response = client.execute(post);
        InputStream is = response.getEntity().getContent();

        String result = new String();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            result += inputLine;
        }

        return result;
    }

    /**
     * 开启登录,模拟微信网页的方式
     *
     * @param account
     * @param password
     * @param loginUrl
     */
    public static void login(String account, String password, String loginUrl) throws IOException {
        URL url = new URL(loginUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        InputStream in = conn.getInputStream();
        OutputStream out = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(out, "UTF-8"));

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", account));
        params.add(new BasicNameValuePair("pwd", password));
        params.add(new BasicNameValuePair("imgcode", ""));
        params.add(new BasicNameValuePair("f", "json"));

        writer.write(getQuery(params));
        writer.flush();

        String str;
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        while ((str = br.readLine()) != null) {
            sb = sb.append(str);
        }
        String result = sb.toString();
        JsonUtil.getRet(result);
        writer.close();
        out.close();
        conn.connect();
    }

    /**
     * post的内容其实是这样的username=yanzhi154249%40163.com&pwd=cfa9e97aaf0325349ed1cbbb28111c8e&imgcode=&f=json
     */
    private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (NameValuePair pair : params) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }
        return result.toString();
    }
}
