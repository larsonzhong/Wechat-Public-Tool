package com.powerall.wxfxtools.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.powerall.wxfxtools.R;
import com.powerall.wxfxtools.model.bean.FansBean;
import com.powerall.wxfxtools.model.holder.FansResultHolder;
import com.powerall.wxfxtools.util.DataManager;
import com.powerall.wxfxtools.util.WechatManager;
import com.powerall.wxfxtools.view.CustomListView;
import com.powerall.wxfxtools.view.adapter.FansListAdapter;

import java.util.ArrayList;


public class MainActivity extends Activity {
    private static final int PAGE_FANS = 10;
    private ArrayList<FansBean> fans = new ArrayList<>();
    private FansListAdapter adapter;
    private CustomListView ptrListView;
    private int FANS_LOAD_COMPLETE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        getFansList();
    }

    private void initView() {
        ptrListView = (CustomListView) findViewById(R.id.fans_list);
        adapter = new FansListAdapter(MainActivity.this, fans);
        ptrListView.setAdapter(adapter);

        ptrListView.setOnRefreshListener(new CustomListView.OnRefreshListener() {
            @Override//下拉刷新
            public void onRefresh() {
                new GetDataTask(GetDataTask.PTR_MODE_REFRESH).execute();
            }
        });

        ptrListView.setCanLoadMore(false);//在已经有数据的时候才能看到加载更多按钮
        ptrListView.setAutoLoadMore(true);//下拉的时候自动加载
        ptrListView.setOnLoadListener(new CustomListView.OnLoadMoreListener() {
            @Override//加载更多
            public void onLoadMore() {
                new GetDataTask(GetDataTask.PTR_MODE_LOAD).execute();
            }
        });

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == FANS_LOAD_COMPLETE) {
                adapter = new FansListAdapter(MainActivity.this, fans);
                ptrListView.setAdapter(adapter);
                ptrListView.onLoadMoreComplete();
                adapter.notifyDataSetChanged();
                ptrListView.setCanLoadMore(true);
            }
        }
    };

    /**
     * 打开获取粉丝页面
     */
    public void getFansList() {
        new GetDataTask(GetDataTask.PTR_MODE_LOAD).execute();
    }

    private class GetDataTask extends AsyncTask<Void, Void, Void> {

        private boolean end = false;
        private int mode;

        public static final int PTR_MODE_REFRESH = 2;
        public static final int PTR_MODE_LOAD = 3;

        public GetDataTask(int mode) {
            this.mode = mode;
            end = false;
            if (DataManager.getInstance().getCurrentFansHolder() == null) {
                end = true;
                Log.d("currentFansHolder", "空对象导致无法加载");
                return;
            }

            try {
                if (mode == PTR_MODE_LOAD) {
                    if (DataManager.getInstance().getCurrentFansHolder().getFansCount()
                            % PAGE_FANS != 0) {
                        end = true;
                    } else {
                        int page = DataManager.getInstance().getCurrentFansHolder()
                                .getFansBeans().size() / 10;

                        DataManager.getInstance().getWechatManager().getFansList(page, DataManager.getInstance().getCurrentFansHolder().getCurrentGroupId() + "",
                                new WechatManager.OnActionFinishListener() {

                                    @Override
                                    public void onFinish(int code, Object object) {
                                        switch (code) {
                                            case WechatManager.ACTION_SUCCESS://已经获取到粉丝的基本资料（id，name）
                                                FansResultHolder fansResultHolder = (FansResultHolder) object;
                                                if (fansResultHolder == null) {
                                                    Looper.prepare();
                                                    Toast.makeText(MainActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                DataManager.getInstance().getCurrentFansHolder().mergeFansResult(fansResultHolder);//添加到fansHolder
                                                fans = DataManager.getInstance().getCurrentFansHolder().getFansBeans();//更新当前所有fans
                                                System.out.print("fans size is " + fans.size());
                                                mHandler.sendEmptyMessage(FANS_LOAD_COMPLETE);
                                                break;
                                            case WechatManager.ACTION_TIME_OUT:

                                                break;
                                            case WechatManager.ACTION_OTHER:

                                                break;
                                            case WechatManager.ACTION_SPECIFICED_ERROR:


                                                break;
                                        }

                                        end = true;

                                    }
                                });

                    }
                } else if (mode == PTR_MODE_REFRESH) {
                    DataManager.getInstance().getWechatManager().getFansList(0, DataManager.getInstance().getCurrentFansHolder().getCurrentGroupId() + "",
                            new WechatManager.OnActionFinishListener() {

                                @Override
                                public void onFinish(int code, Object object) {
                                    // TODO Auto-generated method stub
                                    switch (code) {
                                        case WechatManager.ACTION_SUCCESS:
                                            Looper.prepare();
                                            Toast.makeText(MainActivity.this, "这是刷新粉丝列表操作，暂时不做", Toast.LENGTH_SHORT).show();
//这是刷新粉丝列表操作，暂时不做
//                                            DataManager.getInstance().doFansGeted((FansResultHolder) object);

                                            break;
                                        case WechatManager.ACTION_TIME_OUT:

                                            break;
                                        case WechatManager.ACTION_OTHER:

                                            break;
                                        case WechatManager.ACTION_SPECIFICED_ERROR:

                                            break;
                                    }

                                    end = true;

                                }
                            });

                }
            } catch (Exception e) {

            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Simulates a background job.
            try {
                while (!end) {
                    Thread.sleep(50);
                }
            } catch (Exception exception) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
            switch (mode) {
                case PTR_MODE_LOAD:

                    break;
                case PTR_MODE_REFRESH:

                    break;
            }

        }
    }


}

