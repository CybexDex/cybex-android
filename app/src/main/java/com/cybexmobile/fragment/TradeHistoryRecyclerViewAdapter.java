package com.cybexmobile.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybexmobile.fragment.MarketTradeHistoryFragment.OnListFragmentInteractionListener;
import com.cybexmobile.fragment.dummy.DummyContent.DummyItem;
import com.cybexmobile.R;
import com.cybexmobile.market.MarketTrade;
import com.cybexmobile.utils.MyUtils;

import java.text.NumberFormat;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class TradeHistoryRecyclerViewAdapter extends RecyclerView.Adapter<TradeHistoryRecyclerViewAdapter.ViewHolder> {

    private final List<MarketTrade> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final Context mContext;

    TradeHistoryRecyclerViewAdapter(List<MarketTrade> items, OnListFragmentInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
        mContext = context;
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
        NumberFormat formatter = MyUtils.getSuitableDecimalFormat(holder.mItem != null ? holder.mItem.quote : "");
        holder.mPriceView.setText(String.valueOf(formatter.format(mValues.get(position).price)));
        holder.mBaseView.setText(String.valueOf(MyUtils.getNumberKMGExpressionFormat(mValues.get(position).baseAmount)));
        holder.mQuoteView.setText(String.valueOf(MyUtils.getNumberKMGExpressionFormat(mValues.get(position).quoteAmount)));
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
