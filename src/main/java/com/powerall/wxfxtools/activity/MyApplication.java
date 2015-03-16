package com.powerall.wxfxtools.activity;

import android.app.Activity;
import android.app.Application;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.powerall.wxfxtools.model.bean.UserBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by larson on 13/02/15.
 */
public class MyApplication extends Application {
    public static String cookies;
    public static UserBean currentUser;

    private static MyApplication application;
    private List<Activity> activities;


    public static MyApplication getInstance() {
        if (application == null)
            application = new MyApplication();
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        configImageLoader();
        activities = new ArrayList<>();
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    // 遍历所有Activity并finish
    public void exit() {
        for (Activity activity : activities) {
            activity.finish();
        }
    }

    /**
     * 初始化imageloader,自定义缓存路径，默认是包名下的cache下
     */
    private void configImageLoader() {
        File cacheDir = StorageUtils.getOwnCacheDirectory(this, "img/cache");
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this)
                .memoryCacheExtraOptions(480, 800)
                        // maxwidth, max height，即保存的每个缓存文件的最大长宽
                .threadPoolSize(3)
                        // 线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
                        // You can pass your own memory cache
                        // implementation/你可以通过自己的内存缓存实现
                .memoryCacheSize(2 * 1024 * 1024)
                .discCacheSize(50 * 1024 * 1024)
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                        // 将保存的时候的URI名称用MD5 加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .discCacheFileCount(100)
                        // 缓存的文件数量
                .discCache(new UnlimitedDiscCache(cacheDir))
                        // 自定义缓存路径
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .imageDownloader(
                        new BaseImageDownloader(this, 5 * 1000, 30 * 1000)) // connectTimeout
                        // (5
                        // s),
                        // readTimeout
                        // (30
                        // s)超时时间
                .writeDebugLogs() // Remove for releaseapp
                .build();// 开始构建
        ImageLoader.getInstance().init(config);
        ImageLoader.getInstance().init(config);
    }
}
