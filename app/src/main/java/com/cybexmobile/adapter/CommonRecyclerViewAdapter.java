package com.cybexmobile.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.activity.LockAssetsActivity;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.LockUpAssetObject;
import com.cybexmobile.utils.DateUtils;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CommonRecyclerViewAdapter extends RecyclerView.Adapter<CommonRecyclerViewAdapter.ViewHolder> {

    private List<LockAssetsActivity.LockUpAssetItem> mDatas;

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


    public CommonRecyclerViewAdapter(List<LockAssetsActivity.LockUpAssetItem> datas) {
        mDatas = datas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lockup_assets, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LockAssetsActivity.LockUpAssetItem item = mDatas.get(position);
        LockUpAssetObject lockUpAssetObject = item.lockUpAssetobject;
        if(lockUpAssetObject != null){
            loadImage(lockUpAssetObject.balance.asset_id.toString(), holder.mAssetSymbol);
            long timeStamp = DateUtils.formatToMillis(lockUpAssetObject.vesting_policy.begin_timestamp);
            long currentTimeStamp = System.currentTimeMillis();
            long duration = lockUpAssetObject.vesting_policy.vesting_duration_seconds;
            long time = (currentTimeStamp - timeStamp) / 1000;
            holder.mProgressbar.setProgress((int) (100 * time / duration));
            holder.mProgressText.setText(String.format("%s%%", String.valueOf((100 * time / duration))));
            holder.mExpirationDate.setText(DateUtils.formatToDate(DateUtils.PATTERN_yyyy_MM_dd, timeStamp + duration * 1000));
        }
        AssetObject assetObject = item.assetObject;
        if(assetObject != null){
            String precisionFormmatter ="%." + assetObject.precision + "f";
            double price = (lockUpAssetObject.balance.amount) / Math.pow(10, assetObject.precision);
            holder.mAssetPrice.setText(String.format(Locale.US, precisionFormmatter, price));
            holder.mRmbPrice.setText(String.format(Locale.US, "≈¥%.2f", item.cybRmbPrice * price ));
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
