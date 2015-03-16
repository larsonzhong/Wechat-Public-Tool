package com.powerall.wxfxtools.util;

import android.os.Environment;

/**
 * Created by larson on 02/03/15.
 */
public class Configs {

    public static final class Path {

        private static final String APP_PATH = Environment
                .getExternalStorageDirectory().getAbsolutePath()
                + "/Android/data/com.mychat/";
        public static final String VOICE_PATH = APP_PATH + "amr/";
        public static final String PICTURE_PATH = APP_PATH + "img/";
        public static final String FILE_PATH = APP_PATH + "file/";
        public static final String VIDEO_PATH = APP_PATH + "video/";
        public static final String AVATAR_PATH = FILE_PATH;
        public static final String LOG_PATH = FILE_PATH + "logs/";
        public static final String DOWNLOAD_APK_PATH = FILE_PATH + "apk/";
    }


    public static final class Code {
        public static final int REQUEST_CODE_TAKE_PICTURE = 200;
        public static final int REQUEST_CODE_ALBMN_IMAGE = 201;
        public static final int REQUEST_CODE_POSITION = 202;

        public static final int FILE_TRANSFER_ERROR = 301;
        //		public static final int FILE_TRANSFER_COMPLETE = 302;
        public static final int FILE_TRANSFER_CANCEL = 303;
        public static final int REQUEST_CODE_TAKE_VIDEO = 501;
        public static final int REQUEST_CODE_ALBMN_VIDEO = 502;
        public static final int REQUEST_CODE_CONTACT = 503;

    }

    /**
     * 文件类型
     *
     * @author larson
     */
    public static final class Type {
        public static final int PHOTO = 505;
        public static final int VIDEO = 506;
        public static final int RECORD = 507;
    }

    /**
     * 录制/拍摄参数
     */
    public static final class Format {
        public static final String AMR = ".amr";
        public static final String LIMIT_VIDEO_DURATION = "300"; // 发送的视频最长不超过5分钟
        public static final double VIDEO_QUALITY = 0.5;// 视频质量
        public static final double VIDEO_SIZE_LIMIT = 480 * 800;// 分辨率
    }

    public class Url {
        public static final String WECHAT_URL_LOGIN = "https://mp.weixin.qq.com/cgi-bin/login";//登录
        public static final String WECHAT_URL_GET_USER_PROFILE = "https://mp.weixin.qq.com/cgi-bin/home";//获取用户信息
        public static final String WECHAT_URL_GET_FANS_LIST = "https://mp.weixin.qq.com/cgi-bin/contactmanage";//获取粉丝用户
        public static final String WECHAT_URL_CHAT_SINGLE_SEND = "https://mp.weixin.qq.com/cgi-bin/singlesend";//发送消息
        public static final String WECHAT_URL_GET_CHAT_NEW_ITEM = "https://mp.weixin.qq.com/cgi-bin/singlesendpage";//获取发送的消息
        public static final String WECHAT_URL_GET_MESSAGE_PROFILE_IMG = "https://mp.weixin.qq.com/misc/getheadimg";//获取用户头像


        /**
         * https://mp.weixin.qq.com/cgi-bin/filetransfer?
         * action=upload_material&f=json&writetype=doublewrite&groupid=1
         * &ticket_id=gh_981e344c0838&ticket=1e1ab372464e64408ec5f272650b45c6a0a1fbb3&token=201345845&lang=zh_CN
         */
        public static final String WECHAT_URL_UPLOAD_FILE = "https://mp.weixin.qq.com/cgi-bin/filetransfer";//上传文件的Url
        public static final String WECHAT_URL_GET_UPLOAD_INFO = "https://mp.weixin.qq.com/cgi-bin/filepage";
    }
}
