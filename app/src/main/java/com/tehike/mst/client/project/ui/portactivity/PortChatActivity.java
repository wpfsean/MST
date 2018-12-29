package com.tehike.mst.client.project.ui.portactivity;

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
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.entity.ChatMsgEntity;
import com.tehike.mst.client.project.entity.SipBean;
import com.tehike.mst.client.project.entity.SipClient;
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
import com.tehike.mst.client.project.utils.SharedPreferencesUtils;
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
 * Created by Root on 2018/7/23.
 */

public class PortChatActivity extends BaseActivity implements View.OnClickListener {

    /**
     * 发送按钮
     */
    @BindView(R.id.send_message_btn_layout)
    TextView mBtnSend;

    /**
     * 消息输入框
     */
    @BindView(R.id.sendmessage_layout)
    EditText mEditTextContent;

    /**
     * 历史消息布局
     */
    @BindView(R.id.message_listview_layout)
    ListView mListView;

    /**
     * 显示当前的聊天对象布局
     */
    @BindView(R.id.current_fragment_name)
    TextView current_fragment_name;

    /**
     * 电量信息
     */
    @BindView(R.id.icon_electritity_show)
    ImageView batteryIcon;

    /**
     * 信号强度
     */
    @BindView(R.id.icon_network)
    ImageView rssiIcon;

    /**
     * 显示当前时间分秒
     */
    @BindView(R.id.sipinfor_title_time_layout)
    TextView currentTimeLayout;

    /**
     * 显示当前的年月日
     */
    @BindView(R.id.sipinfor_title_date_layout)
    TextView currentYearLayout;

    /**
     * 消息图标
     */
    @BindView(R.id.icon_message_show)
    ImageView messageIcon;

    /**
     * 连接状态
     */
    @BindView(R.id.icon_connection_show)
    ImageView connetIcon;

    /**
     * 聊天对象
     */
    String who = "";

    /**
     * 聊天对象的设备名
     */
    String deviceName = "";


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
    String sipNum = "";

    /**
     * 线程是否正在运行
     */
    boolean threadIsRun = true;


    List<SipBean> receiveData = null;


    @Override
    protected int intiLayout() {
        return R.layout.activity_port_chat;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        mBtnSend.setOnClickListener(this);

        //显示时间
        initTime();

        //初始化数据对象
        initDb();

        //初始化参数
        initParamater();

        //使未读消息变为已读
        initMessRead();

        //加载历史记录
   //     getAllHistory();

        //适配加载数据
        initAdapter();

    }

