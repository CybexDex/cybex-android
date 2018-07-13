package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
    private WatchlistData mCurrentWatchlist;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public WatchlistSelectRecyclerViewAdapter(Context context, WatchlistData watchlistData, List<WatchlistData> watchlists) {
        mWatchlists = watchlists;
        mCurrentWatchlist = watchlistData;
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick(watchlist);
                }
            }
        });
        if(mCurrentWatchlist != null && mCurrentWatchlist.getBaseSymbol().equals(watchlist.getBaseSymbol()) &&
                mCurrentWatchlist.getQuoteSymbol().equals(watchlist.getQuoteSymbol())){
            holder.itemView.setSelected(true);
            holder.mTvSymbol.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
            holder.mTvSymbol.setSelected(false);
        }
        holder.mTvSymbol.setText(String.format("%s/%s", AssetUtil.parseSymbol(watchlist.getQuoteSymbol()), AssetUtil.parseSymbol(watchlist.getBaseSymbol())));
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
        holder.mTvVolume.setText(watchlist.getQuoteVol() == 0.f ? "-" : AssetUtil.formatAmountToKMB(watchlist.getQuoteVol(), 2));
    }

    @Override
    public int getItemCount() {
        return mWatchlists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_watchlist_select_tv_symbol)
        TextView mTvSymbol;
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
