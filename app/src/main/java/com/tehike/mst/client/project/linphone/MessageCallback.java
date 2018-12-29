package com.tehike.mst.client.project.linphone;

import org.linphone.core.LinphoneChatMessage;

/**
 * Created by Root on 2018/6/15.
 * 添加接收短消息 的回调
 */

public abstract class MessageCallback {

    public void receiverMessage(LinphoneChatMessage linphoneChatMessage) {
    }
}
