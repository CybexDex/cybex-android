package com.cybexmobile.activity.chat;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.utils.SoftKeyBoardListener;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chat.ChatLogin;
import com.cybex.provider.graphene.chat.ChatMessage;
import com.cybex.provider.graphene.chat.ChatMessageRequest;
import com.cybex.provider.graphene.chat.ChatMessages;
import com.cybex.provider.graphene.chat.ChatReply;
import com.cybex.provider.graphene.chat.ChatRequest;
import com.cybex.provider.graphene.websocket.WebSocketFailure;
import com.cybex.provider.graphene.websocket.WebSocketMessage;
import com.cybex.provider.graphene.websocket.WebSocketOpen;
import com.cybex.provider.graphene.chat.ChatSubscribe;
import com.cybex.basemodule.BitsharesWalletWraper;
import com.cybex.provider.websocket.chat.RxChatWebSocket;
import com.cybexmobile.R;
import com.cybexmobile.activity.login.LoginActivity;
import com.cybexmobile.activity.setting.enotes.SetCloudPasswordActivity;
import com.cybexmobile.adapter.ChatRecyclerViewAdapter;
import com.cybexmobile.adapter.decoration.VisibleDividerItemDecoration;
import com.cybexmobile.utils.DeviceUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.OnTouch;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_CHANNEL;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_CHANNEL_TITLE;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_NAME;
import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
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
    @BindView(R.id.chat_pb_loading)
    ProgressBar mPbChatLoading;

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
    private String mChannelTitle;
    private boolean mIsLogin;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Unbinder mUnbinder;
    private WebSocketService mWebSocketService;
    private FullAccountObject mFullAccountObject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        SoftKeyBoardListener.setListener(this, this);
        mAccountName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        mIsLogin = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_IS_LOGIN_IN, false);
        mChannel = getIntent().getStringExtra(INTENT_PARAM_CHANNEL);
        mChannelTitle = getIntent().getStringExtra(INTENT_PARAM_CHANNEL_TITLE);
        mTvTitle.setText(mChannelTitle);
        if(!mIsLogin){
            mTvSendNormal.setText(getResources().getString(R.string.action_sign_in));
            mTvSendForced.setText(getResources().getString(R.string.action_sign_in));
            mTvSendNormal.setEnabled(true);
            mTvSendForced.setEnabled(true);
        }
        initRecyclerView();
        initChatWebSocket();
        bindService();
    }

    private void bindService() {
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void initRecyclerView() {
        mChatOnScrollListener = new ChatOnScrollListener();
        mRvChatMessage.addOnScrollListener(mChatOnScrollListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        linearLayoutManager.setSmoothScrollbarEnabled(true);
        mRvChatMessage.setLayoutManager(linearLayoutManager);
        mRvChatMessage.addItemDecoration(new VisibleDividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mChatRecyclerViewAdapter = new ChatRecyclerViewAdapter(this, mAccountName, mChatMessages);
        mChatRecyclerViewAdapter.setOnItemClickListener(this);
        mRvChatMessage.setAdapter(mChatRecyclerViewAdapter);
    }

    private void initChatWebSocket() {
        mRxChatWebSocket = new RxChatWebSocket(RxChatWebSocket.CHAT_URL);
        mCompositeDisposable.add(mRxChatWebSocket.onOpen()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WebSocketOpen>() {
                    @Override
                    public void accept(WebSocketOpen webSocketOpen) throws Exception {
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
                .subscribe(new Consumer<WebSocketFailure>() {
                    @Override
                    public void accept(WebSocketFailure webSocketFailure) throws Exception {
                        Log.d(RxChatWebSocket.TAG, "正在重新建立连接...");
                        mPbChatLoading.setVisibility(View.VISIBLE);
                        //重连
                        mRxChatWebSocket.reconnect(3, TimeUnit.SECONDS);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
        mCompositeDisposable.add(mRxChatWebSocket.onSubscribe()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WebSocketMessage>() {
                    @Override
                    public void accept(WebSocketMessage chatSocketMessage) throws Exception {
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
                            } else {
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
        mPbChatLoading.setVisibility(View.VISIBLE);
    }

    private void notifyUI(LinkedList<ChatSubscribe<ChatReply>> subscribeReplies,
                          LinkedList<ChatSubscribe<ChatMessages>> subscribeMessages) {
        if(subscribeReplies != null && subscribeReplies.size() > 0){
            //登录回应时 清空所有消息以防重连消息重复显示
            if(subscribeReplies.getFirst().getType() == ChatSubscribe.TYPE_LOGIN_REPLY){
                mPbChatLoading.setVisibility(View.GONE);
                mChatMessages.clear();
                mChatRecyclerViewAdapter.notifyDataSetChanged();
            }
            mTvTitle.setText(String.format(Locale.ENGLISH, "%s(%d)", mChannelTitle, subscribeReplies.getLast().getOnline()));
        }
        if(subscribeMessages != null && subscribeMessages.size() > 0){
            mTvTitle.setText(String.format(Locale.ENGLISH, "%s(%d)", mChannelTitle, subscribeMessages.getLast().getOnline()));
            for(ChatSubscribe<ChatMessages> subscribeMessage : subscribeMessages){
                mChatMessages.addAll(subscribeMessage.getData().getMessages());
            }
            //插入动画效果
            mChatRecyclerViewAdapter.notifyItemInserted(mChatMessages.size() - 1);
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
        unbindService(mConnection);
        mRxChatWebSocket.close(1000, "close");
        mCompositeDisposable.dispose();
        mUnbinder.unbind();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            mAccountName = data.getStringExtra(INTENT_PARAM_NAME);
            mIsLogin = data.getBooleanExtra(INTENT_PARAM_LOGIN_IN, false);
            mTvSendNormal.setText(getResources().getString(R.string.text_chat_send));
            mTvSendForced.setText(getResources().getString(R.string.text_chat_send));
            mTvSendNormal.setEnabled(!TextUtils.isEmpty(mTvMessageNormal.getText().toString()));
            mTvSendForced.setEnabled(!TextUtils.isEmpty(mTvMessageNormal.getText().toString()));
            mChatRecyclerViewAdapter.setUsername(mAccountName);
            mChatRecyclerViewAdapter.notifyDataSetChanged();
        } else if (requestCode == Constant.REQUEST_CODE_UPDATE_ACCOUNT && resultCode == Constant.RESULT_CODE_UPDATE_ACCOUNT ) {
            mFullAccountObject = mWebSocketService.getFullAccount(mAccountName);
        }
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
        mTvMessageLength.setTextColor(getResources().getColor(editable.toString().length() < 100 ? R.color.primary_color_grey : R.color.btn_red_end));
        mTvMessageLength.setText(String.format("%s/100", editable.length()));
        if (mIsLogin) {
            mTvSendForced.setEnabled(!TextUtils.isEmpty(editable.toString()));
            mTvSendNormal.setEnabled(!TextUtils.isEmpty(editable.toString()));
        }
    }

    @OnClick(R.id.chat_tv_message_normal)
    public void onMessageNormalClick(View view){
        mEtMessageForced.requestFocus();
        showSoftInput(mEtMessageForced);
    }

    @OnClick({R.id.chat_tv_send_normal, R.id.chat_tv_send_forced})
    public void onSendClick(View view){
        if(!mIsLogin){ //未登录
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 1);
            return;
        }
        if (isLoginFromENotes() && mFullAccountObject.account.active.key_auths.size() < 2) {
            CybexDialog.showLimitOrderCancelConfirmationDialog(
                    ChatActivity.this,
                    getResources().getString(R.string.nfc_dialog_add_cloud_password_content),
                    getResources().getString(R.string.nfc_dialog_add_cloud_password_button),
                    new CybexDialog.ConfirmationDialogClickListener() {
                        @Override
                        public void onClick(Dialog dialog) {
                            Intent intent = new Intent(ChatActivity.this, SetCloudPasswordActivity.class);
                            startActivityForResult(intent, Constant.REQUEST_CODE_UPDATE_ACCOUNT);
                        }
                    });
            return;
        }
        if(mRxChatWebSocket == null || !mRxChatWebSocket.isConnected()){
            return;
        }
        String message = mEtMessageForced.getText().toString();
        //过滤空白字符
        if(message.matches("[\\s]*")){
            Toast.makeText(this, getResources().getString(R.string.text_toast_unable_to_send_blank_message), Toast.LENGTH_SHORT).show();
            return;
        }
        if(view.getId() == R.id.chat_tv_send_forced && (!mSwitchAnonymouslyForced.isChecked() || !BitsharesWalletWraper.getInstance().is_locked())){
            hideSoftInput(mEtMessageForced);
        }
        checkWalletLockedAndSendMessage(message);
    }

    @OnClick(R.id.chat_tv_new_message_count)
    public void onNewMessageClick(View view) {
        mIsScrollToBottom = true;
        scrollToLastPosition();
    }

    /**
     * 验证钱包是否锁定 解锁成功发送消息
     * @param message
     */
    private void checkWalletLockedAndSendMessage(String message) {
        if(!mSwitchAnonymouslyForced.isChecked() || !BitsharesWalletWraper.getInstance().is_locked()){
            toSendMessage(message);
            return;
        }
        if(mFullAccountObject == null) {
            mFullAccountObject = mWebSocketService.getFullAccount(mAccountName);
        }
        CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mFullAccountObject.account, mAccountName,
                new UnlockDialog.UnLockDialogClickListener() {
                    @Override
                    public void onUnLocked(String password) {
                        toSendMessage(message);
                    }
                }, new UnlockDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(int result) {
                        hideSoftInput(mEtMessageForced);
                    }
                });
    }

    /**
     * 签名并发送消息
     * @param message
     */
    private void toSendMessage(String message) {
        //签名加密耗时之前把输入框清空 以防点击发送按钮卡顿
        mTvMessageNormal.setText("");
        mEtMessageForced.setText("");
        if(mFullAccountObject == null) {
            mFullAccountObject = mWebSocketService.getFullAccount(mAccountName);
        }
        mCompositeDisposable.add(Single.fromCallable(new Callable<ChatRequest<ChatMessageRequest>>() {
                    @Override
                    public ChatRequest<ChatMessageRequest> call() {
                        ChatMessageRequest chatMessageRequest = new ChatMessageRequest();
                        chatMessageRequest.setMessage(message);
                        chatMessageRequest.setUserName(mAccountName);
                        chatMessageRequest.setSign("");
                        //判断是否实名
                        if(mSwitchAnonymouslyForced.isChecked() && mFullAccountObject != null){
                            chatMessageRequest.setSign(BitsharesWalletWraper.getInstance().getChatMessageSignature(
                                    mFullAccountObject.account, String.format("%s_%s", mAccountName, message)));
                        }
                        return new ChatRequest<>(ChatRequest.TYPE_MESSAGE, chatMessageRequest);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ChatRequest<ChatMessageRequest>>() {
                    @Override
                    public void accept(ChatRequest<ChatMessageRequest> chatRequest) throws Exception {
                        sendMessage(chatRequest);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));

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
        if(mChatMessages.size() > 0){
            mRvChatMessage.smoothScrollToPosition(mChatMessages.size() - 1);
        }
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
        ChatLogin chatLogin = new ChatLogin(mChannel, "100", DeviceUtils.getAndroidId(this));
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
                        if(aBoolean){

                        }
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

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            if(mIsLogin){
                mFullAccountObject = mWebSocketService.getFullAccount(mAccountName);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

}
