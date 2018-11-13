package com.cybexmobile.activity.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.utils.SoftKeyBoardListener;
import com.cybex.provider.graphene.chat.ChatMessage;
import com.cybexmobile.R;
import com.cybexmobile.adapter.ChatRecyclerViewAdapter;
import com.cybexmobile.adapter.decoration.VisibleDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import butterknife.OnTouch;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_CHANNEL;

public class ChatActivity extends BaseActivity implements SoftKeyBoardListener.OnSoftKeyBoardChangeListener,
        ChatRecyclerViewAdapter.OnItemClickListener {

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
    private boolean mIsScrollToBottom;
    //未读聊天数据计数
    private int mNewChatMessageCount;

    private ChatOnScrollListener mChatOnScrollListener;
    private PopupWindow mPopWindow;
    private TextView mTvUsername;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        SoftKeyBoardListener.setListener(this, this);
        mTvTitle.setText(getIntent().getStringExtra(INTENT_PARAM_CHANNEL));
        mChatMessages.add(new ChatMessage("wh23456", "求大神解析"));
        mChatMessages.add(new ChatMessage(null, "这。。。。\uD83D\uDE13\uD83D\uDE13\uD83D\uDE13"));
        mChatMessages.add(new ChatMessage("earth79", "十月一号之前就是这样，来回震荡，出不来趋势的。不过相信接下来的一个星期会有趋势出来的，破6800还是6300。应该很快出来结果，这个价位拖的时间太长了，多空都耗不起。"));
        mChatMessages.add(new ChatMessage("tangqi", "最近还是多看少动吧，还好昨天跑得快。简直是在坑爹。"));
        mChatMessages.add(new ChatMessage(null, "这行情看不懂了⚡️"));
        mChatMessages.add(new ChatMessage(null, "最近还是多看少动吧，还好昨天跑得快。简直是在坑爹。"));
        mChatMessages.add(new ChatMessage("thay65", "我更倾向于这是超跌反弹。\uD83D\uDE04\uD83D\uDE04"));
        mChatMessages.add(new ChatMessage(null, "最近还是多看少动吧，还好昨天跑得快。简直是在坑爹。"));
        mChatMessages.add(new ChatMessage(null, "最近还是多看少动吧，还好昨天跑得快。简直是在坑爹。"));
        mChatMessages.add(new ChatMessage(null, "最近还是多看少动吧，还好昨天跑得快。简直是在坑爹。"));
        mChatMessages.add(new ChatMessage("qwertyuiopasdfghjkklmnbvcxzqazwxxcervgtuninkhkhtt", "最近还是多看少动吧，还好昨天跑得快。简直是在坑爹。我挖掘份骄傲放假哦怕设计方法傲娇佛菩萨叫佛啊睡觉哦怕设计个电饭锅里放的购买的顾客；的 哦怕将打破放假哦事件发生 福建省军法审判叫佛菩萨叫发生的范德萨发顺丰是"));

        mChatOnScrollListener = new ChatOnScrollListener();
        mRvChatMessage.addOnScrollListener(mChatOnScrollListener);
        mRvChatMessage.addItemDecoration(new VisibleDividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mChatRecyclerViewAdapter = new ChatRecyclerViewAdapter(this, mChatMessages);
        mChatRecyclerViewAdapter.setOnItemClickListener(this);
        mRvChatMessage.setAdapter(mChatRecyclerViewAdapter);
        autoGeneratedChatMessage();
    }

    //测试新增数据
    private void autoGeneratedChatMessage(){
        Flowable.interval(5, 10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        mChatMessages.add(new ChatMessage(null, "最近还是多看少动吧，还好昨天跑得快。简直是在坑爹。"));
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
                });
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

    @OnEditorAction(R.id.chat_et_message_forced)
    public boolean onMessageEditorAction(TextView textView, int actionId, KeyEvent event){
        if(actionId == EditorInfo.IME_ACTION_SEND){
            mChatMessages.add(new ChatMessage("游客", textView.getText().toString()));
            mChatRecyclerViewAdapter.notifyDataSetChanged();
            mEtMessageForced.setText("");
            mTvMessageNormal.setText("");
        }
        return false;
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
        mChatMessages.add(new ChatMessage("游客", message));
        mChatRecyclerViewAdapter.notifyDataSetChanged();
        scrollToLastPosition();
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
     * 顶部弹出名字
     * @param anchor
     * @param username
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

    private void hidePopup(){
        if(mPopWindow != null && mPopWindow.isShowing()){
            mPopWindow.dismiss();
        }
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
