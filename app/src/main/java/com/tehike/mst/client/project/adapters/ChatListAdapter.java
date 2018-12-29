package com.tehike.mst.client.project.adapters;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.entity.SipBean;
import com.tehike.mst.client.project.linphone.SipManager;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.utils.TimeUtils;
import com.tehike.mst.client.project.utils.WriteLogToFile;

import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;

import java.util.List;

/**
 * Created by Root on 2018/7/23.
 * <p>
 * 显示当前sip列表的适配器
 *
 *
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyViewHolder> {

    /**
     * 上下文对象
     */
    Context context;

    /**
     * 数据集
     */
    List<SipBean> mList;

    /**
     * 聊天室对象
     */
    LinphoneChatRoom[] rooms;

    /**
     * 点击回调
     */
    private OnItemClickListener onItemClickListener;

    /**
     *
     * 点击设置回调事件
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 构造方法
     *
     * @param context
     * @param mList
     */
    public ChatListAdapter(Context context, List<SipBean> mList) {
        this.context = context;
        this.mList = mList;
        //获取当前对象与别的设备的聊天室
        if (SipService.isReady() || SipManager.isInstanceiated()) {
            rooms = SipManager.getLc().getChatRooms();
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_list_item_layout_port, parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.name.setText(mList.get(position).getName());
        try {
            //查询最后一条聊天记录
            String itemUser = mList.get(position).getNumber();
            for (int j = 0; j < rooms.length; j++) {
                String roomUser = rooms[j].getPeerAddress().getUserName();
                if (itemUser.equals(roomUser)) {
                    int size = rooms[j].getHistorySize();
                    if (size > 0) {
                        LinphoneChatMessage[] ms = rooms[j].getHistory();
                        String his = ms[ms.length - 1].getText();
                        holder.mess.setText(his);
                        holder.time.setText(TimeUtils.timeStamp2Date(ms[ms.length - 1].getTime() / 1000 + ""));
                    }
                    break;
                }
            }
        } catch (Exception e) {
            WriteLogToFile.info("获取聊天室的最后信息异常:"+e.getMessage());
        }
        //item点击事件
        if (onItemClickListener != null) {
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(mList.get(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {

        return mList.size() > 0 ? mList.size() : 0;
    }

    /**
     * 内部类
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView name;//sip名称
        private TextView mess;//最后 的消息
        private TextView time;//最后消息的时间
        private LinearLayout item;//当前item的布局

        public MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_sip_uesername_layout);
            mess = itemView.findViewById(R.id.last_mess_layout);
            time = itemView.findViewById(R.id.last_message_time_layout);
            item = itemView.findViewById(R.id.sip_list_item_layout);
        }
    }

    public interface OnItemClickListener {
        void onClick(SipBean sipClient);
    }
}