    private void initAdapter() {
        //初始化适配器
        mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
        mListView.setAdapter(mAdapter);
        mListView.setSelection(mListView.getCount());
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
            currentTimeLayout.setText(currentTime);
        }
    }

    /**
     * 初始化显示时间及日期
     */
    private void initTime() {
        TimeThread timeThread = new TimeThread();
        new Thread(timeThread).start();

        SimpleDateFormat dateD = new SimpleDateFormat("yyyy年MM月dd日");
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        currentYearLayout.setText(dateD.format(date).toString());
    }


    /**
     * 初始化数据库对象
     */
    private void initDb() {
        mDataArrays.clear();
    }

    /**
     * 使所有的未读消息变为已读
     */
    private void initMessRead() {
        if (SipService.isReady() || SipManager.isInstanceiated()) {
            LinphoneChatRoom[] rooms = SipManager.getLc().getChatRooms();
            if (rooms.length > 0) {
                for (LinphoneChatRoom room : rooms) {
                    if (room.getPeerAddress().getUserName().equals(who)) {
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
        sipNum = SysinfoUtils.getSysinfo().getSipUsername();

        //获取当前对话列表点击 的用户名
        SipClient sipClient = (SipClient) getIntent().getExtras().getSerializable("sipclient");


        try {
            String videoSourceStr = FileUtil.readFile(AppConfig.SOURCES_SIP).toString();
            receiveData = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(videoSourceStr), SipBean.class);
        } catch (Exception e) {
        }


        if (sipClient != null) {
            String chatObject = sipClient.getUsrname();
            if (!TextUtils.isEmpty(chatObject)) {
                who = chatObject;

                if (receiveData != null && receiveData.size() > 0) {
                    for (SipBean device : receiveData) {
                        if (device.getNumber().equals(who)) {
                            deviceName = device.getName();
                            current_fragment_name.setText(device.getName());
                        }
                    }
                }
                String sipserver = SysinfoUtils.getSysinfo().getSipServer();
                if (!TextUtils.isEmpty(sipserver)) {
                    try {
                        linphoneAddress = LinphoneCoreFactory.instance().createLinphoneAddress("sip:" + who + "@" + sipserver);
                    } catch (LinphoneCoreException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                return;
            }
        }
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
                    if (receiveData != null && receiveData.size() > 0) {
                        for (SipBean device : receiveData) {
                            if (device.getNumber().equals(who)) {
                                chatMsgEntity.setName(device.getName());
                            }
                        }
                    }
                    chatMsgEntity.setDate(TimeUtils.longTime2Short(new Date().toString()));
                    chatMsgEntity.setMsgType(true);
                    chatMsgEntity.setText(linphoneChatMessage.getText());
                    mDataArrays.add(chatMsgEntity);
                    mAdapter.notifyDataSetChanged();
                    mEditTextContent.setText("");
                    mListView.setSelection(mListView.getCount() - 1);
                }
            });
        }
    }

    /**
     * 取出所有的聊天记录
     */
    private void getAllHistory() {
        //根据条件查询聊天记录
        Cursor cursor = db.query("chat", null, "fromuser =? or touser = ?", new String[]{who, who}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String fromuser = cursor.getString(cursor.getColumnIndex("fromuser"));
                String message = cursor.getString(cursor.getColumnIndex("message"));
                String toUser = cursor.getString(cursor.getColumnIndex("touser"));
                if (toUser.equals(who)) {
                    ChatMsgEntity mEntity = new ChatMsgEntity();
                    mEntity.setDate(TimeUtils.longTime2Short(time));
                    if (receiveData != null && receiveData.size() > 0) {
                        for (SipBean device : receiveData) {
                            if (device.getNumber().equals(fromuser)) {
                                mEntity.setName(device.getName());
                            }
                        }
                    }
                    mEntity.setMsgType(false);
                    mEntity.setText(message);
                    mDataArrays.add(mEntity);
                } else if (fromuser.equals(who)) {
                    ChatMsgEntity tEntity = new ChatMsgEntity();
                    tEntity.setDate(TimeUtils.longTime2Short(time));
                    if (receiveData != null && receiveData.size() > 0) {
                        for (SipBean device : receiveData) {
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
    private void sendMess() {
        String chatMessage = mEditTextContent.getText().toString().trim();
        if (!TextUtils.isEmpty(chatMessage) && chatMessage.length() > 0) {
            //送消息的展示界面
            ChatMsgEntity entity = new ChatMsgEntity();
            entity.setText(chatMessage);
            entity.setMsgType(false);

            if (receiveData != null && receiveData.size() > 0) {
                for (SipBean device : receiveData) {
                    if (device.getNumber().equals(sipNum)) {
                        entity.setName(device.getName());
                        break;
                    }
                }
            }
            entity.setDate(getDate());
            mDataArrays.add(entity);
            mAdapter.notifyDataSetChanged();
            mEditTextContent.setText("");
            mListView.setSelection(mListView.getCount() - 1);
            //（发送sip短消息到对方）
            if (SipService.isReady())
                Linphone.getLC().getChatRoom(linphoneAddress).sendMessage(chatMessage);

//            //把发的消息插入到数据库
            ContentValues contentValues = new ContentValues();
            contentValues.put("time", new Date().toString());
            contentValues.put("fromuser", sipNum);
            contentValues.put("message", chatMessage);
            contentValues.put("touser", who);
            db.insert("chat", null, contentValues);
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
            updateUi(batteryIcon, R.mipmap.icon_electricity_a);
        }
        if (level >= 50 && level < 75) {
            updateUi(batteryIcon, R.mipmap.icon_electricity_b);
        }
        if (level >= 25 && level < 50) {
            updateUi(batteryIcon, R.mipmap.icon_electricity_c);
        }
        if (level >= 0 && level < 25) {
            updateUi(batteryIcon, R.mipmap.icon_electricity_disable);
        }

        int rssi = AppConfig.DEVICE_WIFI;

        if (rssi > -50 && rssi < 0) {
            updateUi(rssiIcon, R.mipmap.icon_network);
        } else if (rssi > -70 && rssi <= -50) {
            updateUi(rssiIcon, R.mipmap.icon_network_a);
        } else if (rssi < -70) {
            updateUi(rssiIcon, R.mipmap.icon_network_b);
        } else if (rssi == -200) {
            updateUi(rssiIcon, R.mipmap.icon_network_disable);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_message_btn_layout:
                sendMess();
                break;
        }
    }


    @OnClick(R.id.finish_back_layou)
    public void finishPage(View view) {
        threadIsRun = false;
        ActivityUtils.removeActivity(this);
        PortChatActivity.this.finish();
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
                        connetIcon.setBackgroundResource(R.mipmap.icon_connection_normal);
                    break;
                case 4:
                    if (isVisible)
                        connetIcon.setBackgroundResource(R.mipmap.icon_connection_disable);
                    break;
                case 8:
                    if (isVisible)
                        displayCurrentTime();
                    break;
            }
        }
    };

}