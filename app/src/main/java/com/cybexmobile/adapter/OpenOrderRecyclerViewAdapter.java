package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.activity.OpenOrdersActivity;
import com.cybexmobile.data.item.OpenOrderItem;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;

public class OpenOrderRecyclerViewAdapter extends RecyclerView.Adapter<OpenOrderRecyclerViewAdapter.ViewHolder> implements Filterable {

    private List<OpenOrderItem> mOpenOrderItems;
    private List<OpenOrderItem> mOriginalOpenOrderItems;
    private Context mContext;
    private double mTotal;
    private getTotalValueInterface mListener;

    public interface getTotalValueInterface {
        void displayTotalValue(double total);
    }

    public OpenOrderRecyclerViewAdapter(List<OpenOrderItem> dataList, Context context, getTotalValueInterface listener) {
        mOpenOrderItems = dataList;
        mOriginalOpenOrderItems = dataList;
        mContext = context;
        mListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView mSellOrBuyTextView;
        TextView mBaseTextView;
        TextView mQuoteTextView;
        TextView mVolumeTextView;
        TextView mPriceTextView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mSellOrBuyTextView = view.findViewById(R.id.sell_or_buy_text);
            mBaseTextView = view.findViewById(R.id.base_currency_open_order);
            mQuoteTextView = view.findViewById(R.id.quote_currency_open_order);
            mVolumeTextView = view.findViewById(R.id.volume);
            mPriceTextView = view.findViewById(R.id.current_price_open_order);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        OpenOrderItem openOrderItem = mOpenOrderItems.get(position);
        AssetObject base = openOrderItem.openOrder.getBaseObject();
        AssetObject quote = openOrderItem.openOrder.getQuoteObject();
        LimitOrderObject data = openOrderItem.openOrder.getLimitOrder();
        double amount;
        double price;
        if (position == 0) {
            mTotal = 0;
        }
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
                    /**
                     * fix bug:CYM-426
                     * 订单部分撮合
                     */
                    holder.mSellOrBuyTextView.setText(mContext.getResources().getString(R.string.open_order_sell));
                    holder.mSellOrBuyTextView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_sell));
                    amount = data.for_sale / Math.pow(10, base.precision);
                    holder.mVolumeTextView.setText(String.format("%s %s %s", mContext.getResources().getString(R.string.open_orders_volume), String.format(basePrecision, amount), baseSymbol));
                    holder.mQuoteTextView.setText(baseSymbol);
                    holder.mBaseTextView.setText(String.format("/%s", quoteSymbol));
                    price = (data.sell_price.quote.amount / Math.pow(10, quote.precision)) / (data.sell_price.base.amount / Math.pow(10, base.precision));
                    holder.mPriceTextView.setText(String.format("%s %s", String.format(quotePrecision, price), quoteSymbol));
                } else {
                    holder.mSellOrBuyTextView.setText(mContext.getResources().getString(R.string.open_order_buy));
                    holder.mSellOrBuyTextView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_buy));
                    amount = data.sell_price.quote.amount / Math.pow(10, quote.precision);
                    holder.mVolumeTextView.setText(String.format("%s %s %s", mContext.getResources().getString(R.string.open_orders_volume), String.format(quotePrecision, amount), quoteSymbol));
                    holder.mQuoteTextView.setText(quoteSymbol);
                    holder.mBaseTextView.setText(String.format("/%s", baseSymbol));
                    price = (data.sell_price.base.amount / Math.pow(10, base.precision)) / (data.sell_price.quote.amount / Math.pow(10, quote.precision));
                    holder.mPriceTextView.setText(String.format("%s %s", String.format(basePrecision, price), baseSymbol));
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_open_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public Filter getFilter() {
        return new OpenOrderFilter();
    }

    class OpenOrderFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterStr = constraint.toString();
            FilterResults results = new FilterResults();
            if (TextUtils.isEmpty(constraint) || "All".equals(filterStr)) {
                results.values = mOriginalOpenOrderItems;
                results.count = mOriginalOpenOrderItems.size();
                return results;
            }
            List<OpenOrderItem> filterDatas = new ArrayList<>();
            for (OpenOrderItem data : mOriginalOpenOrderItems) {
                if ("Sell".equals(filterStr) && data.isSell) {
                    filterDatas.add(data);
                } else if ("Buy".equals(filterStr) && !data.isSell) {
                    filterDatas.add(data);
                }
            }
            results.values = filterDatas;
            results.count = filterDatas.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mOpenOrderItems = (List<OpenOrderItem>) results.values;
            notifyDataSetChanged();
        }
    }
}
