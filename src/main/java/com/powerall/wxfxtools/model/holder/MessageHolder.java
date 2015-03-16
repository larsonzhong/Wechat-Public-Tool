package com.powerall.wxfxtools.model.holder;


import com.powerall.wxfxtools.model.bean.MessageBean;
import com.powerall.wxfxtools.model.bean.UserBean;
import com.powerall.wxfxtools.util.net.UploadHelper;

import java.util.ArrayList;

public class MessageHolder {
    public static final int GET_MESSAGE_MODE_ALL = 0;
    public static final int GET_MESSAGE_MODE_TODAY = 1;
    public static final int GET_MESSAGE_MODE_YESTERDAY = 2;
    public static final int GET_MESSAGE_MODE_DAY_BEFORE = 3;
    public static final int GET_MESSAGE_MODE_OLDER = 4;
    public static final int GET_MESSAGE_MODE_STAR = 5;
    public static final int GET_MESSAGE_MODE_SEARCH = 6;


    private String latestMsgId = "";
    private ArrayList<MessageBean> messageBeans;
    private UserBean nowBean;
    private int nowMessageMode = GET_MESSAGE_MODE_ALL;
    private int contentMessageMode = GET_MESSAGE_MODE_ALL;

    private int messageCount = 0;

    private boolean holderEmpty = true;

    /*
    about file upload
     */
    private UploadHelper uploadHelper;


    public MessageHolder(UserBean userBean) {
        nowBean = userBean;
        messageBeans = new ArrayList<>();
        addEmptyMessage();
        uploadHelper = new UploadHelper();

    }

    public void mergeMessageResult(MessageResultHolder messageResultHolder) {
        switch (messageResultHolder.getResultMode()) {
            case MessageResultHolder.RESULT_MODE_REFRESH:
                contentMessageMode = messageResultHolder.getMessageMode();
                messageBeans = messageResultHolder.getMessageBeans();

                break;
            case MessageResultHolder.RESULT_MODE_ADD:
                contentMessageMode = messageResultHolder.getMessageMode();
                addMessage(messageResultHolder.getMessageBeans());

                break;
        }
        latestMsgId = messageResultHolder.getLastMsgId();
        nowBean.setLastMsgId(latestMsgId);

        initMessageCount();
    }

    private void initMessageCount() {

        messageCount = 0;
        for (int i = 0; i < messageBeans.size(); i++) {
            MessageBean nowBean = messageBeans.get(i);
            if (nowBean.getType() != MessageBean.MESSAGE_TYPE_DATA && nowBean.getType() != MessageBean.MESSAGE_TYPE_EMPTY) {
                messageCount++;
            }
        }

    }

    public int getMessageCount() {
        return messageCount;
    }

    public void clearMessage(boolean addEmptyMessage) {
        messageBeans.clear();
        if (addEmptyMessage) {
            addEmptyMessage();
        }

    }

    private void addEmptyMessage() {
        MessageBean emptyMessage = new MessageBean();
        emptyMessage.setType(MessageBean.MESSAGE_TYPE_EMPTY);
        messageBeans.add(emptyMessage);
    }

    public void setNowMessageMode(int mode) {
        nowMessageMode = mode;

    }

    public int getNowMessageMode() {
        return nowMessageMode;
    }


    public void setContentMessageMode(int mode) {
        contentMessageMode = mode;

    }

    public int getContentMessageMode() {
        return contentMessageMode;
    }


    public ArrayList<MessageBean> getMessageList() {
        return messageBeans;
    }

    private void addMessage(ArrayList<MessageBean> nowArrayList) {
        for (int i = 0; i < nowArrayList.size(); i++) {
            messageBeans.add(nowArrayList.get(i));
        }

    }


    public void setLatestMsgId(String id) {
        latestMsgId = id;
    }

    public String getLatestMsgId() {
        return latestMsgId;
    }

    public UserBean getUserBean() {

        return nowBean;
    }

    public boolean getHolderEmpty() {
        return holderEmpty;
    }

    public void setHolderEmpty(boolean holderEmpty) {
        this.holderEmpty = holderEmpty;
    }


    public UploadHelper getUploadHelper() {
        return uploadHelper;
    }

}