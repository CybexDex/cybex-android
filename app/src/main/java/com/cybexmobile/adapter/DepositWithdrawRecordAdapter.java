package com.cybexmobile.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.data.GatewayDepositWithdrawRecordsItem;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.utils.DateUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.cybexmobile.utils.DateUtils.PATTERN_MM_dd_HH_mm_ss;

public class DepositWithdrawRecordAdapter extends RecyclerView.Adapter<DepositWithdrawRecordAdapter.ViewHolder> {

    Context mContext;
    private List<GatewayDepositWithdrawRecordsItem> mGatewayDepositWithdrawRecordsItem = new ArrayList<>();

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_deposit_withdraw_asset_icon)
        ImageView mAssetIcon;
        @BindView(R.id.item_deposit_withdraw_asset_symbol)
        TextView mAssetSymbol;
        @BindView(R.id.item_deposit_withdraw_asset_amount)
        TextView mAssetAmount;
        @BindView(R.id.item_deposit_withdraw_update_time)
        TextView mAssetUpdateTime;
        @BindView(R.id.item_deposit_withdraw_status)
        TextView mAssetStatus;
        @BindView(R.id.item_deposit_withdraw_address)
        TextView mAssetAddress;
        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public DepositWithdrawRecordAdapter(Context context, List<GatewayDepositWithdrawRecordsItem> gatewayDepositWithdrawRecordsItemList) {
        mContext = context;
        mGatewayDepositWithdrawRecordsItem = gatewayDepositWithdrawRecordsItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deposit_withdraw_records_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mGatewayDepositWithdrawRecordsItem.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GatewayDepositWithdrawRecordsItem item = mGatewayDepositWithdrawRecordsItem.get(position);
        AssetObject itemAssetObject = item.getItemAsset();
        loadImage(itemAssetObject.id.toString(), holder.mAssetIcon);
        holder.mAssetSymbol.setText(item.getRecord().getCoinType());
        holder.mAssetAmount.setText(String.format("%."+ itemAssetObject.precision + "f %s", item.getRecord().getAmount() / Math.pow(10, itemAssetObject.precision), item.getRecord().getCoinType()));
        holder.mAssetUpdateTime.setText(DateUtils.formatToDate(PATTERN_MM_dd_HH_mm_ss, DateUtils.formatToMillis(item.getRecord().getUpdateAt())));
        holder.mAssetStatus.setText(String.format("%s%s", item.getRecord().getState().substring(0, 1).toUpperCase(), item.getRecord().getState().substring(1)));
        holder.mAssetAddress.setText(item.getRecord().getAddress());
    }

    private void loadImage(String quoteId, ImageView mCoinSymbol) {
        String quoteIdWithUnderLine = quoteId.replaceAll("\\.", "_");
        Picasso.get().load("https://app.cybex.io/icons/" + quoteIdWithUnderLine + "_grey.png").into(mCoinSymbol);
    }
}
