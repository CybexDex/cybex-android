package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;

import java.util.List;

public class OpenOrderRecyclerViewAdapter extends RecyclerView.Adapter<OpenOrderRecyclerViewAdapter.ViewHolder> {

    private List<LimitOrderObject>  mDataList;
    private List<Boolean> mBooleanList;
    private List<List<AssetObject>> mAssetObjectList;
    private Context mContext;

    public OpenOrderRecyclerViewAdapter(List<LimitOrderObject> dataList, List<Boolean> booleanList, Context context, List<List<AssetObject>> assetObjectList) {
        mDataList = dataList;
        mBooleanList = booleanList;
        mContext = context;
        mAssetObjectList = assetObjectList;
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
        AssetObject quote = mAssetObjectList.get(position).get(0);
        AssetObject base = mAssetObjectList.get(position).get(1);
        LimitOrderObject data = mDataList.get(position);
        if (mBooleanList.get(position)) {
            holder.mSellOrBuyTextView.setText(mContext.getResources().getString(R.string.open_order_sell));
            holder.mSellOrBuyTextView.setBackground(mContext.getResources().getDrawable(R.drawable.sell_item_background));
            if (data.sell_price.base.asset_id.equals(base.id)) {
                holder.mVolumeTextView.setText(String.valueOf(data.sell_price.base.amount / Math.pow(10, base.precision)));
            } else {
                holder.mVolumeTextView.setText(String.valueOf(data.sell_price.quote.amount / Math.pow(10, base.precision)));
            }
        } else {
            holder.mSellOrBuyTextView.setText(mContext.getResources().getString(R.string.open_order_buy));
            holder.mSellOrBuyTextView.setBackground(mContext.getResources().getDrawable(R.drawable.buy_item_background));
            if (data.sell_price.quote.asset_id.equals(quote.id)) {
                holder.mVolumeTextView.setText(String.valueOf(data.sell_price.quote.amount / Math.pow(10, quote.precision)));
            } else {
                holder.mVolumeTextView.setText(String.valueOf(data.sell_price.base.amount / Math.pow(10, quote.precision)));
            }
        }
        if (data.sell_price.base.asset_id.equals(base.id)) {
            holder.mPriceTextView.setText(String.valueOf((data.sell_price.base.amount / Math.pow(10, base.precision)) / (data.sell_price.quote.amount / Math.pow(10, quote.precision))));
        } else {
            holder.mPriceTextView.setText(String.valueOf((data.sell_price.quote.amount / Math.pow(10, quote.precision)) / (data.sell_price.base.amount / Math.pow(10, base.precision))));
        }
        holder.mQuoteTextView.setText(quote.symbol);
        holder.mBaseTextView.setText(String.format("/%s", base.symbol));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.open_order_item, parent, false);
        return new ViewHolder(view);
    }
}
