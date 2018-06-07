package com.cybexmobile.fragment;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybexmobile.fragment.OrderHistoryListFragment.OnListFragmentInteractionListener;
import com.cybexmobile.fragment.dummy.DummyContent.DummyItem;
import com.cybexmobile.R;
import com.cybexmobile.utils.MyUtils;
import com.cybexmobile.market.Order;
import com.cybexmobile.market.OrderBook;

import java.text.NumberFormat;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class OrderHistoryItemRecyclerViewAdapter extends RecyclerView.Adapter<OrderHistoryItemRecyclerViewAdapter.ViewHolder> {

    private OrderBook mValues;
    private String mQuoteName;
    private final OnListFragmentInteractionListener mListener;
    private Context mContext;


    public OrderHistoryItemRecyclerViewAdapter(String quoteName, OrderBook orderBook, OnListFragmentInteractionListener listener, Context context) {
        mValues = orderBook;
        mQuoteName = quoteName;
        mListener = listener;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        NumberFormat formatter = MyUtils.getSuitableDecimalFormat(mQuoteName);
        if (position < mValues.bids.size()) {
            holder.mBuyPrice.setText(String.valueOf(formatter.format(mValues.bids.get(position).price)));
            holder.mVolume.setText(String.valueOf(MyUtils.getNumberKMGExpressionFormat(mValues.bids.get(position).quote)));
        }
        if (position < mValues.asks.size()) {
            holder.mSellPrice.setText(String.valueOf(formatter.format(mValues.asks.get(position).price)));
            holder.mSellVolume.setText(String.valueOf(MyUtils.getNumberKMGExpressionFormat(mValues.asks.get(position).quote)));
        }

        float percentageBids = 0f;
        if (position < mValues.bids.size()) {
            percentageBids = (float) getPercentage(mValues.bids, position);
        }
        float percentageAsks = 0f;
        if (position < mValues.asks.size()) {
            percentageAsks = (float) getPercentage(mValues.asks, position);
        }
        LinearLayout.LayoutParams barpar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, percentageBids);
        LinearLayout.LayoutParams barpar2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1 - percentageBids);
        LinearLayout.LayoutParams barParAsks = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, percentageAsks);
        LinearLayout.LayoutParams barparAsks2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1 - percentageAsks);
        holder.mProgressBar.setLayoutParams(barpar);
        holder.mProgressBar2.setLayoutParams(barpar2);
        holder.mProgressBar2.setBackgroundColor(Color.TRANSPARENT);
        holder.mProgressBar.setBackgroundColor(mContext.getResources().getColor(R.color.fade_background_green));
        holder.mProgressBarRed.setLayoutParams(barparAsks2);
        holder.mProgressBarRedCompliment.setLayoutParams(barParAsks);
        holder.mProgressBarRed.setBackgroundColor(Color.TRANSPARENT);
        holder.mProgressBarRedCompliment.setBackgroundColor(mContext.getResources().getColor(R.color.fade_background_red));
        holder.mTextLinearLayout.bringToFront();
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mValues != null) {
            if (mValues.asks.size() > 20 || mValues.bids.size() > 20) {
                return 20;
            } else {
                if (mValues.asks.size() >= mValues.bids.size()) {
                    return mValues.asks.size();
                } else {
                    return mValues.bids.size();
                }
            }
        } else {
            return 0;
        }

    }

    public void setmValues(OrderBook orderBook) {
        this.mValues = orderBook;
        notifyDataSetChanged();
    }

    private double getPercentage(List<Order> orderList, int position) {
        double divider = 0;
        for (int i = 0; i <= position; i++) {
            divider += orderList.get(i).base;
        }
        return divider / getSum(orderList);
    }

    private double getSum(List<Order> orderList) {
        double sum = 0;
        int length = 0;
        if (orderList != null && orderList.size() != 0) {
            if (orderList.size() > 20) {
                length = 20;
            } else {
                length = orderList.size();
            }
            for (int i = 0; i < length; i++) {
                sum += orderList.get(i).base;
            }
        }
        return sum;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mBuyPrice;
        final TextView mVolume;
        TextView mSellPrice;
        TextView mSellVolume;
        TextView mProgressBar;
        TextView mProgressBar2;
        TextView mProgressBarRed;
        TextView mProgressBarRedCompliment;
        LinearLayout mTextLinearLayout;
        DummyItem mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mBuyPrice = (TextView) view.findViewById(R.id.buy_price);
            mVolume = (TextView) view.findViewById(R.id.order_volume);
            mProgressBar = (TextView) view.findViewById(R.id.catStatsRowBar);
            mProgressBar2 = (TextView) view.findViewById(R.id.catStatsRowBar2);
            mProgressBarRed = (TextView) view.findViewById(R.id.row_bar_red);
            mProgressBarRedCompliment = (TextView) view.findViewById(R.id.row_bar_red_compliment);
            mTextLinearLayout = (LinearLayout) view.findViewById(R.id.text_linear_layout);
            mSellPrice = (TextView) view.findViewById(R.id.sell_price);
            mSellVolume = (TextView) view.findViewById(R.id.sell_order_volume);
        }
    }
}
