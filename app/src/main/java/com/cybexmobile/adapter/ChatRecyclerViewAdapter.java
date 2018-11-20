package com.cybexmobile.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybex.provider.graphene.chat.ChatMessage;
import com.cybexmobile.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder> {

    public static final int TYPE_MESSAGE = 1;
    public static final int TYPE_STATE = 2;

    private static final String TEXT_SPLIT = ": ";

    private Context mContext;
    private List<ChatMessage> mChatMessages;
    private int mType;
    private OnItemClickListener mListener;
    private String mUserName;

    public ChatRecyclerViewAdapter(Context context, String userName, List<ChatMessage> chatMessages) {
        this(context, TYPE_MESSAGE, userName, chatMessages);
    }

    public ChatRecyclerViewAdapter(Context context, int type, String userName, List<ChatMessage> chatMessages) {
        mContext = context;
        mChatMessages = chatMessages;
        mType = type;
        mUserName = userName;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public void setUsername(String username) {
        mUserName = username;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ChatMessage chatMessage = mChatMessages.get(position);
        if(chatMessage == null){
            return;
        }
        String username = parseUsername(chatMessage.getUserName(), chatMessage.getSigned());
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(username);
        ssb.append(TEXT_SPLIT);
        ssb.append(chatMessage.getMessage());
        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if(TextUtils.isEmpty(chatMessage.getUserName())){
                    return;
                }
                if(mListener != null){
                    mListener.onItemUserNameClick(widget, chatMessage);
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                if(!TextUtils.isEmpty(mUserName) && mUserName.equals(chatMessage.getUserName())) {
                    ds.setColor(mContext.getResources().getColor(R.color.primary_color_orange));
                }
                ds.setUnderlineText(false);
            }
        }, 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if(mListener != null){
                    mListener.onItemMessageClick(chatMessage);
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(mContext.getResources().getColor(R.color.font_color_white_dark));
                ds.setUnderlineText(false);
            }
        }, username.length() + TEXT_SPLIT.length(),
                username.length() + TEXT_SPLIT.length() + chatMessage.getMessage().length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.mTvMessage.setText(ssb);
        holder.mTvMessage.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public int getItemCount() {
        return mChatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mType;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_chat_message_tv_message) TextView mTvMessage;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /**
     * 用户名最多显示15位 | 用户名为空显示"游客"
     * @param username
     * @return
     */
    private String parseUsername(String username, int signed){
        if(signed == ChatMessage.SIGNED_FAILED){
            return "游客";
        }
        if(username.length() > 15){
            return username.substring(0, 15).concat("...");
        }
        return username;
    }

    public interface OnItemClickListener{
        void onItemUserNameClick(View view, ChatMessage message);
        void onItemMessageClick(ChatMessage message);
    }

}
