package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.activity.OwnOrderHistoryActivity;
import com.cybexmobile.data.item.OpenOrderItem;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.graphene.chain.OrderHistory;
import com.cybexmobile.utils.AssetUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OwnOrderHistoryRecyclerViewAdapter extends RecyclerView.Adapter<OwnOrderHistoryRecyclerViewAdapter.ViewHolder> {

    private List<OwnOrderHistoryActivity.OrderHistoryItem> mOrderHistoryItems;
    private Context mContext;

    public OwnOrderHistoryRecyclerViewAdapter(Context context, List<OwnOrderHistoryActivity.OrderHistoryItem> orderHistoryItems) {
        mOrderHistoryItems = orderHistoryItems;
        mContext = context;
    }

    public void setOrderHistoryItems(List<OwnOrderHistoryActivity.OrderHistoryItem> orderHistoryItems){
        mOrderHistoryItems = orderHistoryItems;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_own_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        OwnOrderHistoryActivity.OrderHistoryItem orderHistoryItem = mOrderHistoryItems.get(position);
        AssetObject base = orderHistoryItem.baseAsset;
        AssetObject quote = orderHistoryItem.quoteAsset;
        OrderHistory orderHistory = orderHistoryItem.orderHistory;
        if (base != null && quote != null) {
            if ((!base.symbol.startsWith("CYB") && !base.symbol.startsWith("JADE")) ||
                    (!quote.symbol.startsWith("CYB") && !quote.symbol.startsWith("JADE"))) {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.height = 0;
                layoutParams.width = 0;
                holder.itemView.setVisibility(View.GONE);
            } else {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.height = RecyclerView.LayoutParams.WRAP_CONTENT;
                layoutParams.width = RecyclerView.LayoutParams.WRAP_CONTENT;
                holder.itemView.setVisibility(View.VISIBLE);
                String quoteSymbol = quote.symbol.contains("JADE") ? quote.symbol.substring(5, quote.symbol.length()) : quote.symbol;
                String baseSymbol = base.symbol.contains("JADE") ? base.symbol.substring(5, base.symbol.length()) : base.symbol;
                holder.mTvBuySell.setText(mContext.getResources().getString(orderHistoryItem.isSell ? R.string.open_order_sell : R.string.open_order_buy));
                holder.mTvBuySell.setBackground(mContext.getResources().getDrawable(orderHistoryItem.isSell ?R.drawable.bg_btn_sell : R.drawable.bg_btn_buy));
                holder.mTvBaseSymbol.setText(baseSymbol);
                holder.mTvQuoteSymbol.setText(quoteSymbol);
                double baseAmount;
                double quoteAmount;
                if(orderHistoryItem.isSell){
                    baseAmount = orderHistoryItem.orderHistory.receives.amount / Math.pow(10, base.precision);
                    quoteAmount = orderHistoryItem.orderHistory.pays.amount / Math.pow(10, quote.precision);
                }else {
                    baseAmount = orderHistoryItem.orderHistory.pays.amount / Math.pow(10, base.precision);
                    quoteAmount = orderHistoryItem.orderHistory.receives.amount / Math.pow(10, quote.precision);
                }
                holder.mTvBasePrice.setText(String.format("%." + base.precision + "f %s", baseAmount/quoteAmount, baseSymbol));
                holder.mTvBaseAmount.setText(String.format("%." + base.precision + "f %s", baseAmount, baseSymbol));
                holder.mTvQuoteAmount.setText(String.format("%." + quote.precision +"f %s", quoteAmount, quoteSymbol));
            }
        }else{
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.height = 0;
                layoutParams.width = 0;
                holder.itemView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mOrderHistoryItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_own_order_history_tv_buy_sell)
        TextView mTvBuySell;
        @BindView(R.id.item_own_order_history_tv_quote_symbol)
        TextView mTvQuoteSymbol;
        @BindView(R.id.item_own_order_history_tv_base_symbol)
        TextView mTvBaseSymbol;
        @BindView(R.id.item_own_order_history_tv_base_amount)
        TextView mTvBaseAmount;
        @BindView(R.id.item_own_order_history_tv_base_price)
        TextView mTvBasePrice;
        @BindView(R.id.item_own_order_history_tv_quote_amount)
        TextView mTvQuoteAmount;
        @BindView(R.id.item_own_order_history_tv_time)
        TextView mTvTime;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
