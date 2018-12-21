package com.cybexmobile.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybex.provider.market.WatchlistData;
import com.cybexmobile.fragment.dummy.DummyContent.DummyItem;
import com.cybexmobile.R;
import com.cybex.basemodule.utils.AssetUtil;

import java.math.RoundingMode;
import java.util.List;

public class OrderHistoryRecyclerViewAdapter extends RecyclerView.Adapter<OrderHistoryRecyclerViewAdapter.ViewHolder> {

    private WatchlistData mWatchlistData;
    private Context mContext;
    private List<List<String>> mSellOrders;
    private List<List<String>> mBuyOrders;

    public OrderHistoryRecyclerViewAdapter(WatchlistData watchlistData, Context context) {
        mWatchlistData = watchlistData;
        mContext = context;
    }

    public OrderHistoryRecyclerViewAdapter(WatchlistData watchlistData, List<List<String>> sellOrders, List<List<String>> buyOrders, Context context) {
        mWatchlistData = watchlistData;
        mSellOrders = sellOrders;
        mBuyOrders = buyOrders;
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
        if (position < mBuyOrders.size()) {
            holder.mBuyPrice.setText(AssetUtil.formatNumberRounding(Double.parseDouble(mBuyOrders.get(position).get(0)), mWatchlistData.getPricePrecision()));
            holder.mVolume.setText(AssetUtil.formatAmountToKMB(Double.parseDouble(mBuyOrders.get(position).get(1)), mWatchlistData.getAmountPrecision()));
        }
        if (position < mSellOrders.size()) {
            holder.mSellPrice.setText(AssetUtil.formatNumberRounding(Double.parseDouble(mSellOrders.get(position).get(0)), mWatchlistData.getPricePrecision(), RoundingMode.UP));
            holder.mSellVolume.setText(AssetUtil.formatAmountToKMB(Double.parseDouble(mSellOrders.get(position).get(1)), mWatchlistData.getAmountPrecision()));
        }
        float percentageBids = 0f;
        if (position < mBuyOrders.size()) {
            percentageBids = (float) getPercentage(mBuyOrders, position);
        }
        float percentageAsks = 0f;
        if (position < mSellOrders.size()) {
            percentageAsks = (float) getPercentage(mSellOrders, position);
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
    }

    @Override
    public int getItemCount() {
        if (mSellOrders == null && mBuyOrders == null) {
            return 0;
        }
        if(mSellOrders == null){
            return mBuyOrders.size();
        }
        if(mBuyOrders == null) {
            return mSellOrders.size();
        }
        return mSellOrders.size() >= mBuyOrders.size() ? mSellOrders.size() : mBuyOrders.size();
    }

    public void setValues(List<List<String>> sellOrders, List<List<String>> buyOrders) {
        mSellOrders = sellOrders;
        mBuyOrders = buyOrders;
        notifyDataSetChanged();
    }

    private double getPercentage(List<List<String>> orders, int position) {
        double divider = 0;
        for (int i = 0; i <= position; i++) {
            divider += Double.parseDouble(orders.get(i).get(1));
        }
        return divider / getSum(orders);
    }

    private double getSum(List<List<String>> orders) {
        double sum = 0;
        if (orders != null && orders.size() != 0) {
            for (int i = 0; i < orders.size(); i++) {
                sum += Double.parseDouble(orders.get(i).get(1));
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
            mBuyPrice = view.findViewById(R.id.buy_price);
            mVolume = view.findViewById(R.id.order_volume);
            mProgressBar = view.findViewById(R.id.catStatsRowBar);
            mProgressBar2 = view.findViewById(R.id.catStatsRowBar2);
            mProgressBarRed = view.findViewById(R.id.row_bar_red);
            mProgressBarRedCompliment = view.findViewById(R.id.row_bar_red_compliment);
            mTextLinearLayout = view.findViewById(R.id.text_linear_layout);
            mSellPrice = view.findViewById(R.id.sell_price);
            mSellVolume = view.findViewById(R.id.sell_order_volume);
        }
    }
}
