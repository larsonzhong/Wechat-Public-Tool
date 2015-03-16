package com.powerall.wxfxtools.util;

import com.powerall.wxfxtools.activity.MyApplication;
import com.powerall.wxfxtools.model.holder.FansHolder;
import com.powerall.wxfxtools.model.holder.FansResultHolder;
import com.powerall.wxfxtools.model.holder.MessageHolder;

import java.util.ArrayList;

/**
 * Created by larson on 11/02/15.
 */
public class DataManager {
    private WechatManager mWechatManager;
    private static DataManager dataManager;
    ArrayList<FansListChangeListener> fansListChangeListeners;
    private ArrayList<FansHolder> fansHolders;
    private ArrayList<MessageHolder> messageHolders;


    public static DataManager getInstance() {
        if (dataManager == null)
            dataManager = new DataManager();
        return dataManager;
    }

    private DataManager() {
        mWechatManager = new WechatManager(MyApplication.getInstance());
        fansListChangeListeners = new ArrayList<>();
        messageHolders = new ArrayList<>();
        fansHolders = new ArrayList<>();
        fansHolders.add(new FansHolder(MyApplication.currentUser));//fansHolder初始化
        messageHolders.add(new MessageHolder(MyApplication.currentUser));//MessageHolder的初始化
    }

    public ArrayList<MessageHolder> getMessageHolders() {
        return messageHolders;
    }

    public WechatManager getWechatManager() {
        return mWechatManager;
    }


    public interface FansListChangeListener {
        public void onFansGeted(FansResultHolder fansResultHolder);
    }

    public void addFansListChangeListener(FansListChangeListener fansListChangeListener) {
        this.fansListChangeListeners.add(fansListChangeListener);
    }


    public ArrayList<FansHolder> getFansHolders() {
        return fansHolders;
    }

    public FansHolder getCurrentFansHolder() {
        return fansHolders.get(0);
    }


}
