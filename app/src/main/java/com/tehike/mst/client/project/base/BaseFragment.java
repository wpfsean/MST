package com.tehike.mst.client.project.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.ui.widget.NetworkStateView;
import com.tehike.mst.client.project.utils.ProgressDialogUtils;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 描述：BaseFragment，用于子Fragment继承（同Activity）
 * <p>
 * 网络状态变化的监听回调
 * ProgressBar的提示
 * ButterKnife的注解
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/28 9:01
 */

public abstract class BaseFragment extends Fragment implements NetworkStateView.OnRefreshListener {

    /**
     * 当前可见的View
     */
    private View mView;

    /**
     * 一个unbinder合同，在调用时将取消绑定视图
     */
    private Unbinder unbinder;

    /**
     * ProgreeDialog工具类
     */
    private ProgressDialogUtils progressDialog;

    /**
     * 网络状态的View
     */
    private NetworkStateView networkStateView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //添加View
        mView = inflater.inflate(R.layout.base_fragment, container, false);
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (null != parent) {
            parent.removeView(mView);
        }
        //添加子View
        addChildView(inflater);

        //添加注解
        unbinder = ButterKnife.bind(this, mView);

        //初始化Dialog
        initDialog();

        afterCreate(savedInstanceState);

        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 添加子Fragment的布局文件
     */
    private void addChildView(LayoutInflater inflater) {
        networkStateView = (NetworkStateView) mView.findViewById(R.id.nsv_state_view);
        FrameLayout container = (FrameLayout) mView.findViewById(R.id.fl_fragment_child_container);
        View child = inflater.inflate(getLayoutId(), null);
        container.addView(child, 0);
    }

    protected abstract int getLayoutId();

    protected abstract void afterCreate(Bundle savedInstanceState);

    /**
     * 初始化Dialog
     */
    private void initDialog() {
        progressDialog = new ProgressDialogUtils(getActivity(), R.style.dialog_transparent_style);
    }

    /**
     * 显示加载中的布局
     */
    public void showLoadingView() {
        networkStateView.showLoading();
    }

    /**
     * 显示加载完成后的布局(即子类Activity的布局)
     */
    public void showContentView() {
        networkStateView.showSuccess();
    }

    /**
     * 显示没有网络的布局
     */
    public void showNoNetworkView() {
        networkStateView.showNoNetwork();
        networkStateView.setOnRefreshListener(this);
    }

    /**
     * 显示没有数据的布局
     */
    public void showEmptyView() {
        networkStateView.showEmpty();
        networkStateView.setOnRefreshListener(this);
    }

    /**
     * 显示数据错误，网络错误等布局
     */
    public void showErrorView() {
        networkStateView.showError();
        networkStateView.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        onNetworkViewRefresh();
    }

    /**
     * 重新请求网络
     */
    public void onNetworkViewRefresh() {
    }

    /**
     * 显示加载的ProgressDialog
     */
    public void showProgressDialog() {
        progressDialog.showProgressDialog();
    }

    /**
     * 显示有加载文字ProgressDialog，文字显示在ProgressDialog的下面
     */
    public void showProgressDialogWithText(String text) {
        progressDialog.showProgressDialogWithText(text);
    }

    /**
     * 显示加载成功的ProgressDialog，文字显示在ProgressDialog的下面
     */
    public void showProgressSuccess(String message, long time) {
        progressDialog.showProgressSuccess(message, time);
    }

    /**
     * 显示加载成功的ProgressDialog，文字显示在ProgressDialog的下面
     * ProgressDialog默认消失时间为1秒(1000毫秒)
     */
    public void showProgressSuccess(String message) {
        progressDialog.showProgressSuccess(message);
    }

    /**
     * 显示加载失败的ProgressDialog，文字显示在ProgressDialog的下面
     */
    public void showProgressFail(String message, long time) {
        progressDialog.showProgressFail(message, time);
    }

    /**
     * 显示加载失败的ProgressDialog，文字显示在ProgressDialog的下面
     * ProgressDialog默认消失时间为1秒(1000毫秒)
     */
    public void showProgressFail(String message) {
        progressDialog.showProgressFail(message);
    }

    /**
     * 隐藏加载的ProgressDialog
     */
    public void dismissProgressDialog() {
        progressDialog.dismissProgressDialog();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
