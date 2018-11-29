package com.cybexmobile.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybex.basemodule.adapter.viewholder.EmptyViewHolder;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.R;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.MarketTicker;
import com.cybex.basemodule.utils.AssetUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PortfolioRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_CONTENT = 1;

    private int mLayoutId;
    private List<AccountBalanceObjectItem> mBalanceObjectItems;
    private Context mContext;

    public PortfolioRecyclerViewAdapter(@LayoutRes int layoutId, List<AccountBalanceObjectItem> balanceObjectItems, Context context) {
        mLayoutId = layoutId;
        mBalanceObjectItems = balanceObjectItems;
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_EMPTY) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_empty, parent, false);
            return new EmptyViewHolder(view);
        }
        view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.mIvImage.setImageResource(R.drawable.ic_no_assert);
            emptyViewHolder.mTvEmpty.setText(mContext.getResources().getString(R.string.balance_page_no_asset));
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;
        AccountBalanceObjectItem item = mBalanceObjectItems.get(position);
        AccountBalanceObject accountBalanceObject = item.accountBalanceObject;
        AssetObject assetObject = item.assetObject;
        loadImage(accountBalanceObject.asset_type.toString(), viewHolder.mAssetImage);
        if (assetObject == null) {
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
            double price = accountBalanceObject.balance / Math.pow(10, assetObject.precision);
            viewHolder.mAssetCybAmount.setText(String.format("%." + assetObject.precision + "f", price));
            viewHolder.mAssetFrozenAmount.setText(item.frozenAmount == 0 ? mContext.getResources().getString(R.string.balance_page_frozen_no_asset) : mContext.getResources().getString(R.string.balance_page_frozen_asset) + AssetUtil.formatNumberRounding(item.frozenAmount, assetObject.precision));
            viewHolder.mAssetRmb.setText(item.balanceItemRmbPrice == 0 ? "-" : "≈¥" + AssetUtil.formatNumberRounding(item.balanceItemRmbPrice * price, 4));
        }

    }

    @Override
    public int getItemViewType(int position) {
        return mBalanceObjectItems == null || mBalanceObjectItems.size() == 0 ? TYPE_EMPTY : TYPE_CONTENT;
    }

    @Override
    public int getItemCount() {
        return mBalanceObjectItems == null || mBalanceObjectItems.size() == 0 ? 1 : mBalanceObjectItems.size();
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
