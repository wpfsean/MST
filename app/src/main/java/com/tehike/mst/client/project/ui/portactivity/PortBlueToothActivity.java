package com.tehike.mst.client.project.ui.portactivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.adapters.bluetooth.DevicesAdapter;
import com.tehike.mst.client.project.adapters.bluetooth.MessageAdapter;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.ui.widget.SpaceItemDecoration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 描述：蓝牙连接类
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/11/14 14:41
 */


public class PortBlueToothActivity extends BaseActivity {


    /**
     * 展示device的布局
     */
    @BindView(R.id.devices)
    RecyclerView devicesRecyclerView;

    /**
     * 展示devicees设备的适配器
     */
    DevicesAdapter devicesAdapter;

    /**
     * 文本输入框
     */
    @BindView(R.id.input)
    EditText inputEt;

    /**
     * 显示消息的View
     */
    @BindView(R.id.msglist)
    RecyclerView messageRecyclerView;

    /**
     * 消息适配器
     */
    MessageAdapter messageAdapter;

    /**
     * 广播监听
     */
    BlueToothStateReceiver mReceiver;

    /**
     * 当前设备连接的名称
     */
    @BindView(R.id.current_device)
    TextView current_device;

    /**
     * 默认的蓝牙适配器
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * 连接的线程
     */
    ConnectThread connectThread;

    /**
     * 等待连接 的线程
     */
    AcceptThread acceptThread;

