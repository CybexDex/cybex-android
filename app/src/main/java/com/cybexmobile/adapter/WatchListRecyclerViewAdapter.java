package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.fragment.WatchlistFragment.OnListFragmentInteractionListener;
import com.cybexmobile.R;
import com.cybexmobile.utils.AssetUtil;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class WatchListRecyclerViewAdapter extends RecyclerView.Adapter<WatchListRecyclerViewAdapter.ViewHolder> {

    private List<WatchlistData> mValues;
    private OnListFragmentInteractionListener mListener;
    private Context mContext;

    public WatchListRecyclerViewAdapter(List<WatchlistData> items, OnListFragmentInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    public void setWatchlistData(List<WatchlistData> watchlistData){
        mValues = watchlistData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_watch, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        WatchlistData watchlistData = mValues.get(position);
        NumberFormat formatter = new DecimalFormat("0.00");
        holder.mQuoteCurrency.setText(AssetUtil.parseSymbol(watchlistData.getQuoteSymbol()));
        holder.mBaseCurrency.setText(String.format("/%s", AssetUtil.parseSymbol(watchlistData.getBaseSymbol())));
        holder.mVolume.setText(watchlistData.getQuoteVol() == 0.f ? "-" : AssetUtil.formatAmountToKMB(watchlistData.getQuoteVol(), 2));
        holder.mCurrentPrice.setText(watchlistData.getCurrentPrice() == 0.f ? "-" : AssetUtil.formatNumberRounding(watchlistData.getCurrentPrice(), watchlistData.getBasePrecision()));

        double change = 0.f;
        if (watchlistData.getChange() != null) {
            try {
                change = Double.parseDouble(watchlistData.getChange());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        holder.mChangeRate.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                mContext.getResources().getDimension(change > 10 ? R.dimen.font_small : R.dimen.font_large));
        if (change > 0.f) {
            holder.mChangeRate.setText(String.format("+%s%%", String.valueOf(formatter.format(change * 100))));
            holder.mChangeRate.setBackgroundColor(mContext.getResources().getColor(R.color.increasing_color));
        } else if (change < 0.f) {
            holder.mChangeRate.setText(String.format("%s%%", String.valueOf(formatter.format(change * 100))));
            holder.mChangeRate.setBackgroundColor(mContext.getResources().getColor(R.color.decreasing_color));
        } else {
            holder.mChangeRate.setText(watchlistData.getCurrentPrice() == 0.f ? "-" : "0.00%");
            holder.mChangeRate.setBackgroundColor(mContext.getResources().getColor(R.color.no_change_color));
        }
        loadImage(watchlistData.getQuoteId(), holder.mSymboleView);
        /**
         * fix bug: CYM-250
         * 保留两位小数点
         */
        holder.mRmbPriceTextView.setText(watchlistData.getRmbPrice() * watchlistData.getCurrentPrice() == 0 ? "-" :
                String.format(Locale.US, "≈¥ %.2f", watchlistData.getRmbPrice() * watchlistData.getCurrentPrice()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(watchlistData, mValues, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mBaseCurrency;
        TextView mQuoteCurrency;
        TextView mCurrentPrice;
        TextView mVolume;
        TextView mChangeRate;
        TextView mRmbPriceTextView;
        ImageView mSymboleView;

        public ViewHolder(View view) {
            super(view);
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
        Picasso.get()
                .load("https://app.cybex.io/icons/" + quoteIdWithUnderLine + "_grey.png")
                .into(mCoinSymbol);
    }

    public void setItemToPosition(WatchlistData watchListData, int position) {
        mValues.set(position, watchListData);
        notifyItemChanged(position);
    }
}
