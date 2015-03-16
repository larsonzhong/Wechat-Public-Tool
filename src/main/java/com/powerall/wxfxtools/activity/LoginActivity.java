package com.powerall.wxfxtools.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.powerall.wxfxtools.R;
import com.powerall.wxfxtools.model.bean.UserBean;
import com.powerall.wxfxtools.model.holder.ResultHolder;
import com.powerall.wxfxtools.util.DataParser;
import com.powerall.wxfxtools.util.net.NetworkUtil;
import com.powerall.wxfxtools.util.StringUtil;
import com.powerall.wxfxtools.util.Util;
import com.powerall.wxfxtools.util.WechatLoader;

import org.apache.http.Header;

import java.util.Map;


/**
 * 微信分销商家发送消息的工具
 */
public class LoginActivity extends Activity {
    private String account;
    private String pwd;
    private EditText userNameEditText, passWordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();

    }

    private void initView() {
        userNameEditText = (EditText) findViewById(R.id.login_edit_text_user_id);
        passWordEditText = (EditText) findViewById(R.id.login_edit_text_pass_word);
        Button loginButton = (Button) findViewById(R.id.login_button_login);
        loginButton.setOnClickListener(new loginClickListener());

        userNameEditText.setText("yanzhi154249@163.com");//799487815@qq.com
        passWordEditText.setText("yishuo423");//powerallsz
    }


    private static final int INPUT_OK = 0;
    private static final int INPUT_USER_NAME_PROBLEM = 1;
    private static final int INPUT_PASSWORD_PROBLEM = 2;

    private int checkInput() {
        int result = INPUT_OK;
        String userName = userNameEditText.getText().toString();
        if (userName.length() < 1) {
            return INPUT_USER_NAME_PROBLEM;
        }
        String passWord = passWordEditText.getText().toString();
        if (passWord.length() < 1) {
            return INPUT_PASSWORD_PROBLEM;
        }
        return result;
    }

    private class loginClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (NetworkUtil.getNetworkType(getApplicationContext()) != NetworkUtil.NOCONNECTION) {

                int checkResult = checkInput();
                switch (checkResult) {
                    case INPUT_OK:
                        break;
                    case INPUT_USER_NAME_PROBLEM:
                        Toast.makeText(getApplicationContext(), "请输入正确的用户名",
                                Toast.LENGTH_LONG).show();
                        return;
                    case INPUT_PASSWORD_PROBLEM:
                        Toast.makeText(getApplicationContext(), "请输入密码",
                                Toast.LENGTH_LONG).show();
                        return;
                }
                if (!NetworkUtil.isNetConnected(LoginActivity.this)) {
                    Util.dismissDialog();
                    Util.createEnsureDialog(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Util.dismissDialog();
                            Intent intent = new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS);
                            startActivity(intent);
                            finish();
                        }
                    }, true, LoginActivity.this, "网络", "无网络连接，进入设置网络？", true);
                } else {
                    account = userNameEditText.getText().toString();
                    pwd = passWordEditText.getText().toString();
                    pwd = StringUtil.getMD5Str(pwd);
                    login();
                }

            }

        }
    }

    public void login() {
        Util.showLoadingDialog(LoginActivity.this, "正在登录，请稍候", Util.DIALOG_POP_NOT_CANCELABLE);
        new Thread(loginRunnable).start();
    }

    Runnable loginRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Looper.prepare();
                WechatLoader.login(loginCallback, account, pwd, "", "json");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * 处理登录结果的回调
     */
    private WechatLoader.WechatLoginCallBack loginCallback = new WechatLoader.WechatLoginCallBack() {
        @Override
        public void onBack(int resultCode, String strResult, Header[] headers) {
            switch (resultCode) {
                case WechatLoader.WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT:

                    break;
                case WechatLoader.WECHAT_RESULT_MESSAGE_ERROR_OTHER:

                    break;
                case WechatLoader.WECHAT_RESULT_MESSAGE_OK:
                    try {
                        MyApplication.currentUser = new UserBean(account, pwd);
                        DataParser.parseLogin(new DataParser.LoginParseCallBack() {
                            @Override
                            public void onBack(int code, UserBean userBean) {
                                switch (code) {
                                    case DataParser.PARSE_SUCCESS://登录后开始解析用户数据,因为登录只是返回登录结果
                                        WechatLoader.wechatGetUserProfile(profileHandler, MyApplication.currentUser);
                                        break;

                                    case DataParser.PARSE_FAILED:
                                        Looper.prepare();
                                        Toast.makeText(LoginActivity.this, "解析失败", Toast.LENGTH_SHORT).show();
                                        break;
                                    case DataParser.PARSE_ACC_PWD_ERR:
                                        Looper.prepare();
                                        Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }, strResult, headers, MyApplication.currentUser);

                    } catch (Exception exception) {

                    }
                    break;
            }
        }
    };



    /**
     * 解析用户数据的callback
     */
    private Handler profileHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WechatLoader.WECHAT_RESULT_MESSAGE_ERROR_OTHER:
                    closePD();
                    Util.createEnsureDialog(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    closePD();
                                    login();

                                }
                            }, false, LoginActivity.this, "错误", "获取信息失败，重试？", true).show();
                    break;
                case WechatLoader.WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT:
                    closePD();
                    Util.createEnsureDialog(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    closePD();
                                    login();

                                }
                            }, false, LoginActivity.this, "错误", "获取信息超时，重试？", true).show();
                    break;
                case WechatLoader.WECHAT_RESULT_MESSAGE_OK:
                    try {
                        ResultHolder holder = (ResultHolder) msg.obj;
                        String strResult = holder.get("result");
                        String referer = holder.get("referer");
                        int getUserProfileState = DataParser
                                .parseUserProfile(strResult,
                                        MyApplication.currentUser);

                        switch (getUserProfileState) {
                            case DataParser.GET_USER_PROFILE_SUCCESS://成功获取到用户数据，跳转到主界面
                                closePD();
                                Intent jumbIntent = new Intent();
                                jumbIntent.setClass(
                                        LoginActivity.this,
                                        MainActivity.class);
                                startActivity(jumbIntent);
                                finish();
                                break;

                            case DataParser.GET_USER_PROFILE_FAILED:
                                closePD();
                                Util.createEnsureDialog(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    View v) {
                                                // TODO
                                                login();
                                            }
                                        },
                                        false,
                                        LoginActivity.this,
                                        "错误", "登录失败，重试？",
                                        true).show();

                                break;
                        }

                    } catch (Exception exception) {

                        Log.e("get user profile result error",
                                "" + exception);
                    }

                    break;
            }

        }
    };

    private void closePD() {
        Util.dismissDialog();
    }


}
