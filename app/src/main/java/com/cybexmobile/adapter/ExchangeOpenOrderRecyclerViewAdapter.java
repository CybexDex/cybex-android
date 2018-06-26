package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.data.item.OpenOrderItem;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.utils.MyUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExchangeOpenOrderRecyclerViewAdapter extends RecyclerView.Adapter<ExchangeOpenOrderRecyclerViewAdapter.ViewHolder> {

    private List<OpenOrderItem> mOpenOrderItems;
    private Context mContext;

    public ExchangeOpenOrderRecyclerViewAdapter(Context context, List<OpenOrderItem> dataList) {
        mOpenOrderItems = dataList;
        mContext = context;
    }

    public void setOpenOrderItems(List<OpenOrderItem> openOrderItems){
        mOpenOrderItems = openOrderItems;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_exchange_open_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        OpenOrderItem openOrderItem = mOpenOrderItems.get(position);
        AssetObject base = openOrderItem.openOrder.getBaseObject();
        AssetObject quote = openOrderItem.openOrder.getQuoteObject();
        LimitOrderObject data = openOrderItem.openOrder.getLimitOrder();
        double amount;
        double price;
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
                String basePrecision = MyUtils.getPrecisedFormatter(base.precision);
                String quotePrecision = MyUtils.getPrecisedFormatter(quote.precision);
                if (openOrderItem.isSell) {
                    holder.mTvBuySell.setText(mContext.getResources().getString(R.string.open_order_sell));
                    holder.mTvBuySell.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_sell));
                    amount = data.sell_price.base.amount / Math.pow(10, base.precision);
                    holder.mTvAssetAmount.setText(String.format(basePrecision + " %s", amount, baseSymbol));
                    holder.mTvQuoteSymbol.setText(baseSymbol);
                    holder.mTvBaseSymbol.setText(quoteSymbol);
                    price = (data.sell_price.quote.amount / Math.pow(10, quote.precision)) / (data.sell_price.base.amount / Math.pow(10, base.precision));
                    holder.mTvAssetPrice.setText(String.format(quotePrecision + " %s", price, quoteSymbol));
                    holder.mTvFilled.setText(String.format(quotePrecision + " %s", price * amount, quoteSymbol));
                } else {
                    holder.mTvBuySell.setText(mContext.getResources().getString(R.string.open_order_buy));
                    holder.mTvBuySell.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_buy));
                    amount = data.sell_price.quote.amount / Math.pow(10, quote.precision);
                    holder.mTvAssetAmount.setText(String.format(quotePrecision + " %s", amount, quoteSymbol));
                    holder.mTvQuoteSymbol.setText(quoteSymbol);
                    holder.mTvBaseSymbol.setText(baseSymbol);
                    price = (data.sell_price.base.amount / Math.pow(10, base.precision)) / (data.sell_price.quote.amount / Math.pow(10, quote.precision));
                    holder.mTvAssetPrice.setText(String.format(basePrecision + " %s", price, baseSymbol));
                    holder.mTvFilled.setText(String.format(basePrecision + " %s", price * amount, baseSymbol));
                }
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
        return mOpenOrderItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_exchange_open_order_tv_buy_sell)
        TextView mTvBuySell;
        @BindView(R.id.item_exchange_open_order_tv_quote_symbol)
        TextView mTvQuoteSymbol;
        @BindView(R.id.item_exchange_open_order_tv_base_symbol)
        TextView mTvBaseSymbol;
        @BindView(R.id.item_exchange_open_order_tv_filled)
        TextView mTvFilled;
        @BindView(R.id.item_exchange_open_order_tv_asset_price)
        TextView mTvAssetPrice;
        @BindView(R.id.item_exchange_open_order_tv_asset_amount)
        TextView mTvAssetAmount;
        @BindView(R.id.item_exchange_open_order_btn_cancel)
        TextView mBtnCancel;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
