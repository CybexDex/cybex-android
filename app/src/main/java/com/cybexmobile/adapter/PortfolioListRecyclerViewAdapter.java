package com.cybexmobile.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.R;
import com.cybexmobile.graphene.chain.AccountBalanceObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.market.MarketStat;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class PortfolioListRecyclerViewAdapter extends RecyclerView.Adapter<PortfolioListRecyclerViewAdapter.ViewHolder> {

    private List<AccountBalanceObject> mAccountBalanceObjectList;

    public PortfolioListRecyclerViewAdapter(List<AccountBalanceObject> accountBalanceObjectList) {
        mAccountBalanceObjectList = accountBalanceObjectList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        ImageView mAssetImage;
        TextView mAssetName;
        TextView mAssetPrice;
        TextView mAssetPriceCYB;
        TextView mAssetRmb;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mAssetImage = view.findViewById(R.id.account_portfolio_item_asset_image);
            mAssetName = view.findViewById(R.id.account_portfolio_item_asset);
            mAssetPrice = view.findViewById(R.id.account_portfolio_item_asset_price);
            mAssetPriceCYB = view.findViewById(R.id.portfolio_price_cyb);
            mAssetRmb = view.findViewById(R.id.portfolio_page_rmb);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_portfolio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AssetObject mAssetObject;
        double priceCyb;
        try {
            mAssetObject = BitsharesWalletWraper.getInstance().get_objects(mAccountBalanceObjectList.get(position).asset_type.toString());
            if (!mAssetObject.symbol.equals("CYB")) {
                priceCyb = BitsharesWalletWraper.getInstance().get_ticker("1.3.0", mAssetObject.id.toString()).latest;
            } else {
                priceCyb = 1;
            }
            double price = mAccountBalanceObjectList.get(position).balance / Math.pow(10, mAssetObject.precision);
            holder.mAssetName.setText(mAssetObject.symbol.contains("JADE") ? mAssetObject.symbol.substring(5, mAssetObject.symbol.length()) : mAssetObject.symbol);
            loadImage(mAccountBalanceObjectList.get(position).asset_type.toString(), holder.mAssetImage);
            holder.mAssetPrice.setText(String.format(Locale.US, "%.5f", mAccountBalanceObjectList.get(position).balance / Math.pow(10 ,mAssetObject.precision)));
            holder.mAssetPriceCYB.setText(String.format(Locale.US, "%.5f CYB", price * priceCyb));
            holder.mAssetRmb.setText(String.format(Locale.US, "≈¥%.2f", MarketStat.getInstance().getRMBPriceFromHashMap("CYB") * price * priceCyb));
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

    }

    private void loadImage(String quoteId, ImageView mCoinSymbol) {
        String quoteIdWithUnderLine = quoteId.replaceAll("\\.", "_");
        Picasso.get()
                .load("https://cybex.io/icons/" + quoteIdWithUnderLine +"_grey.png").into(mCoinSymbol);
    }

    @Override
    public int getItemCount() {
        return mAccountBalanceObjectList.size();
    }
}
