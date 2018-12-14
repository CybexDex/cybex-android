package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.market.WatchlistData;
import com.cybexmobile.R;
import com.cybexmobile.fragment.WatchlistFragment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TopGainerRecyclerViewAdapter extends RecyclerView.Adapter<TopGainerRecyclerViewAdapter.ViewHolder> {

    private List<WatchlistData> mValues;
    private Context mContext;
    private WatchlistFragment.OnListFragmentInteractionListener mListener;
    private NumberFormat formatter = new DecimalFormat("0.00");

    public TopGainerRecyclerViewAdapter(Context context, List<WatchlistData> items,
                                        WatchlistFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mContext = context;
        mListener = listener;
    }

    public void setWatchlistData(List<WatchlistData> watchlistData) {
        mValues = watchlistData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_gainer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (position > 2) {
            holder.mTvSort.setBackground(mContext.getResources().getDrawable(R.drawable.bg_no_change));
        } else {
            holder.mTvSort.setBackground(mContext.getResources().getDrawable(R.drawable.bg_increasing));
        }
        holder.mTvSort.setText(String.valueOf(position + 1));
        if (mValues == null || mValues.size() == 0 || mValues.size() - 1 < position) {
            return;
        }
        final WatchlistData watchlistData = mValues.get(position);
        if (watchlistData == null || watchlistData.getChange() < 0.f) {
            return;
        }
        holder.mTvQuoteSymbol.setText(AssetUtil.parseSymbol(watchlistData.getQuoteSymbol()));
        holder.mTvBaseSymbol.setText(String.format("/%s", AssetUtil.parseSymbol(watchlistData.getBaseSymbol())));
        holder.mTvVolume.setText(watchlistData.getBaseVol() == 0.f ? "-" : AssetUtil.formatAmountToKMB(watchlistData.getBaseVol(), watchlistData.getDayAmountPrecision()));
        holder.mTvCurrentPrice.setText(watchlistData.getCurrentPrice() == 0.f ? "-" : AssetUtil.formatNumberRounding(watchlistData.getCurrentPrice(), watchlistData.getPricePrecision()));

        double change = watchlistData.getChange();
        holder.mTvChangeRate.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                mContext.getResources().getDimension(change >= 100 ? R.dimen.font_small : R.dimen.font_large));
        if (change > 0.f) {
            holder.mTvChangeRate.setText(String.format("+%s%%", String.valueOf(formatter.format(change))));
            holder.mTvChangeRate.setBackground(mContext.getResources().getDrawable(R.drawable.bg_increasing));
        } else if (change < 0.f) {
            holder.mTvChangeRate.setText(String.format("%s%%", String.valueOf(formatter.format(change))));
            holder.mTvChangeRate.setBackground(mContext.getResources().getDrawable(R.drawable.bg_decreasing));
        } else {
            holder.mTvChangeRate.setText(watchlistData.getCurrentPrice() == 0.f ? "-" : "0.00%");
            holder.mTvChangeRate.setBackground(mContext.getResources().getDrawable(R.drawable.bg_no_change));
        }
        /**
         * fix bug: CYM-250
         * 保留四位小数点
         */
        holder.mTvRmbPrice.setText(watchlistData.getRmbPrice() * watchlistData.getCurrentPrice() == 0 ? "-" :
                String.format(Locale.US, "≈¥ %s", AssetUtil.formatNumberRounding(
                        watchlistData.getRmbPrice() * watchlistData.getCurrentPrice(), watchlistData.getRmbPrecision())));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onListFragmentInteraction(watchlistData);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return 6;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_top_gainer_tv_sort)
        TextView mTvSort;
        @BindView(R.id.item_top_gainer_tv_base)
        TextView mTvBaseSymbol;
        @BindView(R.id.item_top_gainer_tv_quote)
        TextView mTvQuoteSymbol;
        @BindView(R.id.item_top_gainer_tv_current_price)
        TextView mTvCurrentPrice;
        @BindView(R.id.item_top_gainer_tv_volume)
        TextView mTvVolume;
        @BindView(R.id.item_top_gainer_tv_change_rate)
        TextView mTvChangeRate;
        @BindView(R.id.item_top_gainer_tv_rmb_price)
        TextView mTvRmbPrice;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
