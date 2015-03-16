package com.powerall.wxfxtools.model.bean;


import com.powerall.wxfxtools.R;

import java.util.ArrayList;
import java.util.List;

public class MessagePlusEntity {

    // 功能名称
    public String name;
    // 功能图标
    public int icon;
    // 功能编号
    public int position;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * 在这里初始化了先
     *
     * @return
     */
    public static List<MessagePlusEntity> getChatFunImgIds() {
        List<MessagePlusEntity> list = new ArrayList<MessagePlusEntity>();
        int[] a = new int[]{R.drawable.chat_fun_image,
                R.drawable.chat_fun_video,
                R.drawable.chat_fun_photo,R.drawable.chat_fun_recorder,R.drawable.chat_fun_text_image};
        String[] str = new String[]{"图片", "视频", "拍照", "摄像", "图文"};
        for (int i = 0; i < str.length; i++) {
            MessagePlusEntity item = new MessagePlusEntity();
            item.icon = a[i];
            item.name = str[i];
            item.position = i;
            list.add(item);
        }
        return list;
    }
}
