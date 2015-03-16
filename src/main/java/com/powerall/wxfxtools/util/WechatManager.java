package com.powerall.wxfxtools.util;

import android.content.Context;
import android.util.Log;

import com.powerall.wxfxtools.activity.MyApplication;
import com.powerall.wxfxtools.model.holder.FansResultHolder;
import com.powerall.wxfxtools.model.bean.MessageBean;
import com.powerall.wxfxtools.model.bean.UserBean;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by larson on 14/02/15.
 */
public class WechatManager {
    public static final int ACTION_SUCCESS = 1;
    public static final int ACTION_TIME_OUT = 2;
    public static final int ACTION_OTHER = 3;
    public static final int ACTION_SPECIFICED_SITUATION = 4;
    public static final int ACTION_SPECIFICED_ERROR = 5;

    private final Context context;
    private int userIndex = 0;//没有用到多用户登录

    public static final int WECHAT_SINGLE_CHAT_OK = 0;
    public static final int CHAT_FANS_NOT_EXIST = -21;
    public static final int WECHAT_SINGLE_CHAT_OUT_OF_DATE = 10706;
    public static final int WECHAT_SINGLE_CHAT_FANS_NOT_RECEIVE = 10703;

    public WechatManager(Context context) {
        this.context = context;
    }


    public interface OnActionFinishListener {
        public void onFinish(int code, Object object);
    }


    public void getFansList(final int page, final String groupId,
                            final WechatManager.OnActionFinishListener onActionFinishListener) {

        WechatLoader.wechatGetFansList(
                new WechatLoader.WechatGetFansCallback() {
                    @Override
                    public void onBack(int resultCode, String strResult, String referer) {
                        // TODO Auto-generated method stub
                        switch (resultCode) {
                            case WechatLoader.WECHAT_RESULT_MESSAGE_OK:

                                boolean refresh = false;
//                                if (page == 0) {
//                                    refresh = true;
//                                }

                                DataParser.parseFansList(strResult, referer,
                                        DataManager.getInstance().getFansHolders().get(userIndex).getCurrentGroupIndex()
                                        , MyApplication.currentUser, refresh,
                                        new DataParser.FansListParseCallback() {
                                            @Override
                                            public void onBack(FansResultHolder fansResultHolder, int code) {
                                                switch (code) {
                                                    case DataParser.PARSE_SUCCESS:

                                                        onActionFinishListener.onFinish(ACTION_SUCCESS, fansResultHolder);
                                                        break;
                                                    case DataParser.PARSE_FAILED:

                                                        onActionFinishListener.onFinish(ACTION_OTHER, null);

                                                        break;
                                                    case DataParser.PARSE_SPECIFIC_ERROR:

                                                        onActionFinishListener.onFinish(ACTION_SPECIFICED_ERROR, null);
                                                        break;

                                                }


                                            }
                                        });
                                break;

                            case WechatLoader.WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT:
                                onActionFinishListener.onFinish(ACTION_TIME_OUT, null);

                                break;

                            case WechatLoader.WECHAT_RESULT_MESSAGE_ERROR_OTHER:

                                onActionFinishListener.onFinish(ACTION_OTHER, null);
                                break;

                        }


                    }
                }, MyApplication.currentUser, groupId, page
        );

    }


    public void singleChat(final UserBean userBean,
                           final MessageBean messageBean,
                           final WechatManager.OnActionFinishListener onActionFinishListener) {

        WechatLoader.wechatChatSingle(
                new WechatLoader.WechatChatSingleCallBack() {

                    @Override
                    public void onBack(int resultCode, String result) {
                        switch (resultCode) {

                            case WechatLoader.WECHAT_RESULT_MESSAGE_OK:

                                try {
                                    Map<String, String> map = JsonUtil.getRet(result);
                                    int ret = Integer.parseInt(map.get("ret"));

                                    if (ret == WECHAT_SINGLE_CHAT_OK) {
                                        messageBean.setSendState(MessageBean.MESSAGE_SEND_OK);
                                        onActionFinishListener.onFinish(ACTION_SUCCESS, true);
                                        return;
                                    } else if (ret == WECHAT_SINGLE_CHAT_OUT_OF_DATE) {

                                        messageBean.setSendState(MessageBean.MESSAGE_SEND_FAILED_LIMIT_OF_TIME);
                                        onActionFinishListener.onFinish(ACTION_SUCCESS, false);
                                        return;
                                    } else if (ret == WECHAT_SINGLE_CHAT_FANS_NOT_RECEIVE) {
                                        messageBean.setSendState(MessageBean.MESSAGE_SEND_FAILED_FANS_NOT_RECEIVE);
                                        onActionFinishListener.onFinish(ACTION_SUCCESS, false);
                                        return;
                                    }else if(ret == CHAT_FANS_NOT_EXIST){
                                        messageBean.setSendState(MessageBean.MESSAGE_SEND_USER_NOT_EXIST);
                                        onActionFinishListener.onFinish(ACTION_OTHER, false);
                                    }

                                } catch (Exception exception) {
                                    Log.e("single chat result parse error", "" + exception);

                                }

                                messageBean.setSendState(MessageBean.MESSAGE_SEND_FAILED_LIMIT_OF_TIME);
                                onActionFinishListener.onFinish(ACTION_OTHER, null);
                                break;

                            case WechatLoader.WECHAT_RESULT_MESSAGE_ERROR_TIMEOUT:
                                onActionFinishListener.onFinish(ACTION_TIME_OUT, null);

                                break;

                            case WechatLoader.WECHAT_RESULT_MESSAGE_ERROR_OTHER:

                                onActionFinishListener.onFinish(ACTION_OTHER, null);
                                break;

                        }


                    }
                }, userBean, messageBean
        );

    }

}
