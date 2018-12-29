package com.tehike.mst.client.project.ui.landactivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;


import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.adapters.SipItemsAdapter;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.entity.SipBean;
import com.tehike.mst.client.project.entity.VideoBean;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.linphone.MessageCallback;
import com.tehike.mst.client.project.linphone.SipManager;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.ui.widget.SpaceItemDecoration;
import com.tehike.mst.client.project.ui.widget.WrapContentLinearLayoutManager;
import com.tehike.mst.client.project.utils.HttpBasicRequest;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.core.LinphoneChatMessage;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 *Created by wpf
 *
 * 横屏sip列表数据展示
 */


public class LandChatListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * 联系人列表
     */
    @BindView(R.id.land_chat_list_recyclerview_layout)
    RecyclerView chatListView;

    /**
     * 下拉刷新
     */
    @BindView(R.id.swipeRefreshLayout_land_layout)
    SwipeRefreshLayout chatSwipeView;

    /**
     * 展示数据的集合
     */
    List<SipBean> dataResources = new ArrayList<>();

    /**
     * 数据适配器
     */
    SipItemsAdapter chatListAdapter = null;

    /**
     * 当前页面是否可见
     */
    boolean isFront = false;


    @Override
    public void onNetChange(int state, String name) {

    }

    @Override
    protected int intiLayout() {
        return R.layout.activity_land_chat_list;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //设置recycleview方向
        chatListView.setLayoutManager(new WrapContentLinearLayoutManager(this, WrapContentLinearLayoutManager.VERTICAL, false));
        chatListView.addItemDecoration(new SpaceItemDecoration(0, 10));
        chatListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        //初始化下拉刷新组件
        initRefreshView();

        //加载cms数据
        initData();
    }

    /**
     * CMS上数据数据
     */
    private void initData() {
        //group分组的id
        String groupID = "0";

        if (NetworkUtils.isConnected()) {


            //获取某个组内数据
            String sipGroupItemUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._USIPGROUPS_GROUP;

            //子线程根据组Id请求组数据
            HttpBasicRequest httpThread = new HttpBasicRequest(sipGroupItemUrl + "0", new HttpBasicRequest.GetHttpData() {
                @Override
                public void httpData(String result) {
                    //无数据
                    if (TextUtils.isEmpty(result)) {
                        handler.sendEmptyMessage(1);
                        return;
                    }
                    //数据异常
                    if (result.contains("Execption")) {
                        handler.sendEmptyMessage(1);
                        return;
                    }

                    if (dataResources != null && dataResources.size() > 0) {
                        dataResources.clear();
                    }
                    Logutil.d("组数据" + result);
                    //解析sip资源
                    try {
                        JSONObject jsonObject = new JSONObject(result);

                        if (!jsonObject.isNull("errorCode")) {
                            Logutil.w("请求不到数据信息");
                            return;
                        }

                        int count = jsonObject.getInt("count");
                        if (count > 0) {
                            JSONArray jsonArray = jsonObject.getJSONArray("resources");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonItem = jsonArray.getJSONObject(i);
                                //解析
                                SipBean groupItemInfoBean = new SipBean();
                                groupItemInfoBean.setDeviceType(jsonItem.getString("deviceType"));
                                groupItemInfoBean.setId(jsonItem.getString("id"));
                                groupItemInfoBean.setIpAddress(jsonItem.getString("ipAddress"));
                                groupItemInfoBean.setName(jsonItem.getString("enteredUserName"));
                                groupItemInfoBean.setNumber(jsonItem.getString("number"));
                                groupItemInfoBean.setSentryId(jsonItem.getInt("sentryId")+"");
                                //判断是否有面部视频
                                if (!jsonItem.isNull("videosource")) {
                                    //解析面部视频
                                    JSONObject jsonItemVideo = jsonItem.getJSONObject("videosource");
                                    if (jsonItemVideo != null) {
                                        //封闭面部视频
                                        VideoBean videoBean = new VideoBean(
                                                jsonItemVideo.getString("channel"),
                                                jsonItemVideo.getString("devicetype"),
                                                jsonItemVideo.getString("id"),
                                                jsonItemVideo.getString("ipaddress"),
                                                jsonItemVideo.getString("enteredUserName"),
                                                jsonItemVideo.getString("password"),
                                                jsonItemVideo.getInt("port"),
                                                jsonItemVideo.getString("username"), "", "", "", "", "", "");
                                        groupItemInfoBean.setVideoBean(videoBean);
                                    }
                                }
                                dataResources.add(groupItemInfoBean);
                            }
                        }
                        handler.sendEmptyMessage(2);
                    } catch (JSONException e) {
                        Logutil.e("Sip组内数据解析异常::" + e.getMessage());
                    }
                }
            });
            new Thread(httpThread).start();
        } else {
            handler.sendEmptyMessage(1);
        }
    }

    /**
     * 初始化下拉刷新组件
     */
    private void initRefreshView() {
        //设置颜色
        chatSwipeView.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //设置下拉刷新监听
        chatSwipeView.setOnRefreshListener(this);

    }

    @Override
    public void onRefresh() {
        super.onRefresh();

        //定时两秒后刷新数据并消失
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initData();
                if (chatSwipeView != null)
                    chatSwipeView.setRefreshing(false);
            }
        }, 2 * 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFront = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isFront = true;

        if (SipService.isReady() || SipManager.isInstanceiated()) {
            //回调,消息时时的刷新
            SipService.addMessageCallback(new MessageCallback() {
                @Override
                public void receiverMessage(LinphoneChatMessage linphoneChatMessage) {
                    String from = linphoneChatMessage.getFrom().getUserName();
                    int p = -1;
                    for (int i = 0; i < dataResources.size(); i++) {
                        if (dataResources.get(i).getNumber().equals(from)) {
                            p = i;
                            break;
                        }
                    }
                    if (chatListAdapter != null)
                        chatListAdapter.notifyItemChanged(p);
                }
            });
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //重新刷新本页面
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initData();

            }
        }, 1 * 1000);

    }

    /**
     * 展示列表数据
     */
    private void disPlayeListData() {
        dismissProgressDialog();
        if (isFront) {
            chatListAdapter = new SipItemsAdapter(LandChatListActivity.this, dataResources);
            if (chatListView != null) {
                chatListView.setAdapter(chatListAdapter);
                chatListAdapter.setOnItemClickListener(new SipItemsAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(SipBean sipClient) {
                        if (sipClient != null) {
                            Intent intent = new Intent();
                            intent.setClass(LandChatListActivity.this, LandChatActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("sipclient", sipClient);
                            intent.putExtras(bundle);
                            LandChatListActivity.this.startActivity(intent);
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (isFront)
                        showProgressFail("No data!!!");
                    break;
                case 2:
                    if (isFront)
                        disPlayeListData();
                    break;
            }
        }
    };
}
