package com.tehike.mst.client.project.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.adapters.ChatListAdapter;
import com.tehike.mst.client.project.base.BaseFragment;
import com.tehike.mst.client.project.entity.SipBean;
import com.tehike.mst.client.project.linphone.MessageCallback;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.ui.widget.SpaceItemDecoration;
import com.tehike.mst.client.project.ui.widget.WrapContentLinearLayoutManager;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.NetworkUtils;

import org.linphone.core.LinphoneChatMessage;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * @author Wpf
 * @version v1.0
 * @date：2016-5-4 下午5:14:58
 */
public class ChatListFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * 显示联系人列表的recyclearview
     */
    @BindView(R.id.chat_contact_list_layout)
    RecyclerView chatList;

    /**
     * 显示下拉刷新的SwipeRefreshLayout
     */
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout sw;

    /**
     * 数据适配器
     */
    ChatListAdapter ada = null;

    /**
     * list展示数据
     */
    List<SipBean> mList = new ArrayList<>();

    /**
     * 是否正在运行
     */
    boolean threadIsRun = true;

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

        //设置下拉 颜色
        sw.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //设置下拉 刷新
        sw.setOnRefreshListener(this);

        //设置recyclerview的布局及item间隔
        chatList.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), WrapContentLinearLayoutManager.VERTICAL, false));
        chatList.addItemDecoration(new SpaceItemDecoration(0, 30));
        chatList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        //联系人列表
        presentationsList();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {


            } else
                Logutil.e("error");

            super.setUserVisibleHint(isVisibleToUser);
    }

    private void presentationsList() {

        //group分组的id
        String groupID = "0";

        if (NetworkUtils.isConnected()) {


//            //  showProgressDialogWithText("数据加载ing...");
//            RequestSipSourcesThread requestSipSourcesThread = new RequestSipSourcesThread(getActivity(), groupID + "", new RequestSipSourcesThread.SipListern() {
//                @Override
//                public void getDataListern(List<SipBean> sipList) {
//                    if (sipList != null && sipList.size() > 0) {
//                        if (mList != null || mList.size() > 0) {
//                            mList.clear();
//                        }
//                        for (SipBean sipBean : sipList) {
//                            SipClient sipClient = new SipClient();
//                            sipClient.setDeviceName(sipBean.getName());
//                            sipClient.setUsrname(sipBean.getNumber());
//                            mList.add(sipClient);
//                        }
//                        handler.sendEmptyMessage(1);
//                    } else {
//                        handler.sendEmptyMessage(2);
//                    }
//                }
//            });
//            requestSipSourcesThread.start();
        } else {
            showNoNetworkView();
        }
    }

    @Override
    public void onNetworkViewRefresh() {
        super.onNetworkViewRefresh();
        showProgressDialogWithText("正在努力加载中...");
        presentationsList();
    }

    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                presentationsList();
                if (sw != null)
                    sw.setRefreshing(false);
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
                for (int i = 0; i < mList.size(); i++) {
                    if (mList.get(i).getNumber().equals(from)) {
                        p = i;
                        break;
                    }
                }
                if (ada != null)
                    ada.notifyItemChanged(p);
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    dismissProgressDialog();
                    ada = new ChatListAdapter(getActivity(), mList);
                    if (chatList != null) {
                        chatList.setAdapter(ada);
                        ada.setOnItemClickListener(new ChatListAdapter.OnItemClickListener() {
                            @Override
                            public void onClick(SipBean sipClient) {
                                if (sipClient != null) {
//                                    Intent intent = new Intent();
//                                    intent.setClass(getActivity(), PortChatActivity.class);
//                                    Bundle bundle = new Bundle();
//                                    bundle.putSerializable("sipclient", sipClient);
//                                    intent.putExtras(bundle);
//                                    getActivity().startActivity(intent);
                                } else {
                                }
                            }
                        });
                    }

                    break;
                case 2:
                    showEmptyView();
                    break;
            }
        }
    };
}
