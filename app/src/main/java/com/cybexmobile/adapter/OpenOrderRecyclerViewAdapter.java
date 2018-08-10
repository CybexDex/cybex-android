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

import com.cybexmobile.R;
import com.cybexmobile.adapter.viewholder.EmptyViewHolder;
import com.cybexmobile.data.item.OpenOrderItem;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.utils.AssetUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OpenOrderRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_CONTENT = 1;

    private List<OpenOrderItem> mOpenOrderItems;
    private List<OpenOrderItem> mOriginalOpenOrderItems;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public OpenOrderRecyclerViewAdapter(Context context, List<OpenOrderItem> dataList) {
        mOpenOrderItems = dataList;
        mOriginalOpenOrderItems = dataList;
        mContext = context;
    }

    public void setOpenOrderItems(List<OpenOrderItem> openOrderItems){
        mOpenOrderItems = openOrderItems;
        mOriginalOpenOrderItems = openOrderItems;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mOnItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if(viewType == TYPE_EMPTY){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_empty, parent, false);
            return new EmptyViewHolder(view);
        }
        view = LayoutInflater.from(mContext).inflate(R.layout.item_exchange_open_order, parent, false);
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
                if (openOrderItem.isSell) {
                    viewHolder.mTvBuySell.setText(mContext.getResources().getString(R.string.open_order_sell));
                    viewHolder.mTvBuySell.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_sell));
                    price = (data.sell_price.quote.amount / Math.pow(10, quote.precision)) / (data.sell_price.base.amount / Math.pow(10, base.precision));
                    viewHolder.mTvAssetPrice.setText(String.format("%s %s", AssetUtil.formatNumberRounding(price, AssetUtil.pricePrecision(price)), quoteSymbol));
                    /**
                     * fix bug:CYM-349
                     * 订单部分撮合
                     */
                    amount = data.for_sale / Math.pow(10, base.precision);
                    viewHolder.mTvAssetAmount.setText(String.format("%s %s", AssetUtil.formatNumberRounding(amount, AssetUtil.amountPrecision(price)), baseSymbol));
                    viewHolder.mTvQuoteSymbol.setText(baseSymbol);
                    viewHolder.mTvBaseSymbol.setText(quoteSymbol);
                    viewHolder.mTvFilled.setText(String.format("%s %s", AssetUtil.formatNumberRounding(price * amount, AssetUtil.pricePrecision(price)), quoteSymbol));
                } else {
                    /**
                     * fix bug:CYM-412
                     * 买单数据显示错误
                     */
                    viewHolder.mTvBuySell.setText(mContext.getResources().getString(R.string.open_order_buy));
                    viewHolder.mTvBuySell.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_buy));
                    amount = data.sell_price.quote.amount / Math.pow(10, quote.precision);
                    price = (data.sell_price.base.amount / Math.pow(10, base.precision)) / amount;
                    viewHolder.mTvAssetPrice.setText(String.format("%s %s", AssetUtil.formatNumberRounding(price, AssetUtil.pricePrecision(price)), baseSymbol));
                    viewHolder.mTvAssetAmount.setText(String.format("%s %s", AssetUtil.formatNumberRounding(amount, AssetUtil.amountPrecision(price)), quoteSymbol));
                    viewHolder.mTvQuoteSymbol.setText(quoteSymbol);
                    viewHolder.mTvBaseSymbol.setText(baseSymbol);
                    viewHolder.mTvFilled.setText(String.format("%s %s", AssetUtil.formatNumberRounding(data.for_sale / Math.pow(10, base.precision), AssetUtil.pricePrecision(price)), baseSymbol));
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
        return mOpenOrderItems == null || mOpenOrderItems.size() == 0 ? 1 : mOpenOrderItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mOpenOrderItems == null || mOpenOrderItems.size() == 0 ? TYPE_EMPTY : TYPE_CONTENT;
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

        @OnClick(R.id.item_exchange_open_order_btn_cancel)
        public void onCancelClick(View view){
            if(mOnItemClickListener != null){
                mOnItemClickListener.onItemClick(mOpenOrderItems.get(getAdapterPosition()));
            }
        }
    }

    public interface OnItemClickListener{
        void onItemClick(OpenOrderItem itemValue);
    }

}
