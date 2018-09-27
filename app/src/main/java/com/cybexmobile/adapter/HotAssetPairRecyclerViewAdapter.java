package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.http.entity.SubLink;
import com.cybex.provider.market.WatchlistData;
import com.cybexmobile.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HotAssetPairRecyclerViewAdapter extends RecyclerView.Adapter<HotAssetPairRecyclerViewAdapter.ViewHolder> {

    private Context mContext;
    private List<WatchlistData> mWatchlistData;
    private OnItemClickListener mListener;
    private NumberFormat formatter = new DecimalFormat("0.00");

    public HotAssetPairRecyclerViewAdapter(Context context, List<WatchlistData> watchlistData) {
        mContext = context;
        mWatchlistData = watchlistData;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
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
        WatchlistData watchlistData = mWatchlistData.get(position);
        if(watchlistData == null){
            return;
        }
        holder.mTvAssetSymbol.setText(String.format("%s/%s", AssetUtil.parseSymbol(watchlistData.getQuoteSymbol()), AssetUtil.parseSymbol(watchlistData.getBaseSymbol())));
        holder.mTvCurrPrice.setText(watchlistData.getCurrentPrice() == 0.f ? "-" : AssetUtil.formatNumberRounding(watchlistData.getCurrentPrice(), watchlistData.getBasePrecision()));
        double change = 0.f;
        if (watchlistData.getChange() != null) {
            try {
                change = Double.parseDouble(watchlistData.getChange());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        holder.mTvChangeRate.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                mContext.getResources().getDimension(change > 10 ? R.dimen.font_small : R.dimen.font_large));
        if (change > 0.f) {
            holder.mTvChangeRate.setText(String.format("+%s%%", String.valueOf(formatter.format(change * 100))));
            holder.mTvChangeRate.setTextColor(mContext.getResources().getColor(R.color.increasing_color));
            holder.mTvCurrPrice.setTextColor(mContext.getResources().getColor(R.color.increasing_color));
        } else if (change < 0.f) {
            holder.mTvChangeRate.setText(String.format("%s%%", String.valueOf(formatter.format(change * 100))));
            holder.mTvChangeRate.setTextColor(mContext.getResources().getColor(R.color.decreasing_color));
            holder.mTvCurrPrice.setTextColor(mContext.getResources().getColor(R.color.decreasing_color));
        } else {
            holder.mTvChangeRate.setText(watchlistData.getCurrentPrice() == 0.f ? "-" : "0.00%");
            holder.mTvChangeRate.setTextColor(mContext.getResources().getColor(R.color.no_change_color));
            holder.mTvCurrPrice.setTextColor(mContext.getResources().getColor(R.color.no_change_color));
        }
        holder.mTvRmbPrice.setText(watchlistData.getRmbPrice() * watchlistData.getCurrentPrice() == 0 ? "-" :
                String.format(Locale.US, "≈¥ %.2f", watchlistData.getRmbPrice() * watchlistData.getCurrentPrice()));
    }

    @Override
    public int getItemCount() {
        return mWatchlistData == null ? 0 : mWatchlistData.size();
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

    public interface OnItemClickListener{
        void onItemClick(WatchlistData watchlistData);
    }
}
