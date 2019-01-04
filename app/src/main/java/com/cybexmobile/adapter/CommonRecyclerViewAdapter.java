package com.cybexmobile.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.activity.lockassets.LockAssetsActivity;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.LockAssetObject;
import com.cybex.provider.graphene.chain.MarketTicker;
import com.cybex.basemodule.utils.DateUtils;
import com.cybexmobile.shake.AntiShake;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommonRecyclerViewAdapter extends RecyclerView.Adapter<CommonRecyclerViewAdapter.ViewHolder> {

    private List<LockAssetsActivity.LockAssetItem> mDatas;
    private OnClickLockAssetItemListener mListener;

    public interface OnClickLockAssetItemListener {
         void onClick(LockAssetsActivity.LockAssetItem lockAssetItem);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.lock_up_asset_image)
        ImageView mAssetSymbol;
        @BindView(R.id.lock_up_asset_symbol)
        TextView mAssetText;
        @BindView(R.id.lock_up_asset_price)
        TextView mAssetPrice;
        @BindView(R.id.lock_up_asset_rmb)
        TextView mRmbPrice;
        @BindView(R.id.lock_up_asset_expire_date)
        TextView mExpirationDate;
        @BindView(R.id.lock_up_asset_locked_tv)
        TextView mLockedTv;
        @BindView(R.id.lock_up_asset_claim_button)
        Button mBalanceClaimButton;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }


    public CommonRecyclerViewAdapter(List<LockAssetsActivity.LockAssetItem> datas, OnClickLockAssetItemListener lockAssetItemListener) {
        mDatas = datas;
        mListener = lockAssetItemListener;
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
            if (timeStamp + duration * 1000 > currentTimeStamp ) {
                holder.mLockedTv.setVisibility(View.VISIBLE);
                holder.mBalanceClaimButton.setVisibility(View.GONE);
            } else {
                holder.mLockedTv.setVisibility(View.GONE);
                holder.mBalanceClaimButton.setVisibility(View.VISIBLE);
            }

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

        if (holder.mBalanceClaimButton != null) {
            holder.mBalanceClaimButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AntiShake.check(v.getId())) { return; }
                    if (mListener != null) {
                        mListener.onClick(item);
                    }
                }
            });
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
