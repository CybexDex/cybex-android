package com.cybexmobile.adapter;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.R;
import com.cybexmobile.graphene.chain.AccountBalanceObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.market.MarketTicker;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class PortfolioRecyclerViewAdapter extends RecyclerView.Adapter<PortfolioRecyclerViewAdapter.ViewHolder> {
    private int mLayoutId;
    private List<AccountBalanceObjectItem> mBalanceObjectItems;

    public PortfolioRecyclerViewAdapter(@LayoutRes int layoutId, List<AccountBalanceObjectItem> balanceObjectItems) {
        mLayoutId = layoutId;
        mBalanceObjectItems = balanceObjectItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AccountBalanceObjectItem item = mBalanceObjectItems.get(position);
        AccountBalanceObject accountBalanceObject = item.accountBalanceObject;
        AssetObject assetObject = item.assetObject;
        MarketTicker marketTicker = item.marketTicker;
        loadImage(accountBalanceObject.asset_type.toString(), holder.mAssetImage);
        if(assetObject != null){
            if (assetObject.symbol.contains("JADE")) {
                holder.mAssetSymbol.setText(assetObject.symbol.substring(5,assetObject.symbol.length()));
            } else {
                holder.mAssetSymbol.setText(assetObject.symbol);
            }
            double priceCyb = 0;
            if(assetObject.symbol.equals("CYB")){
                priceCyb = 1;
            }else if(marketTicker != null){
                priceCyb = marketTicker.latest;
            }
            double price = accountBalanceObject.balance / Math.pow(10, assetObject.precision);
            holder.mAssetAmount.setText(String.valueOf(price));
            holder.mAssetCybAmount.setText(price * priceCyb == 0 ? "-" : String.format(Locale.US, "%.5f CYB", price * priceCyb));
            holder.mAssetRmb.setText(price * priceCyb == 0 ? "-" : String.format(Locale.US, "≈¥%.2f", item.cybPrice * price *priceCyb));
        }

    }

    @Override
    public int getItemCount() {
        return mBalanceObjectItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mAssetImage;
        TextView mAssetSymbol;
        TextView mAssetAmount;
        TextView mAssetCybAmount;
        TextView mAssetRmb;

        ViewHolder(View view) {
            super(view);
            mAssetImage = view.findViewById(R.id.item_portfolio_asset_image);
            mAssetSymbol = view.findViewById(R.id.item_portfolio_asset_symbol);
            mAssetAmount = view.findViewById(R.id.item_portfolio_asset_amount);
            mAssetCybAmount = view.findViewById(R.id.item_portfolio_assets_cyb_amount);
            mAssetRmb = view.findViewById(R.id.item_portfolio_assets_rmb);
        }
    }

    private void loadImage(String quoteId, ImageView mCoinSymbol) {
        String quoteIdWithUnderLine = quoteId.replaceAll("\\.", "_");
        Picasso.get().load("https://cybex.io/icons/" + quoteIdWithUnderLine +"_grey.png").into(mCoinSymbol);
    }

}
