package com.tehike.mst.client.project.linphone;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.db.DbHelper;
import com.tehike.mst.client.project.entity.SipClient;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.ui.landactivity.LandChatActivity;
import com.tehike.mst.client.project.ui.landactivity.LandSingleCallActivity;
import com.tehike.mst.client.project.ui.portactivity.PortChatActivity;
import com.tehike.mst.client.project.ui.portactivity.PortSingleCallActivity;
import com.tehike.mst.client.project.utils.ActivityUtils;
import com.tehike.mst.client.project.utils.Logutil;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallStats;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactoryImpl;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneEvent;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneFriendList;
import org.linphone.core.LinphoneInfoMessage;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.SubscriptionState;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SipService extends Service implements LinphoneCoreListener {
    private static final String TAG = "SipService";
    private PendingIntent mKeepAlivePendingIntent;
    private static SipService instance;
    private static PhoneCallback sPhoneCallback;
    private static RegistrationCallback sRegistrationCallback;
    private static MessageCallback sMessageCallback;

    public static boolean isReady() {
        return instance != null;
    }

    SQLiteDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        LinphoneCoreFactoryImpl.instance();
        SipManager.createAndStart(SipService.this);
        instance = this;
        Intent intent = new Intent(this, KeepAliveHandler.class);
        mKeepAlivePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE)).setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60000, 60000, mKeepAlivePendingIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeAllCallback();
        SipManager.getLc().destroy();
        SipManager.destroy();
        ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE)).cancel(mKeepAlivePendingIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void addPhoneCallback(PhoneCallback phoneCallback) {
        sPhoneCallback = phoneCallback;
    }

    public static void removePhoneCallback() {
        if (sPhoneCallback != null) {
            sPhoneCallback = null;
        }
    }

    public static void addMessageCallback(MessageCallback messageCallback) {
        sMessageCallback = messageCallback;
    }

    public static void removeMessageCallback() {
        if (sMessageCallback != null) {
            sMessageCallback = null;
        }

    }

    public static void addRegistrationCallback(RegistrationCallback registrationCallback) {
        sRegistrationCallback = registrationCallback;
    }

    public static void removeRegistrationCallback() {
        if (sRegistrationCallback != null) {
            sRegistrationCallback = null;
        }
    }

    public void removeAllCallback() {
        removePhoneCallback();
        removeRegistrationCallback();
        removeMessageCallback();
    }

    @Override
    public void registrationState(LinphoneCore linphoneCore, LinphoneProxyConfig linphoneProxyConfig,
                                  LinphoneCore.RegistrationState registrationState, String s) {
        String state = registrationState.toString();
        if (sRegistrationCallback != null && state.equals(LinphoneCore.RegistrationState.RegistrationNone.toString())) {
            sRegistrationCallback.registrationNone();
            Logutil.i("registrationNone");
        } else if (sRegistrationCallback != null && state.equals(LinphoneCore.RegistrationState.RegistrationProgress.toString())) {
            //  sRegistrationCallback.registrationProgress();
        } else if (sRegistrationCallback != null && state.equals(LinphoneCore.RegistrationState.RegistrationOk.toString())) {
            AppConfig.SIP_STATUS = true;
            sRegistrationCallback.registrationOk();
            Logutil.i("Ok");
        } else if (sRegistrationCallback != null && state.equals(LinphoneCore.RegistrationState.RegistrationCleared.toString())) {
            sRegistrationCallback.registrationCleared();
            Logutil.i("registrationCleared");
        } else if (sRegistrationCallback != null && state.equals(LinphoneCore.RegistrationState.RegistrationFailed.toString())) {
            AppConfig.SIP_STATUS = false;
            sRegistrationCallback.registrationFailed();
            Logutil.i("registrationFailed");
        }
    }

    @Override
    public void callState(final LinphoneCore linphoneCore, final LinphoneCall linphoneCall, LinphoneCall.State state, String s) {
        if (state == LinphoneCall.State.IncomingReceived && sPhoneCallback != null) {
            sPhoneCallback.incomingCall(linphoneCall);
            boolean isSupportVideo = linphoneCall.getRemoteParams().getVideoEnabled();
            Logutil.i("当前来是否是可视电话:" + isSupportVideo);
            String str = "有电话打进来了：" + linphoneCall.getRemoteAddress().getPort() + "--" + linphoneCall.getRemoteAddress().getDisplayName();
            Log.i("TAG", "callState = " + str);
            try {
                Linphone.acceptCall();
                Linphone.toggleSpeaker(true);
                linphoneCore.acceptCall(linphoneCall);
                Intent intent = null;
                int direction = AppConfig.APP_DIRECTION;
                Logutil.i(AppConfig.APP_DIRECTION + "方向");
                if (direction == 1) {
                    intent = new Intent(SipService.this, PortSingleCallActivity.class);
                } else if (direction == 2) {
                    intent = new Intent(SipService.this, LandSingleCallActivity.class);
                }
                intent.putExtra("callerNumber", linphoneCall.getRemoteAddress().getUserName());
                intent.putExtra("isCallConnected", true);
                intent.putExtra("isVideoCall", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (LinphoneCoreException e) {
                e.printStackTrace();
            }
        }

        if (state == LinphoneCall.State.OutgoingInit && sPhoneCallback != null) {
            sPhoneCallback.outgoingInit();
        }

        if (state == LinphoneCall.State.Connected && sPhoneCallback != null) {
            sPhoneCallback.callConnected();
        }

        if (state == LinphoneCall.State.Error && sPhoneCallback != null) {
            sPhoneCallback.error();
        }

        if (state == LinphoneCall.State.CallEnd && sPhoneCallback != null) {
            sPhoneCallback.callEnd();
        }

        if (state == LinphoneCall.State.CallReleased && sPhoneCallback != null) {
            sPhoneCallback.callReleased();
        }
    }

    @Override
    public void authInfoRequested(LinphoneCore linphoneCore, String s, String s1, String s2) {

    }

    @Override
    public void authenticationRequested(LinphoneCore linphoneCore, LinphoneAuthInfo linphoneAuthInfo, LinphoneCore.AuthMethod authMethod) {

    }

    @Override
    public void callStatsUpdated(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCallStats linphoneCallStats) {

    }

    @Override
    public void newSubscriptionRequest(LinphoneCore linphoneCore, LinphoneFriend linphoneFriend, String s) {

    }

    @Override
    public void notifyPresenceReceived(LinphoneCore linphoneCore, LinphoneFriend linphoneFriend) {

    }

    @Override
    public void dtmfReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, int i) {

    }

    @Override
    public void notifyReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneAddress linphoneAddress, byte[] bytes) {

    }

    @Override
    public void transferState(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCall.State state) {

    }

    @Override
    public void infoReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneInfoMessage linphoneInfoMessage) {

    }

    @Override
    public void subscriptionStateChanged(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, SubscriptionState subscriptionState) {

    }

    @Override
    public void publishStateChanged(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, PublishState publishState) {

    }

    @Override
    public void show(LinphoneCore linphoneCore) {

    }

    @Override
    public void displayStatus(LinphoneCore linphoneCore, String s) {

    }

    @Override
    public void displayMessage(LinphoneCore linphoneCore, String s) {

    }

    @Override
    public void displayWarning(LinphoneCore linphoneCore, String s) {

    }

    @Override
    public void fileTransferProgressIndication(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, int i) {

    }

    @Override
    public void fileTransferRecv(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, byte[] bytes, int i) {

    }

    @Override
    public int fileTransferSend(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, ByteBuffer byteBuffer, int i) {
        return 0;
    }

    @Override
    public void callEncryptionChanged(LinphoneCore linphoneCore, LinphoneCall linphoneCall, boolean b, String s) {

    }

    @Override
    public void isComposingReceived(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom) {

    }

    @Override
    public void ecCalibrationStatus(LinphoneCore linphoneCore, LinphoneCore.EcCalibratorStatus ecCalibratorStatus, int i, Object o) {

    }

    @Override
    public void globalState(LinphoneCore linphoneCore, LinphoneCore.GlobalState globalState, String s) {

    }

    @Override
    public void uploadProgressIndication(LinphoneCore linphoneCore, int i, int i1) {

    }

    @Override
    public void uploadStateChanged(LinphoneCore linphoneCore, LinphoneCore.LogCollectionUploadState logCollectionUploadState, String s) {

    }

    @Override
    public void friendListCreated(LinphoneCore linphoneCore, LinphoneFriendList linphoneFriendList) {

    }

    @Override
    public void friendListRemoved(LinphoneCore linphoneCore, LinphoneFriendList linphoneFriendList) {

    }

    @Override
    public void networkReachableChanged(LinphoneCore linphoneCore, boolean b) {

    }

    @Override
    public void messageReceived(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom, LinphoneChatMessage linphoneChatMessage) {


        //接收短消息 的回调
        if (sMessageCallback != null) {
            sMessageCallback.receiverMessage(linphoneChatMessage);
        }


        //当前消息的内容信息
        String mess = linphoneChatMessage.getText();
        final String from = linphoneChatMessage.getFrom().getUserName();
        final String to = linphoneChatMessage.getTo().getUserName();
        String messTime = new Date().toString();
        //接收消息并存入数据库
        DbHelper databaseHelper = new DbHelper(App.getApplication());
        db = databaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("time", messTime);
        contentValues.put("fromUser", from);
        contentValues.put("mess", mess);
        contentValues.put("toUser", to);
        db.insert("chatHistory", null, contentValues);

        //过滤要显示的Activity
        String activityName = ActivityUtils.getTopActivity().getClass().getName();
        if (!TextUtils.isEmpty(activityName)) {
            if (activityName.equals("com.tehike.mst.client.project.ui.portactivity.PortChatActivity") || activityName.equals("com.tehike.mst.client.project.ui.landactivity.LandChatActivity") || activityName.equals("com.tehike.client.mst.app.project.ui.landactivity.LandChatListActivity")) {
                return;
            }
        }
        //判断是否有悬浮窗口权限
        if (AppConfig.ARGEE_OVERLAY_PERMISSION)
            disPlayShortMess(from, mess);
    }


    @Override
    public void messageReceivedUnableToDecrypted(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom, LinphoneChatMessage linphoneChatMessage) {

    }

    @Override
    public void notifyReceived(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, String s, LinphoneContent linphoneContent) {

    }

    @Override
    public void configuringStatus(LinphoneCore linphoneCore, LinphoneCore.RemoteProvisioningState remoteProvisioningState, String s) {

    }

    /**
     * 新消息提示
     *
     * @param from
     * @param mess
     */
    private void disPlayShortMess(final String from, String mess) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(App.getApplication());
//        builder.setTitle("新消息");
        View view = View.inflate(App.getApplication(), R.layout.view_receiveralarm_layout, null);
        TextView showInformation = view.findViewById(R.id.prompt_alarm_information_layout);
        showInformation.setText("\u3000\u3000类型:短消息\n" + "\u3000\u3000消息来源:" + from + "\n" + "\u3000\u3000内容:" + mess);

        builder.setView(view);
//        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
        final Dialog dialog = builder.create();
        dialog.getWindow().setLayout(300, 60);
        dialog.getWindow().setGravity(Gravity.TOP);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                SipClient sipClient = new SipClient();
                sipClient.setUsrname(from);
                if (AppConfig.APP_DIRECTION == 2)
                    intent.setClass(App.getApplication(), LandChatActivity.class);
                else
                    intent.setClass(App.getApplication(), PortChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("sipclient", sipClient);
                intent.putExtras(bundle);
                App.getApplication().startActivity(intent);
                if (dialog.isShowing())
                    dialog.dismiss();
            }
        });
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (dialog.isShowing())
                    dialog.dismiss();
            }
        };
        timer.schedule(timerTask, 3000);

    }


}
