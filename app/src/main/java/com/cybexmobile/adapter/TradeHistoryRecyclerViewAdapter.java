package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybex.provider.market.WatchlistData;
import com.cybexmobile.fragment.dummy.DummyContent.DummyItem;
import com.cybexmobile.R;
import com.cybex.provider.graphene.chain.MarketTrade;
import com.cybex.basemodule.utils.AssetUtil;

import java.math.RoundingMode;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * TODO: Replace the implementation with code for your data type.
 */
public class TradeHistoryRecyclerViewAdapter extends RecyclerView.Adapter<TradeHistoryRecyclerViewAdapter.ViewHolder> {

    private final List<MarketTrade> mValues;
    private final Context mContext;
    private WatchlistData mWatchlistData;

    public TradeHistoryRecyclerViewAdapter(List<MarketTrade> items, WatchlistData watchlistData, Context context) {
        mValues = items;
        mContext = context;
        mWatchlistData = watchlistData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trade_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        /**
         * fix bug:CYM-379
         * 精度不对
         */
        //交易历史price卖单ronudup，买单rounddown
        holder.mPriceView.setText(AssetUtil.formatNumberRounding(mValues.get(position).price, mWatchlistData.getPricePrecision(),
                mValues.get(position).showRed.equals("showRed") ? RoundingMode.UP : RoundingMode.DOWN));
        holder.mBaseView.setText(AssetUtil.formatNumberRounding(mValues.get(position).baseAmount, mWatchlistData.getTotalPrecision()));
        holder.mQuoteView.setText(AssetUtil.formatAmountToKMB(mValues.get(position).quoteAmount, mWatchlistData.getAmountPrecision()));
        holder.mDateView.setText(mValues.get(position).date);
        if(mValues.get(position).showRed.equals("showRed")) {
            holder.mPriceView.setTextColor(mContext.getResources().getColor(R.color.decreasing_color));
        } else {
            holder.mPriceView.setTextColor(mContext.getResources().getColor(R.color.increasing_color));
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MarketTrade mItem;
        final View mView;
        final TextView mPriceView;
        final TextView mQuoteView;
        TextView mBaseView;
        TextView mDateView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mPriceView = (TextView) view.findViewById(R.id.market_page_trade_history_price_value);
            mQuoteView = (TextView) view.findViewById(R.id.market_page_trade_history_quote_volume);
            mBaseView = (TextView) view.findViewById(R.id.market_page_trade_history_base_volume);
            mDateView = (TextView) view.findViewById(R.id.market_page_trade_history_date_value);
        }
    }
}
