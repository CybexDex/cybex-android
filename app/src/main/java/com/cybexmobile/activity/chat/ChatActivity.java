package com.cybexmobile.activity.chat;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.utils.SoftKeyBoardListener;
import com.cybex.provider.graphene.chat.ChatLogin;
import com.cybex.provider.graphene.chat.ChatMessage;
import com.cybex.provider.graphene.chat.ChatMessageRequest;
import com.cybex.provider.graphene.chat.ChatMessages;
import com.cybex.provider.graphene.chat.ChatReply;
import com.cybex.provider.graphene.chat.ChatRequest;
import com.cybex.provider.graphene.chat.ChatSocketFailure;
import com.cybex.provider.graphene.chat.ChatSocketMessage;
import com.cybex.provider.graphene.chat.ChatSocketOpen;
import com.cybex.provider.graphene.chat.ChatSubscribe;
import com.cybex.provider.websocket.chat.RxChatWebSocket;
import com.cybexmobile.R;
import com.cybexmobile.adapter.ChatRecyclerViewAdapter;
import com.cybexmobile.adapter.decoration.VisibleDividerItemDecoration;
import com.cybexmobile.utils.DeviceUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.OnTouch;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_CHANNEL;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;

public class ChatActivity extends BaseActivity implements SoftKeyBoardListener.OnSoftKeyBoardChangeListener,
        ChatRecyclerViewAdapter.OnItemClickListener {

    private static final String TAG = ChatActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.chat_tv_title)
    TextView mTvTitle;
    @BindView(R.id.chat_rv_chat_message)
    RecyclerView mRvChatMessage;
    @BindView(R.id.chat_tv_message_normal)
    TextView mTvMessageNormal;
    @BindView(R.id.chat_tv_send_normal)
    TextView mTvSendNormal;
    @BindView(R.id.chat_et_message_forced)
    EditText mEtMessageForced;
    @BindView(R.id.chat_tv_send_forced)
    TextView mTvSendForced;
    @BindView(R.id.chat_cb_anonymously_forced)
    SwitchCompat mSwitchAnonymouslyForced;
    @BindView(R.id.chat_layout_input_forced)
    LinearLayout mLayoutInputForced;
    @BindView(R.id.chat_tv_message_length)
    TextView mTvMessageLength;
    @BindView(R.id.chat_tv_new_message_count)
    TextView mTvNewMessageCount;

    private ChatRecyclerViewAdapter mChatRecyclerViewAdapter;
    private List<ChatMessage> mChatMessages = new ArrayList<>();

    private int mLastCompletelyVisibleItemPosition;
    //是否滑动至底部
    private boolean mIsScrollToBottom = true;
    //未读聊天数据计数
    private int mNewChatMessageCount;

    private ChatOnScrollListener mChatOnScrollListener;
    private PopupWindow mPopWindow;
    private TextView mTvUsername;

    private RxChatWebSocket mRxChatWebSocket;
    private String mAccountName;
    private String mChannel;//频道

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Unbinder mUnbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        SoftKeyBoardListener.setListener(this, this);
        mAccountName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        mChannel = getIntent().getStringExtra(INTENT_PARAM_CHANNEL);
        mTvTitle.setText(mChannel);
        initRecyclerView();
        //申请必要权限
        mCompositeDisposable.add(new RxPermissions(this)
                .request(Manifest.permission.READ_PHONE_STATE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if(granted){
                            initRecyclerView();
                            initChatWebSocket();
                        } else {
                            finish();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        finish();
                    }
                }));
    }

    private void initRecyclerView() {
        mChatOnScrollListener = new ChatOnScrollListener();
        mRvChatMessage.addOnScrollListener(mChatOnScrollListener);
        mRvChatMessage.addItemDecoration(new VisibleDividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mChatRecyclerViewAdapter = new ChatRecyclerViewAdapter(this, mAccountName, DeviceUtils.getDeviceID(this), mChatMessages);
        mChatRecyclerViewAdapter.setOnItemClickListener(this);
        mRvChatMessage.setAdapter(mChatRecyclerViewAdapter);
    }

    private void initChatWebSocket() {
        mRxChatWebSocket = new RxChatWebSocket("ws://47.91.242.71:9099/ws");
        mCompositeDisposable.add(mRxChatWebSocket.onOpen()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ChatSocketOpen>() {
                    @Override
                    public void accept(ChatSocketOpen chatSocketOpen) throws Exception {
                        chatSocketLogin();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                }));
        mCompositeDisposable.add(mRxChatWebSocket.onFailure()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ChatSocketFailure>() {
                    @Override
                    public void accept(ChatSocketFailure chatSocketFailure) throws Exception {
                        Log.d(RxChatWebSocket.TAG, "正在重新建立连接...");
                        //重连
                        mRxChatWebSocket.connect();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
        mCompositeDisposable.add(mRxChatWebSocket.onSubscribe()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ChatSocketMessage>() {
                    @Override
                    public void accept(ChatSocketMessage chatSocketMessage) throws Exception {
                        if (!chatSocketMessage.isText()) {
                            return;
                        }
                        Log.d(RxChatWebSocket.TAG, chatSocketMessage.getText());
                        String[] messages = chatSocketMessage.getText().split("\\n");
                        Gson gson = new GsonBuilder().create();
                        LinkedList<ChatSubscribe<ChatMessages>> subscribeMessages = new LinkedList<>();
                        LinkedList<ChatSubscribe<ChatReply>> subscribeReplies = new LinkedList<>();
                        for(String message : messages){
                            JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();
                            if(jsonObject.get("type").getAsInt() == ChatSubscribe.TYPE_MESSAGE){
                                subscribeMessages.add(gson.fromJson(message, new TypeToken<ChatSubscribe<ChatMessages>>(){}.getType()));
                            } else if(jsonObject.get("type").getAsInt() == ChatSubscribe.TYPE_REPLY){
                                subscribeReplies.add(gson.fromJson(message, new TypeToken<ChatSubscribe<ChatReply>>(){}.getType()));
                            }
                        }
                        notifyUI(subscribeReplies, subscribeMessages);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
        mRxChatWebSocket.connect();
    }

    private void notifyUI(LinkedList<ChatSubscribe<ChatReply>> subscribeReplies,
                          LinkedList<ChatSubscribe<ChatMessages>> subscribeMessages) {
        if(subscribeReplies != null && subscribeReplies.size() > 0){
            mTvTitle.setText(String.format(Locale.ENGLISH, "%s(%d)", mChannel, subscribeReplies.getLast().getOnline()));
        }
        if(subscribeMessages != null && subscribeMessages.size() > 0){
            mTvTitle.setText(String.format(Locale.ENGLISH, "%s(%d)", mChannel, subscribeMessages.getLast().getOnline()));
            for(ChatSubscribe<ChatMessages> subscribeMessage : subscribeMessages){
                mChatMessages.addAll(subscribeMessage.getData().getMessages());
            }
            mChatRecyclerViewAdapter.notifyDataSetChanged();
            if(mIsScrollToBottom){
                hidePopup();
                scrollToLastPosition();
            } else {
                ++mNewChatMessageCount;
                showHintNewMessageView();
                mTvNewMessageCount.setText(String.format(getResources().getString(R.string.text_new_message), mNewChatMessageCount));
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRvChatMessage.removeOnScrollListener(mChatOnScrollListener);
        mUnbinder.unbind();
        Disposable disposable = mRxChatWebSocket.close(1000, "close")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
        //mCompositeDisposable.dispose();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void keyBoardShow(int height) {
        //弹出键盘 显示编辑框并滚动至底部
        mLayoutInputForced.setVisibility(View.VISIBLE);
        scrollToLastPosition();
    }

    @Override
    public void keyBoardHide(int height) {
        //隐藏键盘 输入框失去焦点并隐藏编辑框
        if(mEtMessageForced.isFocused()){
            mEtMessageForced.clearFocus();
        }
        mLayoutInputForced.setVisibility(View.GONE);
    }

    @Override
    public void onItemUserNameClick(View view, ChatMessage message) {
        showPopup(view, message.getUserName());
    }

    @Override
    public void onItemMessageClick(ChatMessage message) {
        hideSoftInput(mEtMessageForced);
    }

    @OnTouch(R.id.chat_rv_chat_message)
    public boolean onTouchEvent(View view, MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            hideSoftInput(mEtMessageForced);
        }
        return false;
    }

    @OnTextChanged(value = R.id.chat_et_message_forced, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onMessageTextChanged(Editable editable){
        mTvMessageNormal.setText(editable.toString());
        mTvMessageLength.setText(String.format("%s/100", editable.length()));
    }

    @OnClick(R.id.chat_tv_message_normal)
    public void onMessageNormalClick(View view){
        mEtMessageForced.requestFocus();
        showSoftInput(mEtMessageForced);
    }

    @OnClick({R.id.chat_tv_send_normal, R.id.chat_tv_send_forced})
    public void onSendClick(View view){
        String message = mEtMessageForced.getText().toString();
        if(TextUtils.isEmpty(message)){
            return;
        }
        ChatMessageRequest chatMessageRequest = new ChatMessageRequest();
        chatMessageRequest.setMessage(message);
        chatMessageRequest.setUserName(mAccountName);
        chatMessageRequest.setSign("");
        ChatRequest<ChatMessageRequest> chatRequest = new ChatRequest<>(ChatRequest.TYPE_MESSAGE, chatMessageRequest);
        sendMessage(chatRequest);
        if(view.getId() == R.id.chat_tv_send_forced){
            hideSoftInput(mEtMessageForced);
        }
        mTvMessageNormal.setText("");
        mEtMessageForced.setText("");
    }

    @OnClick(R.id.chat_tv_new_message_count)
    public void onNewMessageClick(View view) {
        mIsScrollToBottom = true;
        scrollToLastPosition();
    }

    /**
     * 隐藏键盘
     * @param editText 获取焦点
     */
    private void hideSoftInput(EditText editText){
        InputMethodManager manager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert manager != null;
        manager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 弹出键盘
     * @param editText 获取焦点
     */
    private void showSoftInput(EditText editText){
        InputMethodManager manager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert manager != null;
        manager.showSoftInput(editText, 0);
    }

    /**
     * 滑动至底部
     * 关闭新消息弹出框
     */
    private void scrollToLastPosition() {
        hideHintNewMessageView();
        mRvChatMessage.scrollToPosition(mChatMessages.size() - 1);
    }

    /**
     * 隐藏新消息提示
     */
    private void hideHintNewMessageView() {
        mNewChatMessageCount = 0;
        mTvNewMessageCount.setVisibility(View.GONE);
    }

    /**
     * 显示提示有多少条新消息
     */
    private void showHintNewMessageView() {
        mTvNewMessageCount.setVisibility(View.VISIBLE);
    }

    /**
     * 弹出Popup
     * @param anchor 目标视图
     * @param username 用户名
     */
    private void showPopup(View anchor, String username){
        if(mPopWindow == null){
            View view = LayoutInflater.from(this).inflate(R.layout.item_popup_chat_username, null, false);
            mTvUsername = view.findViewById(R.id.popup_chat_message_tv_message);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mPopWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mPopWindow.setTouchable(true);
        }
        mTvUsername.setText(username);
        mPopWindow.showAsDropDown(anchor, 20, - mPopWindow.getContentView().getMeasuredHeight() - anchor.getMeasuredHeight() + anchor.getPaddingTop());
    }

    /**
     * 隐藏Popup
     */
    private void hidePopup(){
        if(mPopWindow != null && mPopWindow.isShowing()){
            mPopWindow.dismiss();
        }
    }

    /**
     * ChatWebSocket登录
     */
    private void chatSocketLogin(){
        ChatLogin chatLogin = new ChatLogin(mChannel, "100", DeviceUtils.getDeviceID(this));
        ChatRequest<ChatLogin> chatRequest = new ChatRequest<>(ChatRequest.TYPE_LOGIN, chatLogin);
        mCompositeDisposable.add(mRxChatWebSocket.sendMessage(chatRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        Log.d(RxChatWebSocket.TAG, aBoolean ? "ChatWebSocket登录发送成功" : "ChatWebSocket登录发送失败");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(RxChatWebSocket.TAG, "ChatWebSocket登录发送失败");
                        Log.e(RxChatWebSocket.TAG, throwable.getMessage());
                    }
                }));
    }

    /**
     * 发送消息
     * @param chatRequest 消息内容
     */
    private void sendMessage(final ChatRequest<ChatMessageRequest> chatRequest) {
        mCompositeDisposable.add(mRxChatWebSocket.sendMessage(chatRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        Log.d(RxChatWebSocket.TAG, aBoolean ? "ChatWebSocket消息发送成功" : "ChatWebSocket消息发送失败");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(RxChatWebSocket.TAG, "ChatWebSocket消息发送失败");
                        Log.e(RxChatWebSocket.TAG, throwable.getMessage());
                    }
                }));
    }

    private class ChatOnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (visibleItemCount > 0 && mLastCompletelyVisibleItemPosition >= totalItemCount - 1) {
                    mIsScrollToBottom = true;
                    hideHintNewMessageView();
                } else {
                    mIsScrollToBottom = false;
                }
            }

        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            mLastCompletelyVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();

        }
    }

}
