package com.tehike.mst.client.project.ui.landactivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.entity.SipGroupInfoBean;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.linphone.SipManager;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.ui.fragment.SipGroupAdapter;
import com.tehike.mst.client.project.ui.widget.SpaceItemDecoration;
import com.tehike.mst.client.project.utils.BatteryUtils;
import com.tehike.mst.client.project.utils.HttpBasicRequest;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.linphone.core.LinphoneChatRoom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 描述：横屏Sip分组页面
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/10/23 9:45
 */

public class LandSipGroupActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * 展示列表的recyclerview
     */
    @BindView(R.id.sip_group_recyclearview)
    public RecyclerView disPlaySipGroupItemViewLayout;

    /**
     * 下拉 刷新控件
     */
    @BindView(R.id.sipgroup_refresh_layout_land)
    SwipeRefreshLayout disPlaysipGroupRefreshViewLayout;

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
    ImageView disPlaySipMessIconLayout;

    /**
     * 当前电量显示
     */
    @BindView(R.id.prompt_electrity_values_land_layout)
    TextView displayCurrentBatteryLayout;

    /**
     * 连接状态
     */
    @BindView(R.id.icon_connection_show)
    ImageView disPlayConnetIconLayout;

    /**
     * 存放SipGroup信息的集合
     */
    List<SipGroupInfoBean> sipGroupResources = null;

    /**
     * 线程是否正在运行
     */
    boolean threadIsRun = true;


    @Override
    protected int intiLayout() {
        return R.layout.activity_land_sip_group;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //初始化下拉刷新控件
        initializeRefreshView();

        //显示时间
        initializeTime();

        //获取sip分组数据
        initSipGroupData();
    }

    /**
     * 初始化显示时间及日期
     */
    private void initializeTime() {
        //刷新时间的线程
        TimingThread timeThread = new TimingThread();
        new Thread(timeThread).start();

        //显示当前的年月日
        SimpleDateFormat dateD = new SimpleDateFormat("yyyy年MM月dd日");
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        disPlayCurrentYearLayout.setText(dateD.format(date).toString());

        //显示当前的电量
        int electricityValues = BatteryUtils.getSystemBattery(App.getApplication());
        displayCurrentBatteryLayout.setText(electricityValues + "");
    }

    /**
     * 显示时间的线程
     */
    class TimingThread extends Thread {
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
     * 初始化下拉刷新控件
     */
    private void initializeRefreshView() {
        //设置下拉 颜色
        disPlaysipGroupRefreshViewLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //设置下拉 刷新
        disPlaysipGroupRefreshViewLayout.setOnRefreshListener(this);
    }

    /**
     * CMS获取Sip分组信息
     */
    private void initSipGroupData() {
        //盛放sip数据的集合
        sipGroupResources = new ArrayList<>();
        //清除集合数据
        if (sipGroupResources != null && sipGroupResources.size() > 0) {
            sipGroupResources.clear();
        }
        //判断 网络
        if (!NetworkUtils.isConnected()) {
            handler.sendEmptyMessage(17);
            return;
        }

        //数据请求
        String sipGroupUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._USIPGROUPS;

        //请求sip组数据
        HttpBasicRequest thread = new HttpBasicRequest(sipGroupUrl, new HttpBasicRequest.GetHttpData() {
            @Override
            public void httpData(String result) {
                if (!TextUtils.isEmpty(result)) {
                    Logutil.d("Sip组数据"+result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        if (!jsonObject.isNull("errorCode")) {
                            Logutil.w("请求不到数据信息");
                            return;
                        }
                        int sipCount = jsonObject.getInt("count");
                        if (sipCount > 0) {
                            JSONArray jsonArray = jsonObject.getJSONArray("groups");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonItem = jsonArray.getJSONObject(i);
                                SipGroupInfoBean sipGroupInfoBean = new SipGroupInfoBean();
                                sipGroupInfoBean.setId(jsonItem.getInt("id"));
                                sipGroupInfoBean.setMember_count(jsonItem.getString("member_count"));
                                sipGroupInfoBean.setName(jsonItem.getString("name"));
                                sipGroupResources.add(sipGroupInfoBean);
                            }
                        }
                        handler.sendEmptyMessage(5);
                    } catch (Exception e) {
                        Logutil.e("解析Sip分组数据异常" + e.getMessage());
                        handler.sendEmptyMessage(3);
                    }
                } else {
                    handler.sendEmptyMessage(3);
                }
            }
        });
        new Thread(thread).start();

    }

    /**
     * 适配数据器
     */
    private void initSipGroupAdapter() {
        if (!isVisible) {
            return;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(LandSipGroupActivity.this, 4);
        gridLayoutManager.setReverseLayout(false);
        gridLayoutManager.setOrientation(GridLayout.VERTICAL);
        SpaceItemDecoration spaceItemDecoration = new SpaceItemDecoration(16,36);

        disPlaySipGroupItemViewLayout.setLayoutManager(gridLayoutManager);
        SipGroupAdapter adapter = new SipGroupAdapter(LandSipGroupActivity.this, sipGroupResources);
        disPlaySipGroupItemViewLayout.setAdapter(adapter);
        disPlaySipGroupItemViewLayout.addItemDecoration(spaceItemDecoration);
        adapter.setItemClickListener(new SipGroupAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                SipGroupInfoBean sipGroupBean = sipGroupResources.get(position);
                if (sipGroupBean != null) {
                    int group_id = sipGroupBean.getId();
                    Intent intent = new Intent();
                    intent.putExtra("group_id", group_id);
                    intent.setClass(LandSipGroupActivity.this, LandSipInforActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 下拉刷新事件
     */
    @Override
    public void onRefresh() {
        //利用子线程延迟两秒后重新刷新数据
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (disPlaysipGroupRefreshViewLayout != null)
                    disPlaysipGroupRefreshViewLayout.setRefreshing(false);
                getResources();

            }
        }, 2 * 1000);
    }

    /**
     * 下拉刷新数据
     */
    private void refreshData() {
        //显示正在刷新
        disPlaysipGroupRefreshViewLayout.setRefreshing(true);
        //延迟两秒后停止刷新 并获取新的数据
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (disPlaysipGroupRefreshViewLayout != null)
                    //提示刷新结果
                    handler.sendEmptyMessage(19);
                disPlaysipGroupRefreshViewLayout.setRefreshing(false);
                getResources();

            }
        }, 2 * 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        disPlayAppStatusIcon();
    }

    /**
     * 显示当前app的状态图标
     */
    private void disPlayAppStatusIcon() {

        //信息状态
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

        if (SipService.isReady() || SipManager.isInstanceiated()) {
            LinphoneChatRoom[] rooms = SipManager.getLc().getChatRooms();
            String nativeSipNumber = SysinfoUtils.getSysinfo().getSipUsername();
            if (TextUtils.isEmpty(nativeSipNumber)) {
                return;
            }
            for (int j = 0; j < rooms.length; j++) {
                int unRead = rooms[j].getUnreadMessagesCount();
                Logutil.i("unRead:" + unRead);
                if (unRead > 0)
                    handler.sendEmptyMessage(16);
                else
                    handler.sendEmptyMessage(15);
            }
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

    /**
     * 网络状态回调
     */
    @Override
    public void onNetChange(int state, String name) {
        if (state == -1 || state == 5) {
            //提示无网络
            handler.sendEmptyMessage(17);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止刷新时间线程
        threadIsRun = false;
        //移除所有的handler监听
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    /**
     * 右侧功能按键的点击事件
     */
    @OnClick({R.id.remote_warning_layou, R.id.remote_gunshoot_layou, R.id.remote_speaking_layou,
            R.id.instant_message_layout, R.id.voice_intercom_icon_layout, R.id.video_intercom_layout, R.id.sip_group_refresh_layout, R.id.sip_group_finish_icon})
    public void onclickEvent(View view) {
        switch (view.getId()) {
            case R.id.voice_intercom_icon_layout:
                if (isVisible)
                    showProgressFail("暂不支持!");
                break;
            case R.id.video_intercom_layout:
                if (isVisible)
                    showProgressFail("暂不支持!");
                break;
            case R.id.instant_message_layout:
                if (isVisible)
                    showProgressFail("暂不支持!");
                break;
            case R.id.remote_warning_layou:
                if (isVisible)
                    showProgressFail("暂不支持!");
                break;
            case R.id.remote_gunshoot_layou:
                if (isVisible)
                    showProgressFail("暂不支持!");
                break;
            case R.id.remote_speaking_layou:
                if (isVisible)
                    showProgressFail("暂不支持!");
                break;
            case R.id.sip_group_refresh_layout:
                refreshData();
                break;
            case R.id.sip_group_finish_icon:
                LandSipGroupActivity.this.finish();
                break;
        }
    }


    /**
     * Handler处理子线程发送的消息
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3://提示sip连接状态正常
                    if (isVisible)
                        disPlayConnetIconLayout.setBackgroundResource(R.mipmap.icon_connection_normal);
                    break;
                case 4://提示sip连接状态断开
                    if (isVisible)
                        disPlayConnetIconLayout.setBackgroundResource(R.mipmap.icon_connection_disable);
                    break;
                case 5://初始化sip组适配器
                    initSipGroupAdapter();
                    break;
                case 6://提示未获取到值班室信息
                    if (isVisible)
                        showProgressFail("未获取到值班室信息");
                    break;
                case 7://提示值班室不在线
                    if (isVisible)
                        showProgressFail("对方不在线！");
                    break;
                case 8://刷新显示当前的时间
                    if (isVisible)
                        displayCurrentTime();
                    break;
                case 15://新消息提示消除
                    updateUi(disPlaySipMessIconLayout, R.mipmap.message);
                    break;
                case 16://提示新消息
                    updateUi(disPlaySipMessIconLayout, R.mipmap.newmessage);
                    break;
                case 17://提示网络异常
                    if (isVisible)
                        showProgressFail("请检查网络状态！");
                    break;
                case 19://提示数据刷新成功
                    if (isVisible)
                        showProgressSuccess("刷新成功!");
                    break;
                case 23://显示当前回调的电量数据
                    int level = msg.arg1;
                    if (isVisible)
                        displayCurrentBatteryLayout.setText("" + level);
                    break;

            }
        }
    };
}
