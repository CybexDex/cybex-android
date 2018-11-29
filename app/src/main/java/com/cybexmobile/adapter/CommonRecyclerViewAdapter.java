package com.cybexmobile.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.activity.lockassets.LockAssetsActivity;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.LockAssetObject;
import com.cybex.provider.graphene.chain.MarketTicker;
import com.cybex.basemodule.utils.DateUtils;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class CommonRecyclerViewAdapter extends RecyclerView.Adapter<CommonRecyclerViewAdapter.ViewHolder> {

    private List<LockAssetsActivity.LockAssetItem> mDatas;

    protected class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mAssetSymbol;
        TextView mAssetText;
        TextView mAssetPrice;
        TextView mRmbPrice;
        TextView mProgressText;
        ProgressBar mProgressbar;
        TextView mExpirationDate;

        ViewHolder(View view) {
            super(view);
            mAssetSymbol = view.findViewById(R.id.lock_up_asset_image);
            mAssetText = view.findViewById(R.id.lock_up_asset_symbol);
            mAssetPrice = view.findViewById(R.id.lock_up_asset_price);
            mRmbPrice = view.findViewById(R.id.lock_up_asset_rmb);
            mProgressText = view.findViewById(R.id.lock_up_asset_progress_text);
            mProgressbar = view.findViewById(R.id.lock_up_asset_progress_bar);
            mExpirationDate = view.findViewById(R.id.lock_up_asset_expire_date);
        }
    }


    public CommonRecyclerViewAdapter(List<LockAssetsActivity.LockAssetItem> datas) {
        mDatas = datas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lockup_assets, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LockAssetsActivity.LockAssetItem item = mDatas.get(position);
        LockAssetObject lockAssetObject = item.lockAssetobject;
        double rmbPrice = 0;
        if (lockAssetObject != null) {
            loadImage(lockAssetObject.balance.asset_id.toString(), holder.mAssetSymbol);
            long timeStamp = DateUtils.formatToMillis(lockAssetObject.vesting_policy.begin_timestamp);
            long currentTimeStamp = System.currentTimeMillis();
            long duration = lockAssetObject.vesting_policy.vesting_duration_seconds;
            long time = (currentTimeStamp - timeStamp) / 1000;
            int progress = (int) (100 * time / duration);
            holder.mProgressbar.setProgress(progress);
            holder.mProgressText.setText(String.format("%s%%", progress >= 100 ? 100 : progress));
            holder.mExpirationDate.setText(DateUtils.formatToDate(DateUtils.PATTERN_yyyy_MM_dd, timeStamp + duration * 1000));
        }
        AssetObject assetObject = item.assetObject;
        if (assetObject != null) {
            String precisionFormmatter = "%." + assetObject.precision + "f";
            double price = (lockAssetObject.balance.amount) / Math.pow(10, assetObject.precision);
            holder.mAssetPrice.setText(String.format(Locale.US, precisionFormmatter, price));

            holder.mRmbPrice.setText(item.itemRmbPrice != 0 ? String.format(Locale.US, "≈¥%.4f", item.itemRmbPrice * price) : "--");
            if (assetObject.symbol.contains("JADE")) {
                holder.mAssetText.setText(assetObject.symbol.substring(5, assetObject.symbol.length()));
            } else {
                holder.mAssetText.setText(assetObject.symbol);
            }
        }

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    private void loadImage(String quoteId, ImageView mCoinSymbol) {
        String quoteIdWithUnderLine = quoteId.replaceAll("\\.", "_");
        Picasso.get().load("https://app.cybex.io/icons/" + quoteIdWithUnderLine + "_grey.png").into(mCoinSymbol);
    }

}
