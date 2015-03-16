package com.powerall.wxfxtools.view.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.powerall.wxfxtools.R;
import com.powerall.wxfxtools.activity.ChatActivity;
import com.powerall.wxfxtools.model.bean.FansBean;

import java.util.ArrayList;

public class FansListAdapter extends BaseAdapter {
    Activity mActivity;
    private ArrayList<FansBean> fansList;

    public FansListAdapter(Activity mActivity, ArrayList<FansBean> fansList) {
        this.mActivity = mActivity;
        this.fansList = fansList;
    }

    @Override
    public int getCount() {
        return fansList.size();
    }

    @Override
    public Object getItem(int position) {
        return fansList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder;
        final FansBean user = fansList.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.fragment_news_qq_item, null);
            holder = new Holder();
            holder.iv = (ImageView) convertView.findViewById(R.id.user_picture);
            holder.tv_name = (TextView) convertView
                    .findViewById(R.id.user_name);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        if (TextUtils.isEmpty(user.getNickname()))
            holder.tv_name.setText(user.getFansId());
        else
            holder.tv_name.setText(user.getNickname());

        //显示头像（不能直接用imageLoader，因为微信安全机制限制，续通通过http请求获取头像）
//        String imgUrl = WechatLoader.WECHAT_URL_GET_MESSAGE_PROFILE_IMG + "?fakeid=" + user.getFansId()+"&token=" + MyApplication.currentUser.getToken() + "&lang=zh_CN";
//        ImageLoader.getInstance().displayImage(imgUrl, holder.iv);

        holder.tv_name.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent jumpIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", user);
                jumpIntent.putExtras(bundle);
                jumpIntent.setClass(mActivity, ChatActivity.class);
                mActivity.startActivity(jumpIntent);
            }
        });

        return convertView;
    }


    class Holder {
        ImageView iv;
        TextView tv_name;
    }
}