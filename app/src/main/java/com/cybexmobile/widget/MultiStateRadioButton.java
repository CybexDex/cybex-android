package com.cybexmobile.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybexmobile.R;

public class MultiStateRadioButton extends FrameLayout implements MultiStateCheckable {

    public static final int STATE_UP = 1;
    public static final int STATE_DOWN = 2;

    /**
     * down  false  up true
     */
    private boolean mBroadcasting;
    private int mState;
    private TextView mTv_checkable_text;
    private ImageView mIvCheckable;
    private OnCheckedChangeListener mOnCheckedChangeWidgetListener;
    private int mImageRes_default, mImageRes_up, mImageRes_down;

    private MultiStateRadioButton(Context context) {
        super(context);
    }

    public MultiStateRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public MultiStateRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }


    private void initView(AttributeSet attrs) {

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SoftRadioButton);
        String text = ta.getString(R.styleable.SoftRadioButton_text);

        mImageRes_up = ta.getResourceId(R.styleable.SoftRadioButton_imageUp, 0);
        mImageRes_down = ta.getResourceId(R.styleable.SoftRadioButton_imageDown, 0);
        mImageRes_default = ta.getResourceId(R.styleable.SoftRadioButton_imageDefault, 0);
        boolean isStateUp = ta.getBoolean(R.styleable.SoftRadioButton_isStateUp, false);
        boolean isStateDown = ta.getBoolean(R.styleable.SoftRadioButton_isStateDown, false);

        ta.recycle();

        LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflate.inflate(R.layout.mulit_state_radio_view, this, true);
        mTv_checkable_text = (TextView) findViewById(R.id.tv_checkable_text);
        mIvCheckable = (ImageView) findViewById(R.id.iv_checkable);
        mTv_checkable_text.setText(text);
        isChecked = isStateUp || isStateDown;
        if(isStateUp){
            mState = STATE_UP;
        }
        if(isStateDown){
            mState = STATE_DOWN;
        }
        refreshView();
        setOnClickListener(null);
    }

    private boolean isChecked;

    public void setText(CharSequence text) {
        mTv_checkable_text.setText(text);
    }

    public CharSequence getText() {
        return mTv_checkable_text.getText();
    }

    private OnClickListener ml;

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        ml = l;
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ml != null) {
                    ml.onClick(v);
                }
                if (isChecked()) {
                    mState = mState == STATE_UP ? STATE_DOWN : STATE_UP;
                } else {
                    mState = STATE_DOWN;
                }
                setChecked(true, mState);
                refreshView();
            }
        });
    }

    private MultiStateRadioGroup softGroup;

    public MultiStateRadioGroup getGroup() {
        if (softGroup == null) {
            if (getParent() != null && getParent() instanceof MultiStateRadioGroup) {
                softGroup = (MultiStateRadioGroup) getParent();
            }
        }
        return softGroup;
    }

    @Override
    public void setChecked(boolean checked, int state) {
        if (checked) {
            makeCallBack(state);
        }
        if (isChecked != checked) {//刷新其他的
            isChecked = checked;
            refreshView();
            if (mBroadcasting) {
                return;
            }
            mBroadcasting = true;
            if (mOnCheckedChangeWidgetListener != null) {
                mOnCheckedChangeWidgetListener.onCheckedChanged(this, isChecked);
            }
            mBroadcasting = false;
        } else {
            if (mState != state) {
                refreshView();
                if (mBroadcasting) {
                    return;
                }
                mBroadcasting = true;
                if (mOnCheckedChangeWidgetListener != null) {
                    mOnCheckedChangeWidgetListener.onCheckedChanged(this, isChecked);
                }
                mBroadcasting = false;
            }
        }
    }

    private void makeCallBack(int state) {
        MultiStateRadioGroup group = getGroup();
        if (group != null && group.getOnCheckedChangeListener() != null) {
            group.getOnCheckedChangeListener().onCheckedChanged(group, getId(), state);
        }
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    public int getState() {
        return mState;
    }

    @Override
    public void toggle() {
        isChecked = !isChecked;
        refreshView();
    }


    private void refreshView() {
        if (isChecked()) {
            mIvCheckable.setImageResource(mState == STATE_UP ? mImageRes_up : mImageRes_down);
        } else {
            mIvCheckable.setImageResource(mImageRes_default);
        }

    }

    public void setOnCheckedChangeWidgetListener(OnCheckedChangeListener onCheckedChangeWidgetListener) {
        mOnCheckedChangeWidgetListener = onCheckedChangeWidgetListener;
    }


    public interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked  The new checked state of buttonView.
         */
        void onCheckedChanged(MultiStateRadioButton buttonView, boolean isChecked);
    }

}
