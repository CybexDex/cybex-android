package com.cybexmobile.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.cybex.basemodule.adapter.viewholder.EmptyViewHolder;
import com.cybex.basemodule.cache.AssetPairCache;
import com.cybex.basemodule.utils.DateUtils;
import com.cybex.provider.graphene.chain.AssetsPair;
import com.cybex.provider.graphene.chain.LimitOrder;
import com.cybexmobile.R;
import com.cybexmobile.data.item.OpenOrderItem;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybexmobile.shake.AntiShake;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OrdersHistoryRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_CONTENT = 1;

    private List<OpenOrderItem> mOpenOrderItems;
    private Context mContext;

    public OrdersHistoryRecyclerViewAdapter(Context context, List<OpenOrderItem> dataList) {
        mOpenOrderItems = dataList;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if(viewType == TYPE_EMPTY){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_empty, parent, false);
            return new EmptyViewHolder(view);
        }
        view = LayoutInflater.from(mContext).inflate(R.layout.item_orders_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof EmptyViewHolder){
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.mTvEmpty.setText(mContext.getResources().getString(R.string.text_no_open_order));
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;
        OpenOrderItem openOrderItem = mOpenOrderItems.get(position);
        AssetObject baseAsset = openOrderItem.baseAsset;
        AssetObject quoteAsset = openOrderItem.quoteAsset;
        LimitOrder limitOrder = openOrderItem.limitOrder;
        if (baseAsset != null && quoteAsset != null) {
            AssetsPair.Config assetPairConfig = AssetPairCache.getInstance().getAssetPairConfig(baseAsset.id.toString(), quoteAsset.id.toString());
            if (assetPairConfig == null) throw new NullPointerException("AssetsPair.Config can't null");
            double amount;
            double price;
            double sold;
            double received;
            if ((!baseAsset.symbol.startsWith("CYB") && !baseAsset.symbol.startsWith("JADE")) ||
                    (!quoteAsset.symbol.startsWith("CYB") && !quoteAsset.symbol.startsWith("JADE"))) {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.height = 0;
                layoutParams.width = 0;
                holder.itemView.setVisibility(View.GONE);
            } else {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.height = RecyclerView.LayoutParams.WRAP_CONTENT;
                layoutParams.width = RecyclerView.LayoutParams.MATCH_PARENT;
                holder.itemView.setVisibility(View.VISIBLE);
                String quoteSymbol = AssetUtil.parseSymbol(quoteAsset.symbol);
                String baseSymbol = AssetUtil.parseSymbol(baseAsset.symbol);
                if (openOrderItem.isSell) {
                    viewHolder.mTvBuySell.setText(mContext.getResources().getString(R.string.open_order_sell));
                    viewHolder.mTvBuySell.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_sell));
                    amount = AssetUtil.divide(limitOrder.amount_to_sell, Math.pow(10, quoteAsset.precision));
                    sold = AssetUtil.divide(limitOrder.sold, Math.pow(10, quoteAsset.precision));
                    received = AssetUtil.divide(limitOrder.received, Math.pow(10, baseAsset.precision));
                    price = AssetUtil.divide(AssetUtil.divide(limitOrder.min_to_receive, Math.pow(10, baseAsset.precision)), amount);
                } else {
                    viewHolder.mTvBuySell.setText(mContext.getResources().getString(R.string.open_order_buy));
                    viewHolder.mTvBuySell.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_buy));
                    amount = AssetUtil.divide(limitOrder.min_to_receive, Math.pow(10, quoteAsset.precision));
                    sold = AssetUtil.divide(limitOrder.received, Math.pow(10, quoteAsset.precision));
                    received = AssetUtil.divide(limitOrder.sold, Math.pow(10, baseAsset.precision));
                    price = AssetUtil.divide(AssetUtil.divide(limitOrder.amount_to_sell, Math.pow(10, baseAsset.precision)), amount);
                }
                viewHolder.mTvQuoteSymbol.setText(quoteSymbol);
                viewHolder.mTvBaseSymbol.setText(baseSymbol);
                viewHolder.mTvPrice.setText(String.format("%s", AssetUtil.formatNumberRounding(price, Integer.parseInt(assetPairConfig.last_price))));
                if (sold > 0) {
                    viewHolder.mTvAveragePrice.setText(String.format("%s", AssetUtil.formatNumberRounding(received/sold, Integer.parseInt(assetPairConfig.last_price))));
                    viewHolder.mTvFilledAmount.setText(String.format("%s %s", AssetUtil.formatNumberRounding(sold, Integer.parseInt(assetPairConfig.amount)), quoteSymbol));
                } else {
                    viewHolder.mTvAveragePrice.setText(mContext.getResources().getText(R.string.text_empty));
                    viewHolder.mTvFilledAmount.setText(mContext.getResources().getText(R.string.text_empty));
                }
                viewHolder.mTvAmount.setText(String.format("%s %s", AssetUtil.formatNumberRounding(amount, Integer.parseInt(assetPairConfig.amount)), quoteSymbol));
                viewHolder.mTvTime.setText(DateUtils.formatToDate(DateUtils.PATTERN_MM_dd_HH_mm_ss, DateUtils.formatToMillis(limitOrder.create_time)));
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
        return mOpenOrderItems == null || mOpenOrderItems.size() == 0 ? 1 : mOpenOrderItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mOpenOrderItems == null || mOpenOrderItems.size() == 0 ? TYPE_EMPTY : TYPE_CONTENT;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_orders_history_tv_buy_sell)
        TextView mTvBuySell;
        @BindView(R.id.item_orders_history_tv_quote_symbol)
        TextView mTvQuoteSymbol;
        @BindView(R.id.item_orders_history_tv_base_symbol)
        TextView mTvBaseSymbol;
        @BindView(R.id.item_orders_history_tv_price)
        TextView mTvPrice;
        @BindView(R.id.item_orders_history_tv_average_price)
        TextView mTvAveragePrice;
        @BindView(R.id.item_orders_history_tv_time)
        TextView mTvTime;
        @BindView(R.id.item_orders_history_tv_filled_amount)
        TextView mTvFilledAmount;
        @BindView(R.id.item_orders_history_tv_amount)
        TextView mTvAmount;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
