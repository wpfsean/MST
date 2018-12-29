
package com.tehike.mst.client.project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.entity.ChatMsgEntity;

import java.util.List;

/**
 * 描述：消息适配器
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/10/26 17:23
 */

public class ChatMsgViewAdapter extends BaseAdapter {


    //区分自己还是别人的消息
    public static interface IMsgViewType {
        int IMVT_COM_MSG = 0;
        int IMVT_TO_MSG = 1;
    }

    //消息集合列表
    private List<ChatMsgEntity> coll;

    //上下文
    private Context ctx;

    //布局加载对象
    private LayoutInflater mInflater;

    //构造函数
    public ChatMsgViewAdapter(Context context, List<ChatMsgEntity> coll) {
        ctx = context;
        this.coll = coll;
        mInflater = LayoutInflater.from(context);
    }

    //消息数量
    public int getCount() {
        return coll.size();
    }

    public Object getItem(int position) {
        return coll.get(position);
    }

    public long getItemId(int position) {
        return position;
    }


    //获取消息对象类型
    public int getItemViewType(int position) {
        ChatMsgEntity entity = coll.get(position);
        if (entity.getMsgType()) {
            return IMsgViewType.IMVT_COM_MSG;
        } else {
            return IMsgViewType.IMVT_TO_MSG;
        }
    }


    public int getViewTypeCount() {
        return 2;
    }


    public View getView(int position, View convertView, ViewGroup parent) {

        ChatMsgEntity entity = coll.get(position);
        boolean isComMsg = entity.getMsgType();

        ViewHolder viewHolder = null;
        if (convertView == null) {
            if (isComMsg) {
                convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left, null);
            } else {
                convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right, null);
            }
            viewHolder = new ViewHolder();
            viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
            viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tv_username);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent);
            viewHolder.isComMsg = isComMsg;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvSendTime.setText(entity.getDate());
        viewHolder.tvUserName.setText(entity.getName());
        viewHolder.tvContent.setText(entity.getText());
        return convertView;
    }

    //内部类
    static class ViewHolder {
        public TextView tvSendTime;
        public TextView tvUserName;
        public TextView tvContent;
        public boolean isComMsg = true;
    }
}
