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
import com.cybexmobile.utils.MyUtils;

import java.util.List;

public class OpenOrderRecyclerViewAdapter extends RecyclerView.Adapter<OpenOrderRecyclerViewAdapter.ViewHolder> {

    private List<LimitOrderObject>  mDataList;
    private List<Boolean> mBooleanList;
    private List<List<AssetObject>> mAssetObjectList;
    private Context mContext;
    private double mTotal;
    private getTotalValueInterface mListener;

    public interface getTotalValueInterface {
        void displayTotalValue(double total);
    }

    public OpenOrderRecyclerViewAdapter(List<LimitOrderObject> dataList, List<Boolean> booleanList, Context context, List<List<AssetObject>> assetObjectList, getTotalValueInterface listener) {
        mDataList = dataList;
        mBooleanList = booleanList;
        mContext = context;
        mAssetObjectList = assetObjectList;
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
        AssetObject quote = mAssetObjectList.get(position).get(0);
        AssetObject base = mAssetObjectList.get(position).get(1);
        LimitOrderObject data = mDataList.get(position);
        String quoteSymbol = quote.symbol.contains("JADE") ? quote.symbol.substring(5, quote.symbol.length()) : quote.symbol;
        String baseSymbol = base.symbol.contains("JADE") ? base.symbol.substring(5, base.symbol.length()) : base.symbol;
        String basePrecision = MyUtils.getPrecisedFormatter(base.precision);
        String quotePrecision = MyUtils.getPrecisedFormatter(quote.precision);
        double amount;
        double price;
        if (position == 0) {
            mTotal = 0;
        }
        if (mBooleanList.get(position)) {
            holder.mSellOrBuyTextView.setText(mContext.getResources().getString(R.string.open_order_sell));
            holder.mSellOrBuyTextView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_sell));
            if (data.sell_price.base.asset_id.equals(base.id)) {
                amount = data.sell_price.base.amount / Math.pow(10, base.precision);
                holder.mVolumeTextView.setText(String.format(basePrecision, amount) + " " + baseSymbol);
            } else {
                amount = data.sell_price.quote.amount / Math.pow(10, base.precision);
                holder.mVolumeTextView.setText(String.format(quotePrecision, amount) + " " + quoteSymbol);
            }
        } else {
            holder.mSellOrBuyTextView.setText(mContext.getResources().getString(R.string.open_order_buy));
            holder.mSellOrBuyTextView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_buy));
            if (data.sell_price.quote.asset_id.equals(quote.id)) {
                amount = data.sell_price.quote.amount / Math.pow(10, quote.precision);
                holder.mVolumeTextView.setText(String.format(quotePrecision, amount) + " " + quoteSymbol);
            } else {
                amount = data.sell_price.base.amount / Math.pow(10, quote.precision);
                holder.mVolumeTextView.setText(String.format(basePrecision, amount) + " " + baseSymbol);
            }
        }
        if (data.sell_price.base.asset_id.equals(base.id)) {
            price = (data.sell_price.base.amount / Math.pow(10, base.precision)) / (data.sell_price.quote.amount / Math.pow(10, quote.precision));
            holder.mPriceTextView.setText(String.format(basePrecision, price));
        } else {
            price = (data.sell_price.quote.amount / Math.pow(10, base.precision)) / (data.sell_price.base.amount / Math.pow(10, quote.precision));
            holder.mPriceTextView.setText(String.format(quotePrecision, price));
        }
        mTotal += price * amount;
        holder.mQuoteTextView.setText(quoteSymbol);
        holder.mBaseTextView.setText(String.format("/%s", baseSymbol));
        if (position == mDataList.size() - 1) {
            mListener.displayTotalValue(mTotal);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_open_order, parent, false);
        return new ViewHolder(view);
    }
}
