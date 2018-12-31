package com.tehike.mst.client.project.ui.landactivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.adapters.ChatMsgViewAdapter;
import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.db.DbHelper;
import com.tehike.mst.client.project.entity.ChatMsgEntity;
import com.tehike.mst.client.project.entity.SipBean;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.linphone.Linphone;
import com.tehike.mst.client.project.linphone.MessageCallback;
import com.tehike.mst.client.project.linphone.SipManager;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.utils.ActivityUtils;
import com.tehike.mst.client.project.utils.CryptoUtil;
import com.tehike.mst.client.project.utils.FileUtil;
import com.tehike.mst.client.project.utils.GsonUtils;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.TimeUtils;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 描述：横屏的聊天页面
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/10/26 16:42
 */

public class LandChatActivity extends BaseActivity {

    /**
     * 消息输入框
     */
    @BindView(R.id.sendmessage_layout)
    EditText mEditContentLayout;

    /**
     * 历史消息布局
     */
    @BindView(R.id.message_listview_layout)
    ListView disPlayChatHistoryView;

    /**
     * 显示当前的聊天对象布局
     */
    @BindView(R.id.current_fragment_name)
    TextView disPlayCurrentChatNameLayout;

    /**
     * 电量信息
     */
    @BindView(R.id.icon_electritity_show)
    ImageView disPlayBatteryIconLayout;

    /**
     * 信号强度
     */
    @BindView(R.id.icon_network)
    ImageView disPlayRssiIconLayout;

    /**
     * 显示当前时间分秒
     */
    @BindView(R.id.sipinfor_title_time_layout)
    TextView disPlayCurrentTimeLayout;

    /**
     * 显示当前的年月日
     */
    @BindView(R.id.sipinfor_title_date_layout)
    TextView disPlayCurrentYearLayout;

    /**
     * 消息图标
     */
    @BindView(R.id.icon_message_show)
    ImageView disPlayMessageIconLayout;

    /**
     * 连接状态
     */
    @BindView(R.id.icon_connection_show)
    ImageView disPlaySipConnetIconLayout;

    /**
     * 聊天对象
     */
    String remoteChatNumber = "";

    /**
     * 聊天对象的设备名
     */
    String remoteChatName = "";


    /**
     * Linphone聊天对象的地址
     */
    LinphoneAddress linphoneAddress;

    /**
     * 消息适配器
     */
    private ChatMsgViewAdapter mAdapter;

    /**
     * 盛放消息的集合容器
     */
    private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();

    /**
     * 数据库对象
     */
    SQLiteDatabase db;

    /**
     * 本机的号码
     */
    String nativeSipNumber = "";

    /**
     * 线程是否正在运行
     */
    boolean threadIsRun = true;


    List<SipBean> allSipSourceList = null;


    @Override
    protected int intiLayout() {
        return R.layout.activity_land_chat;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //显示时间
        initializeTime();

        //初始化参数
        initParamater();

        //使未读消息变为已读
        initMessRead();

        //加载历史记录
        getAllHistory();

        //适配加载数据
        initAdapter();

    }

