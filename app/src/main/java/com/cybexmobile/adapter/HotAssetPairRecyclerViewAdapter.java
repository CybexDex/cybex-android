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
import com.cybexmobile.shake.AntiShake;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HotAssetPairRecyclerViewAdapter extends RecyclerView.Adapter<HotAssetPairRecyclerViewAdapter.ViewHolder> {

    private Context mContext;
    private List<WatchlistData> mWatchlistData;
    private NumberFormat formatter = new DecimalFormat("0.00");
    private WatchlistFragment.OnListFragmentInteractionListener mListener;

    public HotAssetPairRecyclerViewAdapter(Context context, List<WatchlistData> watchlistData,
                                           WatchlistFragment.OnListFragmentInteractionListener listener) {
        mContext = context;
        mWatchlistData = watchlistData;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hot_pair, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mWatchlistData == null || mWatchlistData.size() == 0) {
            return;
        }
        WatchlistData watchlistData = mWatchlistData.get(position);
        if (watchlistData == null) {
            return;
        }
        holder.mTvAssetSymbol.setText(String.format("%s/%s", AssetUtil.parseSymbol(watchlistData.getQuoteSymbol()), AssetUtil.parseSymbol(watchlistData.getBaseSymbol())));
        holder.mTvCurrPrice.setText(watchlistData.getCurrentPrice() == 0.f ? "-" : AssetUtil.formatNumberRounding(watchlistData.getCurrentPrice(), watchlistData.getPricePrecision()));
        double change = watchlistData.getChange();
        holder.mTvChangeRate.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                mContext.getResources().getDimension(change > 100 ? R.dimen.font_small : R.dimen.font_large));
        if (change > 0.f) {
            holder.mTvChangeRate.setText(String.format("+%s%%", String.valueOf(formatter.format(change))));
            holder.mTvChangeRate.setTextColor(mContext.getResources().getColor(R.color.increasing_color));
            holder.mTvCurrPrice.setTextColor(mContext.getResources().getColor(R.color.increasing_color));
        } else if (change < 0.f) {
            holder.mTvChangeRate.setText(String.format("%s%%", String.valueOf(formatter.format(change))));
            holder.mTvChangeRate.setTextColor(mContext.getResources().getColor(R.color.decreasing_color));
            holder.mTvCurrPrice.setTextColor(mContext.getResources().getColor(R.color.decreasing_color));
        } else {
            holder.mTvChangeRate.setText(watchlistData.getCurrentPrice() == 0.f ? "-" : "0.00%");
            holder.mTvChangeRate.setTextColor(mContext.getResources().getColor(R.color.no_change_color));
            holder.mTvCurrPrice.setTextColor(mContext.getResources().getColor(R.color.no_change_color));
        }

        holder.mTvRmbPrice.setText(watchlistData.getRmbPrice() * watchlistData.getCurrentPrice() == 0 ? "-" :
                String.format(Locale.US, "≈¥ %s", AssetUtil.formatNumberRounding(
                        watchlistData.getRmbPrice() * watchlistData.getCurrentPrice(), watchlistData.getRmbPrecision())));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AntiShake.check(v.getId())) { return; }
                if (mListener != null) {
                    mListener.onListFragmentInteraction(watchlistData);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWatchlistData.size() < 3 ? mWatchlistData.size() : 3;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_hot_pair_tv_asset_symbol)
        TextView mTvAssetSymbol;
        @BindView(R.id.item_hot_pair_tv_price)
        TextView mTvCurrPrice;
        @BindView(R.id.item_hot_pair_tv_rmb_price)
        TextView mTvRmbPrice;
        @BindView(R.id.item_hot_pair_tv_change)
        TextView mTvChangeRate;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
