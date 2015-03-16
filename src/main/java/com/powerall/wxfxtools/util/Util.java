package com.powerall.wxfxtools.util;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.powerall.wxfxtools.R;
import com.powerall.wxfxtools.activity.MyApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by larson on 11/02/15.
 */
public class Util {
    public static final int DIALOG_POP_NOT_CANCELABLE = 3;
    public static final int DIALOG_POP_CANCELABLE = 2;
    public static final int DIALOG_POP_NO = 1;


    private static List<Dialog> dialogList = new ArrayList<>();


    public static void dismissDialog() {
        if (dialogList != null) {
            for (Dialog dialog : dialogList) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
            }
        }
    }

    public static Dialog createEnsureDialog(
            View.OnClickListener sureOnClickListener, boolean cancelVisible,
            Context context, String titleText, String contentText, boolean cancelable) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_ensure_layout, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        Button sureButton = (Button) v
                .findViewById(R.id.dialog_ensure_button_sure);

        RelativeLayout cancelLayout = (RelativeLayout) v
                .findViewById(R.id.dialog_ensure_layout_cancel);

        sureButton.setOnClickListener(sureOnClickListener);

        TextView titleTextView = (TextView) v
                .findViewById(R.id.dialog_ensure_text_title);
        titleTextView.setText("" + titleText);

        TextView contentTextView = (TextView) v.findViewById(R.id.dialog_ensure_text_content);
        contentTextView.setText("" + contentText);

        final Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        if (cancelVisible) {

            Button cancelButton = (Button) v
                    .findViewById(R.id.dialog_ensure_button_cancel);
            cancelButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    loadingDialog.dismiss();

                }
            });
        } else {
            cancelLayout.setVisibility(View.GONE);

        }
        loadingDialog.setCancelable(cancelable);// 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));// 设置布局

        dialogList.add(loadingDialog);
        return loadingDialog;
    }


    public static void createLoginDialog(Context context, String title, View.OnClickListener sureClickListener, View.OnClickListener cancelClickListener) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_login_layout,
                null);
        TextView popTitleTextView = (TextView) dialogView
                .findViewById(R.id.dialog_login_text_title);

        Button popSureButton = (Button) dialogView
                .findViewById(R.id.dialog_login_button_sure);
        Button popCancelButton = (Button) dialogView
                .findViewById(R.id.dialog_login_button_cancel);


        EditText loginUserEdit = (EditText) dialogView.findViewById(R.id.dialog_login_edit_user_id);
        EditText loginPwdEdit = (EditText) dialogView.findViewById(R.id.dialog_login_edit_pass_word);

        String userId = loginUserEdit.getText().toString();
        String pwd = StringUtil
                .getMD5Str(loginPwdEdit.getText().toString());
        MyApplication.currentUser.setUserName(userId);
        MyApplication.currentUser.setPwd(pwd);

        popTitleTextView.setText(title);
        popSureButton.setOnClickListener(sureClickListener);
        popCancelButton.setOnClickListener(cancelClickListener);

        Dialog popDialog = new Dialog(context, R.style.dialog);
        popDialog.setContentView(dialogView);
        dialogList.add(popDialog);
    }

    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @return
     */
    public static void showLoadingDialog(Context context,
                                         String loadingText, int dialogCancelType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ImageView outerImg = (ImageView) v.findViewById(R.id.loading_img_outer);
        ImageView innerImg = (ImageView) v.findViewById(R.id.loading_img_inner);
        // 加载动画

        Animation outerRotateAnimation = new RotateAnimation(0, 360,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        outerRotateAnimation.setRepeatCount(-1);
        outerRotateAnimation.setDuration(1000);
        outerRotateAnimation.setInterpolator(new LinearInterpolator());
        Animation innerRotateAnimation = new RotateAnimation(360, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        innerRotateAnimation.setRepeatCount(-1);
        innerRotateAnimation.setDuration(1300);
        innerRotateAnimation.setInterpolator(new LinearInterpolator());
        // 使用ImageView显示动画
        outerImg.startAnimation(outerRotateAnimation);
        innerImg.startAnimation(innerRotateAnimation);

        TextView loadingTextView = (TextView) v.findViewById(R.id.loading_text);
        loadingTextView.setText("" + loadingText);

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        switch (dialogCancelType) {
            case DIALOG_POP_CANCELABLE:

                loadingDialog.setCancelable(true);// 不可以用“返回键”取消
                break;

            case DIALOG_POP_NOT_CANCELABLE:

                loadingDialog.setCancelable(false);
                break;
        }

        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));// 设置布局
        loadingDialog.show();
        dialogList.add(loadingDialog);
    }


    public static String getRandomFloat(int bit) {
        String result = "0.";
        for (int i = 0; i < bit; i++) {
            int nowInt = (int) (10 * Math.random());
            if (nowInt == 10) {
                nowInt = 0;
            }
            result += nowInt;
        }

        return result;
    }
}
