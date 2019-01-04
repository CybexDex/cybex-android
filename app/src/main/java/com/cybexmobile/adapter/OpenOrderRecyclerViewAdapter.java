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
        AssetObject baseAsset = openOrderItem.baseAsset;
        AssetObject quoteAsset = openOrderItem.quoteAsset;
        LimitOrder limitOrder = openOrderItem.limitOrder;
        if (baseAsset != null && quoteAsset != null) {
            AssetsPair.Config assetPairConfig = AssetPairCache.getInstance().getAssetPairConfig(baseAsset.id.toString(), quoteAsset.id.toString());
            if (assetPairConfig == null) throw new NullPointerException("AssetsPair.Config can't null");
            double amount;
            double price;
            double sold;
            if ((!baseAsset.symbol.startsWith("CYB") && !baseAsset.symbol.startsWith("JADE")) ||
                    (!quoteAsset.symbol.startsWith("CYB") && !quoteAsset.symbol.startsWith("JADE"))) {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.height = 0;
                layoutParams.width = 0;
                holder.itemView.setVisibility(View.GONE);
            } else {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.height = RecyclerView.LayoutParams.WRAP_CONTENT;
                layoutParams.width = RecyclerView.LayoutParams.WRAP_CONTENT;
                holder.itemView.setVisibility(View.VISIBLE);
                String quoteSymbol = AssetUtil.parseSymbol(quoteAsset.symbol);
                String baseSymbol = AssetUtil.parseSymbol(baseAsset.symbol);
                if (openOrderItem.isSell) {
                    viewHolder.mTvBuySell.setText(mContext.getResources().getString(R.string.open_order_sell));
                    viewHolder.mTvBuySell.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_sell));
                    viewHolder.mTvScale.setTextColor(mContext.getResources().getColor(R.color.decreasing_color));
                    amount = AssetUtil.divide(limitOrder.amount_to_sell, Math.pow(10, quoteAsset.precision));
                    sold = AssetUtil.divide(limitOrder.sold, Math.pow(10, quoteAsset.precision));
                    price = AssetUtil.divide(AssetUtil.divide(limitOrder.min_to_receive, Math.pow(10, baseAsset.precision)), amount);
                } else {
                    viewHolder.mTvBuySell.setText(mContext.getResources().getString(R.string.open_order_buy));
                    viewHolder.mTvBuySell.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_buy));
                    viewHolder.mTvScale.setTextColor(mContext.getResources().getColor(R.color.increasing_color));
                    amount = AssetUtil.divide(limitOrder.min_to_receive, Math.pow(10, quoteAsset.precision));
                    sold = AssetUtil.divide(limitOrder.received, Math.pow(10, quoteAsset.precision));
                    price = AssetUtil.divide(AssetUtil.divide(limitOrder.amount_to_sell, Math.pow(10, baseAsset.precision)), amount);
                }
                viewHolder.mTvQuoteSymbol.setText(quoteSymbol);
                viewHolder.mTvBaseSymbol.setText(baseSymbol);
                viewHolder.mTvAssetPrice.setText(String.format("%s %s", AssetUtil.formatNumberRounding(price, Integer.parseInt(assetPairConfig.last_price)), baseSymbol));
                viewHolder.mTvAssetAmount.setText(String.format("%s/%s %s", AssetUtil.formatNumberRounding(sold, Integer.parseInt(assetPairConfig.amount)),
                        AssetUtil.formatNumberRounding(amount, Integer.parseInt(assetPairConfig.amount)), quoteSymbol));
                viewHolder.mTvScale.setText(String.format("%s%%", AssetUtil.formatNumberRounding(sold / amount * 100,2)));
                viewHolder.mTvDate.setText(DateUtils.formatToDate(DateUtils.PATTERN_MM_dd_HH_mm_ss, DateUtils.formatToMillis(limitOrder.create_time)));
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
        @BindView(R.id.item_exchange_open_order_tv_asset_price)
        TextView mTvAssetPrice;
        @BindView(R.id.item_exchange_open_order_tv_asset_amount)
        TextView mTvAssetAmount;
        @BindView(R.id.item_exchange_open_order_tv_scale)
        TextView mTvScale;
        @BindView(R.id.item_exchange_open_order_tv_date)
        TextView mTvDate;
        @BindView(R.id.item_exchange_open_order_btn_cancel)
        TextView mBtnCancel;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.item_exchange_open_order_btn_cancel)
        public void onCancelClick(View view){
            if (AntiShake.check(view.getId())) { return; }
            /**
             * fix online bug
             * java.lang.ArrayIndexOutOfBoundsException: length=10; index=-1
             */
            if(mOnItemClickListener != null && getAdapterPosition() != -1){
                mOnItemClickListener.onItemClick(mOpenOrderItems.get(getAdapterPosition()));
            }
        }
    }

    public interface OnItemClickListener{
        void onItemClick(OpenOrderItem itemValue);
    }

}