    /**
     * 显示时间的线程
     */
    class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Logutil.i("Thread error:" + e.getMessage());
                }
                handler.sendEmptyMessage(8);
            } while (threadIsRun);
        }
    }

    /**
     * 显示当前的时间
     */
    private void displayCurrentTime() {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat timeD = new SimpleDateFormat("HH:mm:ss");
        String currentTime = timeD.format(date).toString();
        if (!TextUtils.isEmpty(currentTime)) {
            disPlayCurrentTimeLayout.setText(currentTime);
        }
    }

    /**
     * 初始化显示时间及日期
     */
    private void initializeTime() {
        TimeThread timeThread = new TimeThread();
        new Thread(timeThread).start();

        SimpleDateFormat dateD = new SimpleDateFormat("yyyy年MM月dd日");
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        disPlayCurrentYearLayout.setText(dateD.format(date).toString());
    }


    /**
     * 使所有的未读消息变为已读
     */
    private void initMessRead() {
        if (SipService.isReady() || SipManager.isInstanceiated()) {
            LinphoneChatRoom[] rooms = SipManager.getLc().getChatRooms();
            if (rooms.length > 0) {
                for (LinphoneChatRoom room : rooms) {
                    if (room.getPeerAddress().getUserName().equals(remoteChatNumber)) {
                        room.markAsRead();
                    }
                }
            }
        }
    }

    /**
     * 初始数据
     */
    private void initParamater() {

        //获取本机的sip号码
        nativeSipNumber = SysinfoUtils.getSysinfo().getSipUsername();

        //获取当前对话列表点击 的用户名
        SipBean sipClient = (SipBean) getIntent().getExtras().getSerializable("sipclient");

        //取出本地所有有Sip资源
        try {
            String videoSourceStr = FileUtil.readFile(AppConfig.SOURCES_SIP).toString();
            allSipSourceList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(videoSourceStr), SipBean.class);
        } catch (Exception e) {
        }

        if (sipClient != null) {
            remoteChatNumber = sipClient.getNumber();

            if (allSipSourceList != null && allSipSourceList.size() > 0) {
                for (SipBean device : allSipSourceList) {
                    if (device.getNumber().equals(remoteChatNumber)) {
                        remoteChatName = device.getName();
                        disPlayCurrentChatNameLayout.setText(device.getName());
                    }
                }
                String sipserver = SysinfoUtils.getSysinfo().getSipServer();
                if (!TextUtils.isEmpty(sipserver)) {
                    try {
                        linphoneAddress = LinphoneCoreFactory.instance().createLinphoneAddress("sip:" + remoteChatNumber + "@" + sipserver);
                    } catch (LinphoneCoreException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Logutil.e("No Get Chat Object!!!");
                return;
            }
        }
    }


    private void initAdapter() {
        //初始化适配器
        mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
        disPlayChatHistoryView.setAdapter(mAdapter);
        disPlayChatHistoryView.setSelection(disPlayChatHistoryView.getCount());
    }


    /**
     * 消息回调
     */
    private void initMessReceiverCall() {
        if (SipService.isReady() || SipManager.isInstanceiated()) {
            SipService.addMessageCallback(new MessageCallback() {
                @Override
                public void receiverMessage(LinphoneChatMessage linphoneChatMessage) {
                    ChatMsgEntity chatMsgEntity = new ChatMsgEntity();

                    //显示聊天对象的设备名
                    if (allSipSourceList != null && allSipSourceList.size() > 0) {
                        for (SipBean device : allSipSourceList) {
                            if (device.getNumber().equals(remoteChatNumber)) {
                                chatMsgEntity.setName(device.getName());
                            }
                        }
                    }
                    chatMsgEntity.setDate(TimeUtils.longTime2Short(new Date().toString()));
                    chatMsgEntity.setMsgType(true);
                    chatMsgEntity.setText(linphoneChatMessage.getText());
                    mDataArrays.add(chatMsgEntity);
                    mAdapter.notifyDataSetChanged();
                    mEditContentLayout.setText("");
                    disPlayChatHistoryView.setSelection(disPlayChatHistoryView.getCount() - 1);
                }
            });
        }
    }

    /**
     * 取出所有的聊天记录
     */
    private void getAllHistory() {

        DbHelper dbHelper = new DbHelper(App.getApplication());
        db = dbHelper.getWritableDatabase();

        //根据条件查询聊天记录
        Cursor cursor = db.query("chatHistory", null, "fromuser =? or touser = ?", new String[]{remoteChatNumber, remoteChatNumber}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String fromuser = cursor.getString(cursor.getColumnIndex("fromUser"));
                String message = cursor.getString(cursor.getColumnIndex("mess"));
                String toUser = cursor.getString(cursor.getColumnIndex("toUser"));
                if (toUser.equals(remoteChatNumber)) {
                    ChatMsgEntity mEntity = new ChatMsgEntity();
                    mEntity.setDate(TimeUtils.longTime2Short(time));
                    if (allSipSourceList != null && allSipSourceList.size() > 0) {
                        for (SipBean device : allSipSourceList) {
                            if (device.getNumber().equals(fromuser)) {
                                mEntity.setName(device.getName());
                            }
                        }
                    }
                    mEntity.setMsgType(false);
                    mEntity.setText(message);
                    mDataArrays.add(mEntity);
                } else if (fromuser.equals(remoteChatNumber)) {
                    ChatMsgEntity tEntity = new ChatMsgEntity();
                    tEntity.setDate(TimeUtils.longTime2Short(time));
                    if (allSipSourceList != null && allSipSourceList.size() > 0) {
                        for (SipBean device : allSipSourceList) {
                            if (device.getNumber().equals(fromuser)) {
                                tEntity.setName(device.getName());
                            }
                        }
                    }
                    tEntity.setMsgType(true);
                    tEntity.setText(message);
                    mDataArrays.add(tEntity);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * 发消息
     */

    @OnClick(R.id.send_message_btn_layout)
    public void sendMess(View view) {
        String chatMessage = mEditContentLayout.getText().toString().trim();
        if (!TextUtils.isEmpty(chatMessage) && chatMessage.length() > 0) {
            //送消息的展示界面
            ChatMsgEntity entity = new ChatMsgEntity();
            entity.setText(chatMessage);
            entity.setMsgType(false);

            if (allSipSourceList != null && allSipSourceList.size() > 0) {
                for (SipBean device : allSipSourceList) {
                    if (device.getNumber().equals(nativeSipNumber)) {
                        entity.setName(device.getName());
                        break;
                    }
                }
            }
            entity.setDate(getDate());
            mDataArrays.add(entity);
            mAdapter.notifyDataSetChanged();
            mEditContentLayout.setText("");
            disPlayChatHistoryView.setSelection(disPlayChatHistoryView.getCount() - 1);
            //（发送sip短消息到对方）
            if (SipService.isReady())
                Linphone.getLC().getChatRoom(linphoneAddress).sendMessage(chatMessage);

            //把发的消息插入到数据库
            ContentValues contentValues = new ContentValues();
            contentValues.put("time", new Date().toString());
            contentValues.put("fromUser", nativeSipNumber);
            contentValues.put("mess", chatMessage);
            contentValues.put("toUser", remoteChatNumber);
            db.insert("chatHistory", null, contentValues);
        }
    }

    /**
     * 当前时间记录
     */
    private String getDate() {
        String time = new Date().toString();
        return TimeUtils.longTime2Short(time);
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        //当前页面的消息回调
        initMessReceiverCall();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initMessReceiverCall();

        disPlayAppStatusIcon();


    }


    private void disPlayAppStatusIcon() {

        int level = AppConfig.DEVICE_BATTERY;
        if (level >= 75 && level <= 100) {
            updateUi(disPlayBatteryIconLayout, R.mipmap.icon_electricity_a);
        }
        if (level >= 50 && level < 75) {
            updateUi(disPlayBatteryIconLayout, R.mipmap.icon_electricity_b);
        }
        if (level >= 25 && level < 50) {
            updateUi(disPlayBatteryIconLayout, R.mipmap.icon_electricity_c);
        }
        if (level >= 0 && level < 25) {
            updateUi(disPlayBatteryIconLayout, R.mipmap.icon_electricity_disable);
        }

        int rssi = AppConfig.DEVICE_WIFI;

        if (rssi > -50 && rssi < 0) {
            updateUi(disPlayRssiIconLayout, R.mipmap.icon_network);
        } else if (rssi > -70 && rssi <= -50) {
            updateUi(disPlayRssiIconLayout, R.mipmap.icon_network_a);
        } else if (rssi < -70) {
            updateUi(disPlayRssiIconLayout, R.mipmap.icon_network_b);
        } else if (rssi == -200) {
            updateUi(disPlayRssiIconLayout, R.mipmap.icon_network_disable);
        }


        if (AppConfig.SIP_STATUS) {
            handler.sendEmptyMessage(3);
        } else {
            handler.sendEmptyMessage(4);
        }
    }

    /**
     * 更新UI
     */
    public void updateUi(final ImageView imageView, final int n) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setBackgroundResource(n);
            }
        });
    }

    @OnClick(R.id.finish_back_layou)
    public void finishPage(View view) {
        threadIsRun = false;
        ActivityUtils.removeActivity(this);
        LandChatActivity.this.finish();
    }

    @Override
    public void onNetChange(int state, String name) {

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    if (isVisible)
                        disPlaySipConnetIconLayout.setBackgroundResource(R.mipmap.icon_connection_normal);
                    break;
                case 4:
                    if (isVisible)
                        disPlaySipConnetIconLayout.setBackgroundResource(R.mipmap.icon_connection_disable);
                    break;
                case 8:
                    if (isVisible)
                        displayCurrentTime();
                    break;
            }
        }
    };

}