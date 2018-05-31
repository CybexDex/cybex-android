package com.cybexmobile.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.LockUpAssetObject;
import com.cybexmobile.market.MarketStat;
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

    private List<LockUpAssetObject> mDatas;

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


    public CommonRecyclerViewAdapter(List<LockUpAssetObject> datas) {
        mDatas = datas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lockup_assets, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        long timeStamp = getTimeStamp(mDatas.get(position).vesting_policy.begin_timestamp);
        long currentTimeStamp = System.currentTimeMillis();
        long duration = mDatas.get(position).vesting_policy.vesting_duration_seconds;
        if (timeStamp + duration * 1000 > currentTimeStamp) {
            try {
                AssetObject assetObject = BitsharesWalletWraper.getInstance().get_objects(mDatas.get(position).balance.asset_id.toString());
                loadImage(assetObject.id.toString(), holder.mAssetSymbol);
                String precisionFormmatter ="%." + assetObject.precision + "f";
                double price = (mDatas.get(position).balance.amount) / Math.pow(10, assetObject.precision);
                holder.mAssetPrice.setText(String.format(Locale.US, precisionFormmatter, price));
                holder.mRmbPrice.setText(String.format(Locale.US, "≈¥%.2f", MarketStat.getInstance().getRMBPriceFromHashMap("CYB") * price ));

                if (assetObject.symbol.contains("JADE")) {
                    holder.mAssetText.setText(assetObject.symbol.substring(5, assetObject.symbol.length()));
                } else {
                    holder.mAssetText.setText(assetObject.symbol);
                }
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }

            long time = (currentTimeStamp - timeStamp) / 1000;
            holder.mProgressbar.setProgress((int) (100 * time / duration));
            holder.mProgressText.setText(String.format("%s%%", String.valueOf((100 * time / duration))));
            holder.mExpirationDate.setText(getDate(timeStamp + duration * 1000));
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

    private long getTimeStamp(String timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Calendar calendar = new GregorianCalendar();
        TimeZone mTimeZone = calendar.getTimeZone();
        int mOffset = mTimeZone.getRawOffset();
        try {
            Date parsedDate = dateFormat.parse(timestamp);
            return parsedDate.getTime() + mOffset;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void loadImage(String quoteId, ImageView mCoinSymbol) {
        String quoteIdWithUnderLine = quoteId.replaceAll("\\.", "_");
        Picasso.get().load("https://cybex.io/icons/" + quoteIdWithUnderLine + "_grey.png").into(mCoinSymbol);
    }

    private String getDate(long timeStamp){

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }

}
