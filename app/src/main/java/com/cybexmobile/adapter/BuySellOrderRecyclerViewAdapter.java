package com.cybexmobile.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.fragment.OrderHistoryListFragment.OnListFragmentInteractionListener;
import com.cybexmobile.fragment.dummy.DummyContent.DummyItem;
import com.cybexmobile.market.Order;
import com.cybexmobile.market.OrderBook;
import com.cybexmobile.utils.AssetUtil;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BuySellOrderRecyclerViewAdapter extends RecyclerView.Adapter<BuySellOrderRecyclerViewAdapter.ViewHolder> {

    public static final int TYPE_BUY = 1;
    public static final int TYPE_SELL = 2;

    private Context mContext;
    private List<Order> mOrders;
    private int mType;
    private OnItemClickListener mListener;

    public BuySellOrderRecyclerViewAdapter(Context context, int type, List<Order> orders) {
        mContext = context;
        mOrders = orders;
        mType = type;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_buy_sell_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(mOrders.size() > position){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null){
                        mListener.onItemClick(mOrders.get(position));
                    }
                }
            });
            holder.mOrderPrice.setText(String.format(Locale.US, AssetUtil.formatPrice(mOrders.get(position).price), mOrders.get(position).price));
            holder.mOrderVolume.setText(String.format(Locale.US, AssetUtil.formatAmount(mOrders.get(position).price), mOrders.get(position).quoteAmount));
            float percentage = (float) getPercentage(mOrders, position);
            LinearLayout.LayoutParams layoutParams_colorBar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1 - percentage);
            LinearLayout.LayoutParams layoutParams_colorBarNon = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, percentage);
            holder.mColorBar.setLayoutParams(layoutParams_colorBar);
            holder.mColorBarNon.setLayoutParams(layoutParams_colorBarNon);
            holder.mColorBarNon.setBackgroundColor(Color.TRANSPARENT);
            holder.mColorBar.setBackgroundColor(mContext.getResources().getColor(mType == TYPE_BUY ? R.color.fade_background_green : R.color.fade_background_red));
        } else {
            holder.mOrderPrice.setText(mContext.getResources().getString(R.string.text_empty));
            holder.mOrderVolume.setText(mContext.getResources().getString(R.string.text_empty));
            holder.mColorBarNon.setBackgroundColor(Color.TRANSPARENT);
            holder.mColorBar.setBackgroundColor(Color.TRANSPARENT);
        }
        holder.mOrderPrice.setTextColor(mContext.getResources().getColor(mType == TYPE_BUY ? R.color.increasing_color : R.color.decreasing_color));
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    @Override
    public int getItemViewType(int position) {
        return mType;
    }

    private double getPercentage(List<Order> orderList, int position) {
        double divider = 0;
        double total = getSum(orderList);
        for (int i = 0; i <= position; i++) {
            if(mType == TYPE_BUY){
                divider += orderList.get(i).baseAmount;
            } else {
                if(i > 0){
                    divider += orderList.get(i -1).baseAmount;
                }
            }
        }
        return mType == TYPE_BUY ? divider / total : (total - divider)/total;
    }

    private double getSum(List<Order> orderList) {
        double sum = 0;
        if (orderList != null && orderList.size() != 0) {
            for (int i = 0; i < orderList.size(); i++) {
                sum += orderList.get(i).baseAmount;
            }
        }
        return sum;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_order_price) TextView mOrderPrice;
        @BindView(R.id.tv_order_amount) TextView mOrderVolume;
        @BindView(R.id.tv_color_bar_non) TextView mColorBarNon;
        @BindView(R.id.tv_color_bar) TextView mColorBar;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(Order order);
    }
}
