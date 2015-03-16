package com.powerall.wxfxtools.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.powerall.wxfxtools.R;
import com.powerall.wxfxtools.model.bean.FansBean;
import com.powerall.wxfxtools.model.bean.MessageBean;
import com.powerall.wxfxtools.model.bean.MessagePlusEntity;
import com.powerall.wxfxtools.util.Configs;
import com.powerall.wxfxtools.util.DataManager;
import com.powerall.wxfxtools.util.SoundMeter;
import com.powerall.wxfxtools.util.StringUtil;
import com.powerall.wxfxtools.view.adapter.FaceGridAdapter;
import com.powerall.wxfxtools.view.adapter.FacePagerAdapter;
import com.powerall.wxfxtools.view.adapter.MessagePlusAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatActivity extends BaseActivity implements View.OnClickListener {

    private Context context;

    private static final int INPUT_OK = 0;
    private static final int INPUT_NONE = 1;
    private static final int INPUT_TOO_LONG = 2;
    private static final int MAX_INPUT_LENGTH = 600;


    private FansBean user;
    private EditText messageInput;


    private ViewPager faceViewPager;
    private RelativeLayout faceRelative;
    private LinearLayout mDotsLayout;
    private ListView listView;
    private boolean btn_voice = false;// 消息类别
    private List<String> staticFacesList;
    // 7列3行
    private int columns = 7;
    private int rows = 3;

    private String voiceName;
    private String videoFilePath;// 发图图片的路径
    private LinearLayout voice_rcd_hint_recording;
    private LinearLayout voice_rcd_hint_loading;
    private LinearLayout voice_rcd_hint_tooShort;
    private ImageView sc_img1;
    private ImageView img1, volume;

    private SoundMeter mSensor;// 录音的类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = this;

        user = (FansBean) getIntent().getSerializableExtra("user");
        init();
    }

    private void init() {
        initStaticFaces();

        // 与谁聊天
        TextView title = (TextView) findViewById(R.id.title_to);
        title.setText(this.user.getNickname());
        Button sendButton = (Button) findViewById(R.id.chat_button_send);
        sendButton.setOnClickListener(this);
        messageInput = (EditText) findViewById(R.id.chat_edit_edit);

        listView = (ListView) findViewById(R.id.chat_layout_list);
        listView.setCacheColorHint(0);
//        adapter = new WXMessageListAdapter(WXChatActivity.this,
//                getinitMessages(), listView, this.user);

        // 头
//        listHead = LayoutInflater.from(context).inflate(R.layout.chatlistheader, null);
//        listHeadButton = (Button)
//                listHead.findViewById(R.id.buttonChatHistory);
//        listHeadButton.setOnClickListener(chatHistoryCk);
//        listView.addHeaderView(listHead);
//        listView.setAdapter(adapter);

        ImageButton chooseFaceBtn = (ImageButton) findViewById(R.id.choose_face);
        chooseFaceBtn.setOnClickListener(this);

        mSensor = new SoundMeter();// 初始化录音器
        volume = (ImageView) this.findViewById(R.id.volume);
        img1 = (ImageView) this.findViewById(R.id.img1);
        sc_img1 = (ImageView) this.findViewById(R.id.sc_img1);
        rcChat_popup = this.findViewById(R.id.rcChat_popup);// 录制动画
        del_re = (LinearLayout) this.findViewById(R.id.del_re);
        voice_rcd_hint_recording = (LinearLayout) this
                .findViewById(R.id.voice_rcd_hint_rcding);
        voice_rcd_hint_loading = (LinearLayout) this
                .findViewById(R.id.voice_rcd_hint_loading);
        voice_rcd_hint_tooShort = (LinearLayout) this
                .findViewById(R.id.voice_rcd_hint_tooshort);

        msgTypeBtn = (ImageButton) findViewById(R.id.msgTypeBtn);
        msgTypeBtn.setOnClickListener(this);


        sendText_ly = (LinearLayout) findViewById(R.id.sendText_ly);

        chat_record = (TextView) findViewById(R.id.chat_record);
        chat_record.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                // 按下语音录制按钮时返回false执行父类OnTouch
                return false;
            }
        });

        faceViewPager = (ViewPager) findViewById(R.id.face_viewpager);
        faceRelative = (RelativeLayout) findViewById(R.id.face_relative);
        mDotsLayout = (LinearLayout) findViewById(R.id.page_dot_linear);
        faceViewPager.setOnPageChangeListener(new PageChangeListener());
        InitViewPager();

        // 加号按钮
        ImageButton btn_plus = (ImageButton) findViewById(R.id.btn_plus);
        btn_plus.setOnClickListener(this);

        fun_relative = (RelativeLayout) findViewById(R.id.fun_relative);
        GridView fun_gradview = (GridView) findViewById(R.id.fun_gradview);
        List<MessagePlusEntity> funIds = MessagePlusEntity.getChatFunImgIds();
        fun_gradview.setAdapter(new MessagePlusAdapter(context, funIds));
        fun_gradview.setOnItemClickListener(gridListener);
    }

    private LinearLayout sendText_ly;
    private TextView chat_record;
    private ImageButton msgTypeBtn;
    private View rcChat_popup;
    private LinearLayout del_re;
    private int flag = 1;
    private boolean isShort = false;
    private Handler mHandler = new Handler();
    private long startVoiceT;

    private void initStaticFaces() {
        try {
            staticFacesList = new ArrayList<>();
            String[] faces = getAssets().list("face/png");
            Collections.addAll(staticFacesList, faces);
            staticFacesList.remove("emotion_del_normal.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getPagerCount() {
        int count = staticFacesList.size();
        return count % (columns * rows - 1) == 0 ? count / (columns * rows - 1)
                : count / (columns * rows - 1) + 1;
    }

    /*
     * 初始表情 *
     */
    private void InitViewPager() {
        int pageCount = getPagerCount();
        if (pageCount < 1) {
            return;
        }
        List<View> views = new ArrayList<>();
        // 获取页数
        for (int i = 0; i < getPagerCount(); i++) {
            views.add(viewPagerItem(i));
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(16, 16);
            // LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
            // LayoutParams.WRAP_CONTENT);
            mDotsLayout.addView(dotsItem(i), params);
        }
        FacePagerAdapter mVpAdapter = new FacePagerAdapter(views);
        faceViewPager.setAdapter(mVpAdapter);
        mDotsLayout.getChildAt(0).setSelected(true);
    }

    private ImageView dotsItem(int position) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.chat_dot_image, null);
        ImageView iv = (ImageView) layout.findViewById(R.id.face_dot);
        iv.setId(position);
        return iv;
    }

    private View viewPagerItem(int position) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        GridView gridview = (GridView) inflater.inflate(R.layout.chat_face_gridview,
                null);
        /**
         * 注：因为每一页末尾都有一个删除图标，所以每一页的实际表情columns *　rows　－　1; 空出最后一个位置给删除图标
         * */
        List<String> subList = new ArrayList<>();
        subList.addAll(staticFacesList
                .subList(position * (columns * rows - 1),
                        (columns * rows - 1) * (position + 1) > staticFacesList
                                .size() ? staticFacesList.size() : (columns
                                * rows - 1)
                                * (position + 1)));
        // 0-20 20-40 40-60 60-80
        /**
         * 末尾添加删除图标
         * */
        subList.add("emotion_del_normal.png");
        FaceGridAdapter mGvAdapter = new FaceGridAdapter(subList, this);
        gridview.setAdapter(mGvAdapter);
        gridview.setNumColumns(columns);
        // 单击表情执行的操作
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                try {
                    String png = (String) ((LinearLayout) view).getChildAt(0)
                            .getTag();
                    if (!png.contains("emotion_del_normal")) {// 如果不是删除图标
                        // input.setText(sb);
                        insert(getFace(png));
                    } else {
                        delete();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return gridview;
    }

    /**
     * 这才是表情逻辑，做一些修改就能和微信接口-----//TODO by Larson
     */
    private SpannableStringBuilder getFace(String png) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        try {
            /**
             * 经过测试，虽然这里tempText被替换为png显示，但是但我单击发送按钮时，获取到輸入框的内容是tempText的值而不是png
             * 所以这里对这个tempText值做特殊处理
             * 格式：#[face/png/f_static_000.png]#，以方便判斷當前圖片是哪一個
             * */
            String tempText = "#[" + png + "]#";
            sb.append(tempText);
            sb.setSpan(
                    new ImageSpan(context, BitmapFactory
                            .decodeStream(getAssets().open(png))), sb.length()
                            - tempText.length(), sb.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb;
    }

    /**
     * 向输入框里添加表情
     */
    private void insert(CharSequence text) {
        int iCursorStart = Selection
                .getSelectionStart((messageInput.getText()));
        int iCursorEnd = Selection.getSelectionEnd((messageInput.getText()));
        if (iCursorStart != iCursorEnd) {
            messageInput.getText().replace(iCursorStart,
                    iCursorEnd, "");
        }
        int iCursor = Selection.getSelectionEnd((messageInput.getText()));
        messageInput.getText().insert(iCursor, text);
    }

    /**
     * 删除图标执行事件
     * 注：如果删除的是表情，在删除时实际删除的是tempText即图片占位的字符串，所以必需一次性删除掉tempText，才能将图片删除
     */
    private void delete() {
        if (messageInput.getText().length() != 0) {
            int iCursorEnd = Selection.getSelectionEnd(messageInput.getText());
            int iCursorStart = Selection.getSelectionStart(messageInput
                    .getText());
            if (iCursorEnd > 0) {
                if (iCursorEnd == iCursorStart) {
                    if (isDeletePng(iCursorEnd)) {
                        String st = "#[face/png/f_static_000.png]#";
                        messageInput.getText().delete(iCursorEnd
                                - st.length(), iCursorEnd);
                    } else {
                        messageInput.getText().delete(
                                iCursorEnd - 1, iCursorEnd);
                    }
                } else {
                    messageInput.getText().delete(iCursorStart,
                            iCursorEnd);
                }
            }
        }
    }

    /**
     * 判断即将删除的字符串是否是图片占位字符串tempText 如果是：则讲删除整个tempText
     * *
     */
    protected boolean isDeletePng(int cursor) {
        String st = "#[face/png/f_static_000.png]#";
        String content = messageInput.getText().toString().substring(0, cursor);
        if (content.length() >= st.length()) {
            String checkStr = content.substring(content.length() - st.length(),
                    content.length());
            String regex = "(#\\[face/png/f_static_)\\d{3}(.png\\]#)";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(checkStr);
            return m.matches();
        }
        return false;
    }

    /**
     * 表情页改变时，dots效果也要跟着改变
     */
    class PageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
                mDotsLayout.getChildAt(i).setSelected(false);
            }
            mDotsLayout.getChildAt(arg0).setSelected(true);
        }

    }

    /**
     * 点击功能
     */
    private AdapterView.OnItemClickListener gridListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            switch (position) {
                case 0:// 添加照片按钮
                    Intent takeintent = new Intent(Intent.ACTION_PICK, null);
                    takeintent
                            .setDataAndType(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    "image/*");
                    startActivityForResult(takeintent,
                            Configs.Code.REQUEST_CODE_ALBMN_IMAGE);
                    break;
                case 2:// 拍照发送

                    Intent intent_photo = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    if (checkMemoryCard()) {
                        startActivityForResult(intent_photo,
                                Configs.Code.REQUEST_CODE_TAKE_PICTURE);
                    }
                    break;
                case 3:// 发送视频
                    showChooseDialog(Configs.Type.VIDEO);
                    break;

            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:
                finish();
                break;

            case R.id.chat_button_send:
                int checkResult = checkInput();
                messageSend(checkResult);
                closeInput();
                break;

            case R.id.choose_face:
                if (faceRelative.getVisibility() == View.VISIBLE) {
                    faceRelative.setVisibility(View.GONE);
                } else {
                    faceRelative.setVisibility(View.VISIBLE);
                }
                if (chat_record.getVisibility() == View.VISIBLE)
                    msgTypeBtn.performClick();
                break;

            case R.id.msgTypeBtn:
                if (btn_voice) {
                    chat_record.setVisibility(View.GONE);
                    sendText_ly.setVisibility(View.VISIBLE);
                    btn_voice = false;
                    msgTypeBtn
                            .setImageResource(R.drawable.chatting_setmode_msg_btn);

                } else {
                    chat_record.setVisibility(View.VISIBLE);
                    sendText_ly.setVisibility(View.GONE);
                    msgTypeBtn
                            .setImageResource(R.drawable.chatting_setmode_voice_btn);
                    btn_voice = true;
                }
                break;
            case R.id.btn_plus:
                if (fun_relative.getVisibility() == View.GONE) {
                    faceRelative.setVisibility(View.GONE);
                    fun_relative.setVisibility(View.VISIBLE);
                } else {
                    fun_relative.setVisibility(View.GONE);
                    fun_relative.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void messageSend(int checkResult) {
        switch (checkResult) {
            case INPUT_NONE:
                Toast.makeText(ChatActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                break;
            case INPUT_OK:
                sendNewMessage(null,MessageBean.MESSAGE_TYPE_TEXT);
                break;
            case INPUT_TOO_LONG:
                Toast.makeText(ChatActivity.this, "输入内容超过字数限制", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    /**
     * 关闭键盘事件
     */
    public void closeInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && this.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 选择本地图片还是拍照上传
     */
    private void showChooseDialog(int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (type == Configs.Type.VIDEO) {
            builder.setTitle("发送视频");
            builder.setItems(new String[]{"即时发送", "本地发送"},
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent;
                            switch (which) {
                                case 0:
                                    Intent intent_video = new Intent(
                                            MediaStore.ACTION_VIDEO_CAPTURE);
                                    intent_video.putExtra(
                                            MediaStore.EXTRA_VIDEO_QUALITY,
                                            Configs.Format.VIDEO_QUALITY);
                                    intent_video.putExtra(
                                            MediaStore.EXTRA_SIZE_LIMIT,
                                            Configs.Format.VIDEO_SIZE_LIMIT);
                                    intent_video.putExtra(
                                            MediaStore.EXTRA_DURATION_LIMIT,
                                            Configs.Format.LIMIT_VIDEO_DURATION);
                                    if (checkMemoryCard()) {
                                        startActivityForResult(
                                                intent_video,
                                                Configs.Code.REQUEST_CODE_TAKE_VIDEO);
                                    }
                                    break;

                                case 1:
                                    intent = new Intent(Intent.ACTION_PICK, null);
                                    intent.setDataAndType(
                                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                            "video/*");
                                    startActivityForResult(intent,
                                            Configs.Code.REQUEST_CODE_ALBMN_VIDEO);
                                    break;
                            }
                        }
                    });
        }
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Configs.Code.REQUEST_CODE_TAKE_PICTURE:// 拍照发送
                    String picName = StringUtil.makeUUIDname();
                    String imageFilePath = Configs.Path.PICTURE_PATH + picName + ".jpg";
                    sendNewMessage(imageFilePath,MessageBean.MESSAGE_TYPE_IMG);
                    break;
                case Configs.Code.REQUEST_CODE_ALBMN_IMAGE:// 调用系统相册
                    Uri uri = data.getData();

                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor imageCursor = getContentResolver().query(uri, proj,
                            null, null, null);
                    if (imageCursor != null) {
                        int column_index = imageCursor
                                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        if (imageCursor.getCount() > 0 && imageCursor.moveToFirst()) {
                            imageFilePath = imageCursor.getString(column_index);
                            sendNewMessage(imageFilePath,MessageBean.MESSAGE_TYPE_IMG);
                        } else
                            showToast("图片未找到");
                    } else {
                        showToast("图片未找到");
                    }
                    break;
                case Configs.Code.REQUEST_CODE_TAKE_VIDEO:// 录像发送
                    Uri videoUri = data.getData();
                    Cursor videoCursor = this.getContentResolver().query(videoUri,
                            null, null, null, null);
                    if (videoCursor != null && videoCursor.moveToNext()) {
                        videoFilePath = videoCursor.getString(videoCursor
                                .getColumnIndex(MediaStore.Video.VideoColumns.DATA));
                        videoCursor.close();
                    }
                    sendNewMessage(videoFilePath,MessageBean.MESSAGE_TYPE_VIDEO);
                    videoFilePath = null;
                    break;
                case Configs.Code.REQUEST_CODE_ALBMN_VIDEO:// 发送视频文件
                    Uri video_uri = data.getData();
                    String[] video_proj = {MediaStore.Video.Media.DATA};
                    Cursor _videoCursor = getContentResolver().query(video_uri,
                            video_proj, null, null, null);
                    if (_videoCursor != null) {
                        int column_index = _videoCursor
                                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                        if (_videoCursor.getCount() > 0
                                && _videoCursor.moveToFirst()) {
                            videoFilePath = _videoCursor.getString(column_index);
                            sendNewMessage(videoFilePath,MessageBean.MESSAGE_TYPE_VIDEO);
                            videoFilePath = null;
                        } else {
                            Toast.makeText(this, "视频未找到", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } else {
                        Toast.makeText(this, "视频未找到", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // 按下语音录制按钮时
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!Environment.getExternalStorageDirectory().exists()) {
            Toast.makeText(this, "No SDCard", Toast.LENGTH_LONG).show();
            return false;
        }

        if (btn_voice) {
            // System.out.println("1");
            int[] location = new int[2];
            chat_record.getLocationInWindow(location); // chat_record获取在当前窗口内的绝对坐标
            int btn_rc_Y = location[1];
            int btn_rc_X = location[0];
            int[] del_location = new int[2];
            del_re.getLocationInWindow(del_location);
            int del_Y = del_location[1];
            int del_x = del_location[0];
            if (event.getAction() == MotionEvent.ACTION_DOWN && flag == 1) {
                if (!checkMemoryCard()) {
                    showToast("No SDCard");
                    return false;
                }
                // System.out.println("2");
                if (event.getY() > btn_rc_Y && event.getX() > btn_rc_X) {// 判断手势按下的位置是否是语音录制按钮的范围内
                    // System.out.println("3");
                    chat_record
                            .setBackgroundResource(R.drawable.voice_rcd_btn_pressed);
                    rcChat_popup.setVisibility(View.VISIBLE);
                    voice_rcd_hint_loading.setVisibility(View.VISIBLE);
                    voice_rcd_hint_recording.setVisibility(View.GONE);
                    voice_rcd_hint_tooShort.setVisibility(View.GONE);
                    mHandler.postDelayed(new Runnable() {

                        public void run() {
                            if (!isShort) {
                                voice_rcd_hint_loading.setVisibility(View.GONE);
                                voice_rcd_hint_recording
                                        .setVisibility(View.VISIBLE);
                            }
                        }
                    }, 300);
                    img1.setVisibility(View.VISIBLE);
                    del_re.setVisibility(View.GONE);
                    startVoiceT = System.currentTimeMillis();
                    voiceName = StringUtil.makeUUIDname()
                            + Configs.Format.AMR;
                    start(voiceName);
                    flag = 2;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP && flag == 2) {// 松开手势时执行录制完成
                // System.out.println("4");
                chat_record.setBackgroundResource(R.drawable.voice_rcd_btn_nor);
                if (event.getY() >= del_Y
                        && event.getY() <= del_Y + del_re.getHeight()
                        && event.getX() >= del_x
                        && event.getX() <= del_x + del_re.getWidth()) {
                    rcChat_popup.setVisibility(View.GONE);
                    img1.setVisibility(View.VISIBLE);
                    del_re.setVisibility(View.GONE);
                    stop();
                    flag = 1;
                    File file = new File(Configs.Path.VOICE_PATH + voiceName);
                    if (file.exists()) {
                        file.delete();
                    }
                } else {

                    voice_rcd_hint_recording.setVisibility(View.GONE);
                    stop();
                    long endVoiceT = System.currentTimeMillis();
                    flag = 1;
                    int time = (int) ((endVoiceT - startVoiceT) / 1000);
                    if (time < 1) {
                        isShort = true;
                        voice_rcd_hint_loading.setVisibility(View.GONE);
                        voice_rcd_hint_recording.setVisibility(View.GONE);
                        voice_rcd_hint_tooShort.setVisibility(View.VISIBLE);
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                voice_rcd_hint_tooShort
                                        .setVisibility(View.GONE);
                                rcChat_popup.setVisibility(View.GONE);
                                isShort = false;
                            }
                        }, 500);
                        return false;
                    }
                    if (!TextUtils.isEmpty(voiceName)) {// 把语音文件发送出去
                        sendNewMessage(Configs.Path.VOICE_PATH + voiceName,MessageBean.MESSAGE_TYPE_VOICE);
                        closeInput();
                    }
                    rcChat_popup.setVisibility(View.GONE);
                }
            }
            if (event.getY() < btn_rc_Y) {// 手势按下的位置不在语音录制按钮的范围内
                // System.out.println("5");
                Animation mLitteAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.chat_record_cancel_rc);
                Animation mBigAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.chat_record_cancel_rc2);
                img1.setVisibility(View.GONE);
                del_re.setVisibility(View.VISIBLE);
                del_re.setBackgroundResource(R.drawable.voice_rcd_cancel_bg);
                if (event.getY() >= del_Y
                        && event.getY() <= del_Y + del_re.getHeight()
                        && event.getX() >= del_x
                        && event.getX() <= del_x + del_re.getWidth()) {
                    del_re.setBackgroundResource(R.drawable.voice_rcd_cancel_bg_focused);
                    sc_img1.startAnimation(mLitteAnimation);
                    sc_img1.startAnimation(mBigAnimation);
                }
            } else {

                img1.setVisibility(View.VISIBLE);
                del_re.setVisibility(View.GONE);
                del_re.setBackgroundResource(0);
            }
        }
        return super.onTouchEvent(event);
    }

    private static final int POLL_INTERVAL = 300;// 声音多少时间检测一次

    private void start(String mVoiceName) {
        mSensor.start(mVoiceName);
        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }

    private Runnable mSleepTask = new Runnable() {
        public void run() {
            stop();
        }
    };

    private void stop() {
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
        mSensor.stop();
        volume.setImageResource(R.drawable.amp1);
    }

    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = mSensor.getAmplitude();
            updateDisplay(amp);
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);

        }
    };
    private RelativeLayout fun_relative;

    private void updateDisplay(double signalEMA) {
        switch ((int) signalEMA) {
            case 0:
            case 1:
                volume.setImageResource(R.drawable.amp1);
                break;
            case 2:
            case 3:
                volume.setImageResource(R.drawable.amp2);
                break;
            case 4:
            case 5:
                volume.setImageResource(R.drawable.amp3);
                break;
            case 6:
            case 7:
                volume.setImageResource(R.drawable.amp4);
                break;
            case 8:
            case 9:
                volume.setImageResource(R.drawable.amp5);
                break;
            case 10:
            case 11:
                volume.setImageResource(R.drawable.amp6);
                break;
            default:
                volume.setImageResource(R.drawable.amp7);
                break;
        }
    }


    private int checkInput() {
        int result = INPUT_OK;
        String inputString = messageInput.getText().toString();
        if (inputString.length() == 0) {
            return INPUT_NONE;
        } else if (inputString.length() >= MAX_INPUT_LENGTH) {
            return INPUT_TOO_LONG;
        }
        return result;
    }

    private void sendNewMessage(String filePath, int msgType) {
        MessageBean message = createMessage(msgType);
        message.setSendState(MessageBean.MESSAGE_SEND_PREPARE);

        message.setSendState(MessageBean.MESSAGE_SEND_ING);
        messageInput.setText("");
        String lastMsgId = "16545646";

        if (!TextUtils.isEmpty(filePath)) {
            message.setFilePath(filePath);
        }
        message.sendMessage(DataManager.getInstance(), lastMsgId, MyApplication.currentUser, user.getFansId(), ChatActivity.this);
    }

    private MessageBean createMessage(int type) {

        MessageBean sendMessage = new MessageBean();
        sendMessage.setType(type);
        sendMessage.setFakeId(MyApplication.currentUser.getFakeId());
        sendMessage.setNickName(MyApplication.currentUser.getNickname());
        String nowTime = (System.currentTimeMillis() + "").substring(0, 10);
        sendMessage.setDateTime(nowTime);

        switch (type){
            case MessageBean.MESSAGE_TYPE_TEXT:
                sendMessage.setContent(messageInput.getText().toString());
                break;
            case MessageBean.MESSAGE_TYPE_IMG:
            case MessageBean.MESSAGE_TYPE_VOICE:
//                String fileID = "204014697";
//                sendMessage.setFileId(fileID);
                break;
            case MessageBean.MESSAGE_TYPE_VIDEO:
//                sendMessage.setAppId("204011131");//TODO 这里是为了测试方便，写的一个已经上传的视频文件
                break;
        }

        return sendMessage;

    }
}
