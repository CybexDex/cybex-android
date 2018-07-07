package com.cybexmobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.activity.DepositActivity;
import com.cybexmobile.activity.WithdrawActivity;
import com.cybexmobile.faucet.DepositAndWithdrawObject;
import com.cybexmobile.fragment.DepositItemFragment;
import com.cybexmobile.graphene.chain.AccountBalanceObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.market.MarketTicker;
import com.cybexmobile.utils.MyUtils;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class DepositAndWithdrawAdapter extends RecyclerView.Adapter<DepositAndWithdrawAdapter.ViewHolder> {

    private Context mContext;
    private String mName;
    private List<DepositAndWithdrawObject> mDataList;


    public DepositAndWithdrawAdapter(Context context, String name, List<DepositAndWithdrawObject> depositAndWithdrawObjectList) {
        mContext = context;
        mName = name;
        mDataList = depositAndWithdrawObjectList;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mAssetName;
        TextView mAssetFullName;
        TextView mAssetPrice;
        ImageView mAssetIcon;
        View mView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mAssetName = view.findViewById(R.id.gate_way_asset_name);
            mAssetFullName = view.findViewById(R.id.gate_way_asset_full_name);
            mAssetPrice = view.findViewById(R.id.gate_way_asset_price);
            mAssetIcon = view.findViewById(R.id.gate_way_asset_icon);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssetObject assetObject = mDataList.get(position).getAssetObject();
        AccountBalanceObject accountBalanceObject = mDataList.get(position).getAccountBalanceObject();
        if (assetObject != null) {
            holder.mAssetName.setText(MyUtils.removeJadePrefix(assetObject.symbol));
            loadImage(mDataList.get(position).getId(), holder.mAssetIcon);

            if (accountBalanceObject != null) {
                double balanceAmount = accountBalanceObject.balance / Math.pow(10, assetObject.precision);
                String objectFormatter = MyUtils.getPrecisedFormatter(assetObject.precision);
                holder.mAssetPrice.setText(String.format(Locale.US, objectFormatter, balanceAmount));
            } else {
                holder.mAssetPrice.setText("");
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mName.equals(DepositItemFragment.class.getName())) {
                        Intent intent = new Intent(mContext, DepositActivity.class);
                        intent.putExtra("assetName", MyUtils.removeJadePrefix(assetObject.symbol));
                        intent.putExtra("isEnabled", mDataList.get(position).isEnable());
                        intent.putExtra("enMsg", mDataList.get(position).getEnMsg());
                        intent.putExtra("cnMsg", mDataList.get(position).getCnMsg());
                        intent.putExtra("enInfo", mDataList.get(position).getEnInfo());
                        intent.putExtra("cnInfo", mDataList.get(position).getCnInfo());
                        mContext.startActivity(intent);
                    } else {
                        Intent intent = new Intent(mContext, WithdrawActivity.class);
                        intent.putExtra("assetName", MyUtils.removeJadePrefix(assetObject.symbol));
                        intent.putExtra("isEnabled", mDataList.get(position).isEnable());
                        intent.putExtra("enMsg", mDataList.get(position).getEnMsg());
                        intent.putExtra("cnMsg", mDataList.get(position).getCnMsg());
                        intent.putExtra("enInfo", mDataList.get(position).getEnInfo());
                        intent.putExtra("cnInfo", mDataList.get(position).getCnInfo());
                        intent.putExtra("assetObject", mDataList.get(position).getAssetObject());
                        if(accountBalanceObject != null) {
                            intent.putExtra("availableAmount", accountBalanceObject.balance / Math.pow(10, assetObject.precision));
                        }
                        mContext.startActivity(intent);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deposit_withdraw_item, parent, false);
        return new ViewHolder(view);
    }

    private void loadImage(String quoteId, ImageView mCoinSymbol) {
        String quoteIdWithUnderLine = quoteId.replaceAll("\\.", "_");
        Picasso.get()
                .load("https://app.cybex.io/icons/" + quoteIdWithUnderLine + "_grey.png")
                .into(mCoinSymbol);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }
}
