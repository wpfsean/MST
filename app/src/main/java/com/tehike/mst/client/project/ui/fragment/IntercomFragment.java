package com.tehike.mst.client.project.ui.fragment;

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

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.base.BaseFragment;
import com.tehike.mst.client.project.entity.SipGroupInfoBean;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.ui.portactivity.PortSipInforActivity;
import com.tehike.mst.client.project.utils.HttpBasicRequest;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.NetworkUtils;
import com.tehike.mst.client.project.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 描述：竖屏sip分组页面
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/11/2 14:58
 */

public class IntercomFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * 显示sip组的View
     */
    @BindView(R.id.sip_group_recyclearview)
    public RecyclerView sipGroupView;

    /**
     * 下拉刷新布局
     */
    @BindView(R.id.sipgrou_intercom_refreshlayout)
    SwipeRefreshLayout sipGroupRefreshView;

    /**
     * 盛放数据的集合
     */
    List<SipGroupInfoBean> sipGroupListData = new ArrayList<>();

    /**
     * 当前页面是否可见
     */
    boolean currentFragmentVisiable = false;

    SipGroupAdapter sipGroupAdapter;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_port_fragment_sipgroup_intercom_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //初始化下拉刷新组件
        initializeRefreshModule();

        //获取sip分组资源
        initializeSipGroupData();
    }

    /**
     * 初始化下拉刷新
     */
    private void initializeRefreshModule() {

        //下拉刷新的颜色
        sipGroupRefreshView.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //下拉监听
        sipGroupRefreshView.setOnRefreshListener(this);
    }

    /**
     * 初始化适配器展示sip分组数据
     */
    private void disPlaySipGroupAdapter() {
        //判断父页面存在，并当前页面可见
        if (getActivity() != null)
                //判断adapter是否存在
                if (sipGroupAdapter == null) {
                    sipGroupAdapter = new SipGroupAdapter(getActivity(), sipGroupListData);
                    sipGroupView.setAdapter(sipGroupAdapter);
                }
                //刷新adapter
                sipGroupAdapter.notifyDataSetChanged();
                GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
                gridLayoutManager.setReverseLayout(false);
                gridLayoutManager.setOrientation(GridLayout.VERTICAL);
                sipGroupView.setLayoutManager(gridLayoutManager);
                sipGroupAdapter.setItemClickListener(new SipGroupAdapter.MyItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                    int group_id = sipGroupListData.get(position).getId();
                    Intent intent = new Intent();
                    intent.putExtra("group_id", group_id);
                    intent.setClass(getActivity(), PortSipInforActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    }
                });
    }

    /**
     * 根据webapi接口获取sip分组数据
     */
    private void initializeSipGroupData() {
        //先清空集合内的数据
        if (sipGroupListData != null && sipGroupListData.size() > 0) {
            sipGroupListData.clear();
        }
        //判断网络状态
        if (!NetworkUtils.isConnected())
            handler.sendEmptyMessage(2);

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
                                sipGroupListData.add(sipGroupInfoBean);
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

    @Override
    public void onNetworkViewRefresh() {
        initializeSipGroupData();
    }

    /**
     * 点击返回按键
     */
    @OnClick(R.id.finish_intercom_fragment_layout)
    public void finishPage(View view) {
        getActivity().finish();
    }

    /**
     * 点击刷新按钮
     */
    @OnClick(R.id.refresh_sipgroup_btn_layout)
    public void refreshPageData(View view) {
        refreshData();
    }

    /**
     * 刷新数据
     */
    private void refreshData() {
        if (sipGroupRefreshView != null)
            sipGroupRefreshView.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sipGroupRefreshView != null)
                    sipGroupRefreshView.setRefreshing(false);
                getResources();
                handler.sendEmptyMessage(10);

            }
        }, 2 * 1000);
    }

    @Override
    public void onRefresh() {
        //子线程延时后去刷新
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initializeSipGroupData();
                if (sipGroupRefreshView != null)
                    sipGroupRefreshView.setRefreshing(false);
            }
        }, 2 * 1000);
    }

    @Override
    public void onDestroyView() {

        if (handler != null)
            handler.removeCallbacksAndMessages(null);

        super.onDestroyView();
    }

    /**
     * 六大按键的点击事件
     */
    @OnClick({R.id.port_sipgroup_voice_btn, R.id.port_sipgroup_intercom_btn, R.id.port_sipgroup_remote_waring_btn, R.id.port_sipgroup_video_intercom_btn, R.id.port_sipgroup_remote_shot_btn, R.id.port_sipgroup_remote_speaking_btn})
    public void btnClickEvent(View view) {
        switch (view.getId()) {
            case R.id.port_sipgroup_voice_btn:
                showProgressFail("暂不支持!");
                break;
            case R.id.port_sipgroup_intercom_btn:
                showProgressFail("暂不支持!");
                break;
            case R.id.port_sipgroup_remote_waring_btn:
                showProgressFail("暂不支持!");
                break;
            case R.id.port_sipgroup_video_intercom_btn:
                showProgressFail("暂不支持!");
                break;
            case R.id.port_sipgroup_remote_shot_btn:
                showProgressFail("暂不支持!");
                break;
            case R.id.port_sipgroup_remote_speaking_btn:
                showProgressFail("暂不支持!");
                break;
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        currentFragmentVisiable = isVisibleToUser;
        super.setUserVisibleHint(isVisibleToUser);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    if (getActivity() != null)
                        if (currentFragmentVisiable)
                            showProgressDialogWithText("网络异常...");
                    break;
                case 3:
                    if (getActivity() != null)
                        if (currentFragmentVisiable)
                            showProgressDialogWithText("解析Sip分组数据异常...");
                    break;
                case 5:
                    disPlaySipGroupAdapter();
                    break;

                case 10:
                    if (isVisible())
                        showProgressSuccess("已更新!");
                    break;
            }
        }
    };
}
