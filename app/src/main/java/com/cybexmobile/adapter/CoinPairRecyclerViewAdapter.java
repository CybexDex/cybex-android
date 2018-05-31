package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cybexmobile.fragment.data.WatchListData;
import com.cybexmobile.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class CoinPairRecyclerViewAdapter extends RecyclerView.Adapter<CoinPairRecyclerViewAdapter.ViewHolder> {
    Context mContext;
    private final List<WatchListData> mData;
    private int rowIndex;
    private updateDataListener mListener;

    public interface updateDataListener {
        void onClickHorizontalItem(WatchListData watchListData);
    }
    public CoinPairRecyclerViewAdapter(Context context, List<WatchListData> data, int id, updateDataListener listener) {
        mContext = context;
        mData = data;
        rowIndex = id;
        mListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        WatchListData mItem;
        TextView mChangeView;
        TextView mBaseCoinView;
        TextView mQuoteCoinView;
        RelativeLayout mCoinPairLayout;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            mChangeView = (TextView) view.findViewById(R.id.coin_pair_card_view_percentage);
            mBaseCoinView = (TextView) view.findViewById(R.id.base_currency_market_horizontal_view);
            mQuoteCoinView = (TextView) view.findViewById(R.id.quote_currency_market_horizontal_view);
            mCoinPairLayout = (RelativeLayout) view.findViewById(R.id.coin_pair_card_layout);

        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        NumberFormat formatter = new DecimalFormat("0.00");

        holder.mItem = mData.get(position);

        holder.mBaseCoinView.setText(holder.mItem.getBase());
        holder.mQuoteCoinView.setText(String.format("/%s", holder.mItem.getQuote()));

        double change = 0.f;

        if (holder.mItem.getChange() != null) {
            try {
                change = Double.parseDouble(holder.mItem.getChange());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (change > 0) {
            holder.mChangeView.setText(String.format("+%s%%", String.valueOf(formatter.format(change * 100))));
        } else {
            holder.mChangeView.setText(String.format("%s%%", String.valueOf(formatter.format(change * 100))));
        }


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowIndex = position;
                notifyDataSetChanged();
                mListener.onClickHorizontalItem(holder.mItem);
            }
        });

        if(rowIndex == position) {
            holder.mCoinPairLayout.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.market_coin_pair));
        } else {
            holder.mCoinPairLayout.setBackgroundColor(mContext.getResources().getColor(R.color.horizontalItemBackground));
        }

    }
}