    /**
     * 子线程刷新主UI界面
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String str = (String) msg.obj;
                messageAdapter.addMessage(str);
            } else if (msg.what == 1001) {
                String str = (String) msg.obj;
                messageAdapter.addMessage(str);
            }
        }
    };


    @Override
    protected int intiLayout() {
        return R.layout.activity_port_blue_tooth;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {


        //获取默认的蓝牙适配器
        if (mBluetoothAdapter == null)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //启动被连接 的线程
        if (mBluetoothAdapter != null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
        //设置适配器显示
        devicesRecyclerView.addItemDecoration(new SpaceItemDecoration(15, 0));
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        devicesAdapter = new DevicesAdapter(this);
        devicesRecyclerView.setAdapter(devicesAdapter);

        //消息适配器
        messageRecyclerView.addItemDecoration(new SpaceItemDecoration(5, 0));
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(this);
        messageRecyclerView.setAdapter(messageAdapter);

        //注册广播
        registerBrocastReceiver();

        //设备点击 事件
        devicesAdapter.setOnItemClickListener(new DevicesAdapter.OnItemClickListener() {
            @Override
            public void onClick(final BluetoothDevice device) {
                bandDevices(device);
            }
        });

    }

    @Override
    public void onNetChange(int state, String name) {

    }

    //尝试去配对设备
    private void bandDevices(final BluetoothDevice device) {
        messageAdapter.addMessage("正在尝试与此设备配对并连接:" + device.getName());
        //判断此设备是否已配对
        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                device.createBond();
            }
        } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            messageAdapter.addMessage("配对完成:" + device.getName());
            //已连接
            // connect(device);
            connectThread = new ConnectThread(device);
            connectThread.start();
        }

    }

    /**
     * 注册蓝牙搜索的监听
     */
    private void registerBrocastReceiver() {
        mReceiver = new BlueToothStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);//搜多到蓝牙
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//搜索结束
        registerReceiver(mReceiver, filter);
    }

    @OnClick({R.id.open, R.id.close, R.id.start, R.id.stop, R.id.send})
    public void onclickEvent(View view) {
        switch (view.getId()) {
            case R.id.open:
                if (mBluetoothAdapter == null) {
                    finish();
                    return;
                }
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                    messageAdapter.addMessage("打开蓝牙");
                }
                break;
            case R.id.close:
                if (mBluetoothAdapter == null) {
                    finish();
                    return;
                }
                mBluetoothAdapter.disable();
                messageAdapter.addMessage("打关闭牙");

                break;
            case R.id.start:
                if (mBluetoothAdapter != null) {
                    devicesAdapter.clearDevices();
                    mBluetoothAdapter.startDiscovery();
                    messageAdapter.addMessage("开始搜索");
                }
                break;
            case R.id.stop:
                if (mBluetoothAdapter != null) {
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                        messageAdapter.addMessage("停止搜索");
                        messageAdapter.clearMsgList();
                    }
                }
                break;
            case R.id.send:

                String msg = inputEt.getText().toString();
                if (TextUtils.isEmpty(msg)) {
                    Toast.makeText(this, "消息为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                messageAdapter.addMessage("发送消息：" + msg);
                if (connectThread != null) {//证明我主动去链接别人了
                    connectThread.write(msg);
                } else if (acceptThread != null) {
                    acceptThread.write(msg);
                }
                inputEt.setText("");
                break;
        }
    }

    //广播监听扫描到的设备
    class BlueToothStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (devicesAdapter != null) {
                        devicesAdapter.addDevice(device);
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    messageAdapter.addMessage("扫描结束....");
                    break;
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);

        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }


    class ConnectThread extends Thread {
        private BluetoothDevice mDevice;
        private BluetoothSocket mSocket;
        private InputStream btIs;
        private OutputStream btOs;
        private boolean canRecv;
        private PrintWriter writer;

        public ConnectThread(BluetoothDevice device) {
            mDevice = device;
            canRecv = true;
        }

        @Override
        public void run() {
            if (mDevice != null) {
                try {
                    //获取套接字
                    @SuppressLint("MissingPermission") BluetoothSocket temp = mDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                    //mDevice.createRfcommSocketToServiceRecord(UUID.fromString(BT_UUID));//sdk 2.3以下使用
                    mSocket = temp;
                    //发起连接请求
                    if (mSocket != null) {
                        mSocket.connect();
                    }
                    sendHandlerMsg("连接 " + mDevice.getName() + "成功！");
                    //获取输入输出流
                    btIs = mSocket.getInputStream();
                    btOs = mSocket.getOutputStream();

                    //通讯-接收消息
                    BufferedReader reader = new BufferedReader(new InputStreamReader(btIs, "UTF-8"));
                    String content = null;
                    while (canRecv) {
                        content = reader.readLine();
                        sendHandlerMsg("收到消息：" + content);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    sendHandlerMsg("错误：" + e.getMessage());
                } finally {
                    try {
                        if (mSocket != null) {
                            mSocket.close();
                        }
                        //btIs.close();//两个输出流都依赖socket，关闭socket即可
                        //btOs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        sendHandlerMsg("错误：" + e.getMessage());
                    }
                }
            }
        }

        private void sendHandlerMsg(String content) {
            Message msg = handler.obtainMessage();
            msg.what = 1001;
            msg.obj = content;
            handler.sendMessage(msg);
        }

        public void write(String msg) {
            if (btOs != null) {
                try {
                    if (writer == null) {
                        writer = new PrintWriter(new OutputStreamWriter(btOs, "UTF-8"), true);
                    }
                    writer.println(msg);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    writer.close();
                    sendHandlerMsg("错误：" + e.getMessage());
                }
            }
        }
    }

    class AcceptThread extends Thread {
        private BluetoothServerSocket mServerSocket;
        private BluetoothSocket mSocket;
        private InputStream btIs;
        private OutputStream btOs;
        private PrintWriter writer;
        private boolean canAccept;
        private boolean canRecv;


        public AcceptThread() {
            canAccept = true;
            canRecv = true;
        }

        @Override
        public void run() {
            try {
                //获取套接字
                BluetoothServerSocket temp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("TEST", UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                mServerSocket = temp;
                //监听连接请求 -- 作为测试，只允许连接一个设备
                if (mServerSocket != null) {
                    // while (canAccept) {
                    mSocket = mServerSocket.accept();
                    sendHandlerMsg("有客户端连接");
                    // }
                }
                //获取输入输出流
                btIs = mSocket.getInputStream();
                btOs = mSocket.getOutputStream();
                //通讯-接收消息
                BufferedReader reader = new BufferedReader(new InputStreamReader(btIs, "UTF-8"));
                String content = null;
                while (canRecv) {
                    content = reader.readLine();
                    sendHandlerMsg("收到消息：" + content);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (mSocket != null) {
                        mSocket.close();
                    }
                    // btIs.close();//两个输出流都依赖socket，关闭socket即可
                    // btOs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    sendHandlerMsg("错误：" + e.getMessage());
                }
            }
        }

        private void sendHandlerMsg(String content) {
            Message msg = handler.obtainMessage();
            msg.what = 1001;
            msg.obj = content;
            handler.sendMessage(msg);
        }

        public void write(String msg) {
            if (btOs != null) {
                try {
                    if (writer == null) {
                        writer = new PrintWriter(new OutputStreamWriter(btOs, "UTF-8"), true);
                    }
                    writer.println(msg);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    writer.close();
                    sendHandlerMsg("错误：" + e.getMessage());
                }
            }
        }
    }
}
