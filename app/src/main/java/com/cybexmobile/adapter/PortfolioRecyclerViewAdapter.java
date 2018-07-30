package com.cybexmobile.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybexmobile.adapter.viewholder.EmptyViewHolder;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.R;
import com.cybexmobile.graphene.chain.AccountBalanceObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.market.MarketTicker;
import com.cybexmobile.utils.AssetUtil;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class PortfolioRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int mLayoutId;
    private List<AccountBalanceObjectItem> mBalanceObjectItems;
    private Context mContext;

    public PortfolioRecyclerViewAdapter(@LayoutRes int layoutId, List<AccountBalanceObjectItem> balanceObjectItems, Context context) {
        mLayoutId = layoutId;
        mBalanceObjectItems = balanceObjectItems;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        AccountBalanceObjectItem item = mBalanceObjectItems.get(position);
        AccountBalanceObject accountBalanceObject = item.accountBalanceObject;
        AssetObject assetObject = item.assetObject;
        MarketTicker marketTicker = item.marketTicker;
        loadImage(accountBalanceObject.asset_type.toString(), viewHolder.mAssetImage);
        /**
         * fix bug:CYM-255
         * 只显示前缀为CYB和JADE的资产
         */
        if (assetObject == null || (!assetObject.symbol.startsWith("CYB") && !assetObject.symbol.startsWith("JADE"))) {
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.height = 0;
            layoutParams.width = 0;
            holder.itemView.setVisibility(View.GONE);
        } else {
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.height = RecyclerView.LayoutParams.WRAP_CONTENT;
            layoutParams.width = RecyclerView.LayoutParams.WRAP_CONTENT;
            holder.itemView.setVisibility(View.VISIBLE);
            if (assetObject.symbol.contains("JADE")) {
                viewHolder.mAssetSymbol.setText(assetObject.symbol.substring(5, assetObject.symbol.length()));
            } else {
                viewHolder.mAssetSymbol.setText(assetObject.symbol);
            }
            double priceCyb = 0;
            if (assetObject.symbol.equals("CYB")) {
                priceCyb = 1;
            } else if (marketTicker != null) {
                priceCyb = marketTicker.latest;
            }
            double price = accountBalanceObject.balance / Math.pow(10, assetObject.precision);
            viewHolder.mAssetCybAmount.setText(AssetUtil.formatNumberRounding(price, assetObject.precision));
            viewHolder.mAssetFrozenAmount.setText(item.frozenAmount == 0 ? mContext.getResources().getString(R.string.balance_page_frozen_no_asset) : mContext.getResources().getString(R.string.balance_page_frozen_asset) + AssetUtil.formatNumberRounding(item.frozenAmount, assetObject.precision));
//            holder.mAssetAmount.setText(price * priceCyb == 0 ? "- CYB" : String.format("%s CYB", AssetUtil.formatNumberRounding(price * priceCyb, 5)));
            viewHolder.mAssetRmb.setText(price * priceCyb == 0 ? "-" : "≈¥" + AssetUtil.formatNumberRounding(item.cybPrice * price * priceCyb, 2));
        }

    }

    @Override
    public int getItemCount() {
        return mBalanceObjectItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mAssetImage;
        TextView mAssetSymbol;
        TextView mAssetFrozenAmount;
        TextView mAssetCybAmount;
        TextView mAssetRmb;

        ViewHolder(View view) {
            super(view);
            mAssetImage = view.findViewById(R.id.item_portfolio_asset_image);
            mAssetSymbol = view.findViewById(R.id.item_portfolio_asset_symbol);
            mAssetFrozenAmount = view.findViewById(R.id.item_portfolio_asset_amount);
            mAssetCybAmount = view.findViewById(R.id.item_portfolio_assets_cyb_amount);
            mAssetRmb = view.findViewById(R.id.item_portfolio_assets_rmb);
        }
    }

    private void loadImage(String quoteId, ImageView mCoinSymbol) {
        String quoteIdWithUnderLine = quoteId.replaceAll("\\.", "_");
        Picasso.get().load("https://app.cybex.io/icons/" + quoteIdWithUnderLine + "_grey.png").into(mCoinSymbol);
    }

}
