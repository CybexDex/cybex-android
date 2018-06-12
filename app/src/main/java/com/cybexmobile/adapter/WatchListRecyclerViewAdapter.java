package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.fragment.WatchlistFragment.OnListFragmentInteractionListener;
import com.cybexmobile.R;
import com.cybexmobile.utils.MyUtils;
import com.squareup.picasso.Picasso;

import org.decimal4j.util.DoubleRounder;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class WatchListRecyclerViewAdapter extends RecyclerView.Adapter<WatchListRecyclerViewAdapter.ViewHolder> {

    private final List<WatchlistData> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Context mContext;

    public WatchListRecyclerViewAdapter(List<WatchlistData> items, OnListFragmentInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                 .inflate(R.layout.item_watch, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        String precisinFormatter = MyUtils.getPrecisedFormatter(holder.mItem.getBasePrecision());
        NumberFormat formatter = new DecimalFormat("0.00");
        holder.mItem = mValues.get(position);
        if (mValues.get(position).getQuoteSymbol().contains("JADE")) {
            holder.mQuoteCurrency.setText(mValues.get(position).getQuoteSymbol().substring(5, mValues.get(position).getQuoteSymbol().length()));
        } else {
            holder.mQuoteCurrency.setText(mValues.get(position).getQuoteSymbol());
        }
        if (mValues.get(position).getBaseSymbol().contains("JADE")) {
            holder.mBaseCurrency.setText(String.format("/%s", mValues.get(position).getBaseSymbol().substring(5, mValues.get(position).getBaseSymbol().length())));
        } else {
            holder.mBaseCurrency.setText(String.format("/%s", mValues.get(position).getBaseSymbol()));
        }
        holder.mVolume.setText(holder.mItem.getQuoteVol() == 0.f ? "-" : MyUtils.getNumberKMGExpressionFormat(mValues.get(position).getQuoteVol()));
        holder.mCurrentPrice.setText(holder.mItem.getCurrentPrice() == 0.f ? "-" : String.format(precisinFormatter, holder.mItem.getCurrentPrice()));

        double change = 0.f;
        if(mValues.get(position).getChange()!= null) {
            try {
                change = Double.parseDouble(mValues.get(position).getChange());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (change > 10) {
            holder.mChangeRate.setTextSize(12);
        } else {
            holder.mChangeRate.setTextSize(16);
        }
        if( change > 0.f) {
            holder.mChangeRate.setText(String.format("+%s%%",String.valueOf(formatter.format(change * 100))));
            holder.mChangeRate.setBackgroundColor(mContext.getResources().getColor(R.color.increasing_color));

        } else if (change < 0.f) {
            holder.mChangeRate.setText(String.format("%s%%",String.valueOf(formatter.format(change * 100))));
            holder.mChangeRate.setBackgroundColor(mContext.getResources().getColor(R.color.decreasing_color));

        } else {
            holder.mChangeRate.setText(holder.mItem.getCurrentPrice() == 0.f ? "-" : "0.00%");
            holder.mChangeRate.setBackgroundColor(mContext.getResources().getColor(R.color.no_change_color));
        }
        loadImage(mValues.get(position).getQuoteId(), holder.mSymboleView);
        /**
         * fix bug: CYM-250
         * 保留两位小数点
         */
        holder.mRmbPriceTextView.setText(holder.mItem.getRmbPrice() * holder.mItem.getCurrentPrice() == 0 ? "-" : formatter.format(mValues.get(position).getRmbPrice() * mValues.get(position).getCurrentPrice()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem, mValues, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        public WatchlistData mItem;
        TextView mBaseCurrency;
        TextView mQuoteCurrency;
        TextView mCurrentPrice;
        TextView mVolume;
        TextView mChangeRate;
        TextView mRmbPriceTextView;
        ImageView mSymboleView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mBaseCurrency = (TextView) view.findViewById(R.id.base_currency_watchlist);
            mQuoteCurrency = (TextView) view.findViewById(R.id.quote_currency_watchlist);
            mCurrentPrice = (TextView) view.findViewById(R.id.current_price_watchlist);
            mVolume = (TextView) view.findViewById(R.id.volume);
            mChangeRate = (TextView) view.findViewById(R.id.change_rate_watchlist);
            mSymboleView = view.findViewById(R.id.watch_list_coin_symbol);
            mRmbPriceTextView = view.findViewById(R.id.watch_list_rmb_price);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + "'";
        }
    }

    private void loadImage(String quoteId, ImageView mCoinSymbol) {
        String quoteIdWithUnderLine = quoteId.replaceAll("\\.", "_");
        Picasso.get().load("https://app.cybex.io/icons/" + quoteIdWithUnderLine +"_grey.png").into(mCoinSymbol);
    }

    public void setItemToPosition(WatchlistData watchListData, int position) {
        mValues.set(position, watchListData);
        notifyItemChanged(position);
    }
}
