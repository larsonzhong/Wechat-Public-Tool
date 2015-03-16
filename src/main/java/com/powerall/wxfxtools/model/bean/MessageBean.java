package com.powerall.wxfxtools.model.bean;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.powerall.wxfxtools.util.DataManager;
import com.powerall.wxfxtools.util.WechatLoader;
import com.powerall.wxfxtools.util.WechatManager;

public class MessageBean {

    public static final int MESSAGE_TYPE_TEXT = 1;
    public static final int MESSAGE_TYPE_IMG = 2;
    public static final int MESSAGE_TYPE_VOICE = 3;
    public static final int MESSAGE_TYPE_VIDEO = 15;

    public static final int MESSAGE_TYPE_EMPTY = 4;
    public static final int MESSAGE_TYPE_DATA = 5;

    private String appId;

    public String getAppId() {
        return appId;
    }
    public void setAppId(String appId) {
        this.appId = appId;
    }

    private String id = "";
    private int type = -1;
    private String fakeid = "";
    /*
    default file id is "0"
     */
    private String fileid = "";

    private String nick_name = "";
    private String date_time = "";
    private String content = "";
    private String source = "";
    private String msg_status = "";
    private String has_reply = "";
    private String refuse_reason = "";
    private String is_starred_msg = "0";

    /*
    about media message
     */
    private String title = "";
    private String desc = "";
    private String content_url = "";

    private String show_type = "";
    private String app_sub_type = "";


    /*
     * about audio
     */
    private String play_length = "0";
    private String length = "0";
    /**
     * ****************************************************************
     */
    private String token = "";
    private String slave_sid = "";
    private String slave_user = "";
    private String referer = "";

    /*
     * about chat
     */
    private String toFakeId = "";
    private int owner = -1;
    private int sendState = -1;

    private DataManager mDataManager;
    private UserBean userBean;
    private String lastMsgId;
    private String createTime;
    public static final int MESSAGE_SEND_NONE = -1;
    //indecate that the message is in send mode
    public static final int MESSAGE_SEND_PREPARE = 0;
    public static final int MESSAGE_SEND_ING = 1;
    public static final int MESSAGE_SEND_OK = 2;
    public static final int MESSAGE_SEND_FAILED_LIMIT_OF_TIME = 3;
    public static final int MESSAGE_SEND_FAILED_FANS_NOT_RECEIVE = 4;
    public static final int MESSAGE_SEND_USER_NOT_EXIST = -21;

    public static final int MESSAGE_OWNER_ME = 1;
    public static final int MESSAGE_OWNER_HER = 2;

    /*
    about chat
     */
    private boolean timeTagShow = false;

    public boolean getTimeTagShow() {
        return timeTagShow;
    }

    public void setTimeTagShow(boolean timeTagShow) {
        this.timeTagShow = timeTagShow;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String data) {
        this.token = data;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String data) {
        this.referer = data;
    }

    public String getSlaveSid() {
        return slave_sid;
    }

    public void setSlaveSid(String data) {
        this.slave_sid = data;
    }

    public String getSlaveUser() {
        return slave_user;
    }

    public void setSlaveUser(String data) {
        this.slave_user = data;
    }

    public boolean getStarred() {
        return is_starred_msg.equals("1");
    }

    public void setStarred(boolean star) {

        is_starred_msg = star ? "1" : "0";
    }

    public String getId() {
        return id;
    }

    public void setId(String data) {
        this.id = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int data) {
        this.type = data;
    }

    public String getFakeId() {
        return fakeid;
    }

    public void setFakeId(String data) {
        this.fakeid = data;
    }

    public String getNickName() {
        return nick_name;
    }

    public void setNickName(String data) {
        this.nick_name = data;
    }

    public String getDateTime() {
        return date_time;
    }

    public void setDateTime(String data) {
        this.date_time = data;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String data) {
        this.content = data;
    }


    public String getSource() {
        return source;
    }

    public void setSource(String data) {
        this.source = data;
    }


    public String getPlayLength() {
        return play_length;
    }

    public void setPlayLength(String playLength) {
        this.play_length = playLength;
    }


    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }


    public String getFileId() {
        return fileid;
    }

    public void setFileId(String data) {
        this.fileid = data;
    }


    public String getMsgStatus() {
        return msg_status;
    }

    public void setMsgStatus(String data) {
        this.msg_status = data;
    }


    public String getHasReply() {
        return has_reply;
    }

    public void setHasReply(String data) {
        this.has_reply = data;
    }


    public String getRefuseReason() {

        return refuse_reason;
    }

    public void setRefuseReason(String data) {

        this.refuse_reason = data;
    }


    public String getTitle() {

        return title;
    }

    public void setTitle(String data) {

        this.title = data;
    }


    public String getContentUrl() {

        return content_url;
    }

    public void setContentUrl(String data) {
        this.content_url = data;
    }

    public String getToFakeId() {
        return toFakeId;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getOwner() {
        return owner;
    }

    private Context mContext;

    public int getSendState() {
        return sendState;
    }

    public void setSendState(int sendState) {
        this.sendState = sendState;
    }

    public void sendMessage(DataManager dataManager, String lastMsgId, UserBean userBean, String toFakeId, Context context) {
        this.toFakeId = toFakeId;
        this.owner = MESSAGE_OWNER_ME;
        this.lastMsgId = lastMsgId;
        this.userBean = userBean;
        this.mDataManager = dataManager;
        this.createTime = (System.currentTimeMillis() + "").substring(0, 10);
        this.mContext = context;

        dataManager.getWechatManager().singleChat(userBean, this, new WechatManager.OnActionFinishListener() {
            @Override
            public void onFinish(int code, Object object) {
                if (code == WechatManager.ACTION_SUCCESS) {
                    Boolean result = (Boolean) object;
                    if (result) {
                        getChatNewItem();
                    } else {
                        if (getSendState() == MESSAGE_SEND_FAILED_FANS_NOT_RECEIVE) {
                            Toast.makeText(mContext, "该用户主动屏蔽了您发送的消息，无法与其聊天", Toast.LENGTH_LONG).show();
                            Log.w("wcMsg", "该用户主动屏蔽了您发送的消息，无法与其聊天");
                        } else if (getSendState() == MESSAGE_SEND_FAILED_LIMIT_OF_TIME) {
                            Toast.makeText(mContext, "该用户48小时未主动给您发送消息，无法与其聊天", Toast.LENGTH_LONG).show();
                            Log.w("wcMsg", "该用户48小时未主动给您发送消息，无法与其聊天");
                        } else if (getSendState() == MESSAGE_SEND_USER_NOT_EXIST) {
                            Log.w("wcMsg", "没有获取到fakeID或指定用户不存在");
                            Toast.makeText(mContext, "没有活取到fakeID或指定用户不存在", Toast.LENGTH_LONG).show();
                        }
                    }
                }

            }
        });

    }

    private void getChatNewItem() {
        WechatLoader.wechatGetChatNewItems(
                new WechatLoader.WechatGetChatNewItems() {
                    @Override
                    public void onBack(int resultCode, String strResult) {

                    }
                }, userBean, this, lastMsgId, createTime, toFakeId
        );


    }


    /**
     *  if the message is file message
     */
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
