package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.utils.AssetUtil;
import com.cybexmobile.utils.MyUtils;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WatchlistSelectRecyclerViewAdapter extends RecyclerView.Adapter<WatchlistSelectRecyclerViewAdapter.ViewHolder> {

    private List<WatchlistData> mWatchlists;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public WatchlistSelectRecyclerViewAdapter(Context context, List<WatchlistData> watchlists) {
        mWatchlists = watchlists;
        mContext = context;
    }

    public void setOrderHistoryItems(List<WatchlistData> watchlists){
        mWatchlists = watchlists;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mOnItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_watchlist_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WatchlistData watchlist = mWatchlists.get(position);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(mOnItemClickListener != null && hasFocus){
                    mOnItemClickListener.onItemClick(watchlist);
                }
            }
        });
        holder.mTvQuoteSymbol.setText(AssetUtil.parseSymbol(watchlist.getQuoteSymbol()));
        holder.mTvBaseSymbol.setText(AssetUtil.parseSymbol(watchlist.getBaseSymbol()));
        double change = watchlist.getChange() == null ? 0.0 : Double.parseDouble(watchlist.getChange());
        if(change > 0.f){
            holder.mTvChange.setTextColor(mContext.getResources().getColor(R.color.increasing_color));
            holder.mTvChange.setText(String.format(Locale.US, "+%.2f%%", change * 100));
        }  else if (change < 0.f) {
            holder.mTvChange.setTextColor(mContext.getResources().getColor(R.color.decreasing_color));
            holder.mTvChange.setText(String.format(Locale.US, "%.2f%%", change * 100));
        }else {
            holder.mTvChange.setText(watchlist.getCurrentPrice() == 0.f ? "-" : "0.00%");
            holder.mTvChange.setTextColor(mContext.getResources().getColor(R.color.no_change_color));
        }
        holder.mTvVolume.setText(watchlist.getQuoteVol() == 0.f ? "-" : MyUtils.getNumberKMGExpressionFormat(watchlist.getQuoteVol()));
    }

    @Override
    public int getItemCount() {
        return mWatchlists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_watchlist_select_tv_quote_symbol)
        TextView mTvQuoteSymbol;
        @BindView(R.id.item_watchlist_select_tv_base_symbol)
        TextView mTvBaseSymbol;
        @BindView(R.id.item_watchlist_select_tv_24_change)
        TextView mTvChange;
        @BindView(R.id.item_watchlist_select_tv_24_volume)
        TextView mTvVolume;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }


    }

    public interface OnItemClickListener{
        void onItemClick(WatchlistData watchlist);
    }
}
