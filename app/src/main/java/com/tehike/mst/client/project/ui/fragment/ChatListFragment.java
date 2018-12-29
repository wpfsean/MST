package com.tehike.mst.client.project.ui.fragment;

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
import com.tehike.mst.client.project.base.BaseFragment;
import com.tehike.mst.client.project.entity.SipBean;
import com.tehike.mst.client.project.entity.VideoBean;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.linphone.MessageCallback;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.ui.portactivity.PortChatActivity;
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
 * 描述：Sip列表页面
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/29 17:12
 */
public class ChatListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * 显示联系人列表的recyclearview
     */
    @BindView(R.id.chat_contact_list_layout)
    RecyclerView disPlayAllSipItemsView;

    /**
     * 显示下拉刷新的SwipeRefreshLayout
     */
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout disPlayRefreshSipItemsView;

    /**
     * 当前Fragment是否可见
     */
    boolean currentPageIsVisible = false;

    /**
     * 数据适配器
     */
    SipItemsAdapter mSipItemsAdapter = null;

    /**
     * list展示数据
     */
    List<SipBean> allSipItemsList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_port_fragment_chat;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeParamaters();
        //联系人列表
        initializeSipItemsData();
    }

    /**
     * 初始化参数
     */
    private void initializeParamaters() {
        //设置下拉 颜色
        disPlayRefreshSipItemsView.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //设置下拉 刷新
        disPlayRefreshSipItemsView.setOnRefreshListener(this);

        //设置recyclerview的布局及item间隔
        disPlayAllSipItemsView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), WrapContentLinearLayoutManager.VERTICAL, false));
        disPlayAllSipItemsView.addItemDecoration(new SpaceItemDecoration(0, 30));
        disPlayAllSipItemsView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化获取Sip联系人列表
     */
    private void initializeSipItemsData() {
        if (NetworkUtils.isConnected()) {
            //获取某个组内数据
            String sipGroupItemUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._USIPGROUPS_GROUP;
            //子线程根据组Id请求组数据
            HttpBasicRequest httpThread = new HttpBasicRequest(sipGroupItemUrl + "0", new HttpBasicRequest.GetHttpData() {
                @Override
                public void httpData(String result) {
                    //无数据
                    if (TextUtils.isEmpty(result)) {
                        handler.sendEmptyMessage(2);
                        return;
                    }
                    //数据异常
                    if (result.contains("Execption")) {
                        handler.sendEmptyMessage(2);
                        return;
                    }
                    //清空数据
                    if (allSipItemsList != null && allSipItemsList.size() > 0) {
                        allSipItemsList.clear();
                    }
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
                                groupItemInfoBean.setName(jsonItem.getString("name"));
                                groupItemInfoBean.setNumber(jsonItem.getString("number"));
                                groupItemInfoBean.setSentryId(jsonItem.getInt("sentryId") + "");
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
                                                jsonItemVideo.getString("name"),
                                                jsonItemVideo.getString("password"),
                                                jsonItemVideo.getInt("port"),
                                                jsonItemVideo.getString("username"), "", "", "", "", "", "");
                                        groupItemInfoBean.setVideoBean(videoBean);
                                    }
                                }
                                allSipItemsList.add(groupItemInfoBean);
                            }
                        }
                        handler.sendEmptyMessage(1);
                    } catch (JSONException e) {
                        handler.sendEmptyMessage(2);
                        Logutil.e("Sip组内数据解析异常::" + e.getMessage());
                    }
                }
            });
            new Thread(httpThread).start();

        } else {
            showNoNetworkView();
        }
    }


    @Override
    public void onNetworkViewRefresh() {
        super.onNetworkViewRefresh();
        showProgressDialogWithText("正在努力加载中...");
        initializeSipItemsData();
    }

    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initializeSipItemsData();
                if (disPlayRefreshSipItemsView != null)
                    disPlayRefreshSipItemsView.setRefreshing(false);
            }
        }, 2 * 1000);
    }

    @Override
    public void onResume() {
        super.onResume();

        //回调,消息时时的刷新
        SipService.addMessageCallback(new MessageCallback() {
            @Override
            public void receiverMessage(LinphoneChatMessage linphoneChatMessage) {
                String from = linphoneChatMessage.getFrom().getUserName();
                int p = -1;
                for (int i = 0; i < allSipItemsList.size(); i++) {
                    if (allSipItemsList.get(i).getNumber().equals(from)) {
                        p = i;
                        break;
                    }
                }
                if (mSipItemsAdapter != null)
                    mSipItemsAdapter.notifyItemChanged(p);
            }
        });
    }

    @Override
    public void onDestroyView() {

        if (handler != null)
            handler.removeCallbacksAndMessages(null);

        super.onDestroyView();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    dismissProgressDialog();
                    mSipItemsAdapter = new SipItemsAdapter(getActivity(), allSipItemsList);
                    if (getActivity() != null) {
                        disPlayAllSipItemsView.setAdapter(mSipItemsAdapter);
                        mSipItemsAdapter.setOnItemClickListener(new SipItemsAdapter.OnItemClickListener() {
                            @Override
                            public void onClick(SipBean sipClient) {
                                if (sipClient != null) {
                                    Intent intent = new Intent();
                                    intent.setClass(getActivity(), PortChatActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("sipclient", sipClient);
                                    intent.putExtras(bundle);
                                    getActivity().startActivity(intent);
                                } else {
                                }
                            }
                        });
                    }

                    break;
                case 2:
                    if (getActivity() != null && currentPageIsVisible){
                        showProgressFail("未获取到Sip列表数据！");
                    }
                    break;
            }
        }
    };
}
