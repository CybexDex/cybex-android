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
import com.squareup.picasso.Picasso;

import java.util.List;

public class PortfolioRecyclerViewAdapter extends RecyclerView.Adapter<PortfolioRecyclerViewAdapter.ViewHolder> {
    private List<AccountBalanceObject> mAssetList;


    public PortfolioRecyclerViewAdapter(List<AccountBalanceObject> assetList) {
            mAssetList = assetList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        ImageView mAssetImage;
        TextView mAssetName;
        TextView mAssetPrice;
        TextView mAssetPriceCny;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mAssetImage = view.findViewById(R.id.account_portfolio_item_asset_image);
            mAssetName = view.findViewById(R.id.account_portfolio_item_asset);
            mAssetPrice = view.findViewById(R.id.account_portfolio_item_asset_price);
            mAssetPriceCny = view.findViewById(R.id.account_portfolio_item_cny_price);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_portfolio_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AssetObject mAssetObject;
        double priceCyb;
        try {
            mAssetObject = BitsharesWalletWraper.getInstance().get_objects(mAssetList.get(position).asset_type.toString());
            if (!mAssetObject.symbol.equals("CYB")) {
                priceCyb = BitsharesWalletWraper.getInstance().get_ticker("1.3.0", mAssetObject.id.toString()).latest;
            } else {
                priceCyb = 1;
            }
            double price = mAssetList.get(position).balance / Math.pow(10, mAssetObject.precision);
            if (mAssetObject.symbol.contains("JADE")) {
                holder.mAssetName.setText(mAssetObject.symbol.substring(0, 5));
            } else {
                holder.mAssetName.setText(mAssetObject.symbol);
            }
            loadImage(mAssetList.get(position).asset_type.toString(), holder.mAssetImage);
            holder.mAssetPrice.setText(String.valueOf(mAssetList.get(position).balance / Math.pow(10 ,mAssetObject.precision)));
            holder.mAssetPriceCny.setText(String.valueOf(price * priceCyb));
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mAssetList.size();
    }

    private void loadImage(String quoteId, ImageView mCoinSymbol) {
        String quoteIdWithUnderLine = quoteId.replaceAll("\\.", "_");
        Picasso.get()
                .load("https://cybex.io/icons/" + quoteIdWithUnderLine +"_grey.png").into(mCoinSymbol);
    }

}
