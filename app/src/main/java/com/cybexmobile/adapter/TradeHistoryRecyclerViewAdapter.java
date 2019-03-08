package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybex.basemodule.cache.AssetPairCache;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.graphene.chain.AssetsPair;
import com.cybexmobile.R;
import com.cybex.basemodule.adapter.viewholder.EmptyViewHolder;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.basemodule.utils.DateUtils;
import com.cybexmobile.fragment.orders.TradeHistoryFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TradeHistoryRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_CONTENT = 1;

    private List<TradeHistoryFragment.TradeHistoryItem> mOrderHistoryItems;
    private Context mContext;

    public TradeHistoryRecyclerViewAdapter(Context context, List<TradeHistoryFragment.TradeHistoryItem> orderHistoryItems) {
        mOrderHistoryItems = orderHistoryItems;
        mContext = context;
    }

    public void setOrderHistoryItems(List<TradeHistoryFragment.TradeHistoryItem> orderHistoryItems){
        mOrderHistoryItems = orderHistoryItems;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if(viewType == TYPE_EMPTY){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_empty, parent, false);
            return new EmptyViewHolder(view);
        }
        view = LayoutInflater.from(mContext).inflate(R.layout.item_trade_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof EmptyViewHolder){
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.mTvEmpty.setText(mContext.getResources().getString(R.string.text_no_exchange_history));
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;
        TradeHistoryFragment.TradeHistoryItem orderHistoryItem = mOrderHistoryItems.get(position);
        AssetObject base = orderHistoryItem.baseAsset;
        AssetObject quote = orderHistoryItem.quoteAsset;
        AssetObject fee = orderHistoryItem.feeAsset;
        if (base != null && quote != null) {
            AssetsPair.Config assetPairConfig = AssetPairCache.getInstance().getAssetPairConfig(base.id.toString(), quote.id.toString());
            if(assetPairConfig == null) throw new NullPointerException("AssetsPair.Config can't null");
            if ((!base.symbol.startsWith("CYB") && !base.symbol.startsWith("JADE") && !base.symbol.startsWith("ARENA")) ||
                    (!quote.symbol.startsWith("CYB") && !quote.symbol.startsWith("JADE") && !quote.symbol.startsWith("ARENA"))) {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.height = 0;
                layoutParams.width = 0;
                holder.itemView.setVisibility(View.GONE);
            } else {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.height = RecyclerView.LayoutParams.WRAP_CONTENT;
                layoutParams.width = RecyclerView.LayoutParams.MATCH_PARENT;
                holder.itemView.setVisibility(View.VISIBLE);
                String quoteSymbol = AssetUtil.parseSymbol(quote.symbol);
                String baseSymbol = AssetUtil.parseSymbol(base.symbol);
                String feeSymbol = AssetUtil.parseSymbol(fee.symbol);
                viewHolder.mTvBuySell.setText(mContext.getResources().getString(orderHistoryItem.isSell ? R.string.open_order_sell : R.string.open_order_buy));
                viewHolder.mTvBuySell.setBackground(mContext.getResources().getDrawable(orderHistoryItem.isSell ?R.drawable.bg_btn_sell : R.drawable.bg_btn_buy));
                viewHolder.mTvBaseSymbol.setText(baseSymbol);
                viewHolder.mTvQuoteSymbol.setText(quoteSymbol);
                double baseAmount;
                double quoteAmount;
                double baseAmountForPrice;
                double quoteAmountForPrice;
                double feeAmount = AssetUtil.divide(orderHistoryItem.tradeHistory.fee.amount, Math.pow(10, fee.precision));
                if(orderHistoryItem.isSell){
                    baseAmount = AssetUtil.divide(orderHistoryItem.tradeHistory.receives.amount, Math.pow(10, base.precision));
                    quoteAmount = AssetUtil.divide(orderHistoryItem.tradeHistory.pays.amount, Math.pow(10, quote.precision));
                    if (orderHistoryItem.tradeHistory.receives.asset_id.equals(orderHistoryItem.tradeHistory.fill_price.quote.asset_id)) {
                        baseAmountForPrice = AssetUtil.divide(orderHistoryItem.tradeHistory.fill_price.quote.amount, Math.pow(10, base.precision));
                        quoteAmountForPrice = AssetUtil.divide(orderHistoryItem.tradeHistory.fill_price.base.amount, Math.pow(10, quote.precision));
                    } else {
                        baseAmountForPrice = AssetUtil.divide(orderHistoryItem.tradeHistory.fill_price.base.amount, Math.pow(10, base.precision));
                        quoteAmountForPrice = AssetUtil.divide(orderHistoryItem.tradeHistory.fill_price.quote.amount, Math.pow(10, quote.precision));
                    }
                }else {
                    baseAmount = AssetUtil.divide(orderHistoryItem.tradeHistory.pays.amount, Math.pow(10, base.precision));
                    quoteAmount = AssetUtil.divide(orderHistoryItem.tradeHistory.receives.amount, Math.pow(10, quote.precision));
                    if (orderHistoryItem.tradeHistory.pays.asset_id.equals(orderHistoryItem.tradeHistory.fill_price.quote.asset_id)) {
                        baseAmountForPrice = AssetUtil.divide(orderHistoryItem.tradeHistory.fill_price.quote.amount, Math.pow(10, base.precision));
                        quoteAmountForPrice = AssetUtil.divide(orderHistoryItem.tradeHistory.fill_price.base.amount, Math.pow(10, quote.precision));
                    } else {
                        baseAmountForPrice = AssetUtil.divide(orderHistoryItem.tradeHistory.fill_price.base.amount, Math.pow(10, base.precision));
                        quoteAmountForPrice = AssetUtil.divide(orderHistoryItem.tradeHistory.fill_price.quote.amount, Math.pow(10, quote.precision));
                    }
                }
                viewHolder.mTvPrice.setText(String.format("%s", AssetUtil.formatNumberRounding(AssetUtil.divide(baseAmountForPrice, quoteAmountForPrice), Integer.parseInt(assetPairConfig.last_price))));
                viewHolder.mTvTotal.setText(String.format("%s %s", AssetUtil.formatNumberRounding(baseAmount, Integer.parseInt(assetPairConfig.total)), baseSymbol));
                viewHolder.mTvTime.setText(DateUtils.formatToDate(DateUtils.PATTERN_MM_dd_HH_mm_ss, DateUtils.formatToMillis(orderHistoryItem.accountHistoryObject.timestamp)));
                viewHolder.mTvFilledAmount.setText(String.format("%s", AssetUtil.formatNumberRounding(quoteAmount, Integer.parseInt(assetPairConfig.amount))));
                viewHolder.mTvFee.setText(String.format("%s %s", AssetUtil.formatNumberRounding(feeAmount, fee.precision), feeSymbol));
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
        return mOrderHistoryItems == null || mOrderHistoryItems.size() == 0 ? 1 : mOrderHistoryItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mOrderHistoryItems == null || mOrderHistoryItems.size() == 0 ? TYPE_EMPTY : TYPE_CONTENT;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_trade_history_tv_buy_sell)
        TextView mTvBuySell;
        @BindView(R.id.item_trade_history_tv_quote_symbol)
        TextView mTvQuoteSymbol;
        @BindView(R.id.item_trade_history_tv_base_symbol)
        TextView mTvBaseSymbol;
        @BindView(R.id.item_trade_history_tv_price)
        TextView mTvPrice;
        @BindView(R.id.item_trade_history_tv_total)
        TextView mTvTotal;
        @BindView(R.id.item_trade_history_tv_time)
        TextView mTvTime;
        @BindView(R.id.item_trade_history_tv_filled_amount)
        TextView mTvFilledAmount;
        @BindView(R.id.item_trade_history_tv_fee)
        TextView mTvFee;


        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
