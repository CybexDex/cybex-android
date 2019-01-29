package com.cybexmobile.widget;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cybexmobile.fragment.markets.MarketOrderHistoryFragment;
import com.cybexmobile.fragment.markets.MarketTradeHistoryFragment;

public class ViewPagerForScrollView extends ViewPager {

    private View mCurrentView;
    private Fragment mFragment;
    private boolean enableScroll = false;

//    private int mLastXIntercept;
//    private int mLastYIntercept;

    public ViewPagerForScrollView(Context context) {
        super(context);
    }

    public ViewPagerForScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEnableScroll(boolean enableScroll) {
        this.enableScroll = enableScroll;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mCurrentView == null || mFragment instanceof MarketOrderHistoryFragment || mFragment instanceof MarketTradeHistoryFragment) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int height = 0;
        mCurrentView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int h = mCurrentView.getMeasuredHeight();
        if (h > height) height = h;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void measureCurrentView(Fragment fragment) {
        mFragment = fragment;
        mCurrentView = fragment.getView();
        requestLayout();
    }

    public int measureFragment(View view) {
        if (view == null)
            return 0;

        view.measure(0, 0);
        return view.getMeasuredHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!enableScroll) {
            return false;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (!enableScroll) {
            return false;
        }

        return super.onInterceptTouchEvent(ev);
//        boolean intercepted = false;
//        int x = (int) ev.getX();
//        int y = (int) ev.getY();
//        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                intercepted = false;
//                super.onInterceptTouchEvent(ev);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                int deltaX = x - mLastXIntercept;
//                int deltaY = y - mLastYIntercept;
//                if (Math.abs(deltaX) > Math.abs(deltaY)) {
//                    intercepted = true;
//                } else {
//                    intercepted = false;
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                intercepted = false;
//                break;
//            default:
//                break;
//        }
//
//        mLastYIntercept = y;
//        mLastXIntercept = x;
//        return intercepted;
    }
}
