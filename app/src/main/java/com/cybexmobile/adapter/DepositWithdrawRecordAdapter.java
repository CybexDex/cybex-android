package com.cybexmobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybex.basemodule.constant.Constant;
import com.cybex.provider.http.gateway.entity.GatewayNewDepositWithdrawRecordItem;
import com.cybexmobile.R;
import com.cybex.basemodule.adapter.viewholder.EmptyViewHolder;
import com.cybexmobile.data.item.GatewayDepositWithdrawRecordsItem;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.basemodule.utils.DateUtils;
import com.cybexmobile.intent.IntentFactory;
import com.cybexmobile.shake.AntiShake;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.cybex.basemodule.utils.DateUtils.PATTERN_MM_dd_HH_mm_ss;

public class DepositWithdrawRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_CONTENT = 1;

    Context mContext;
    private List<GatewayNewDepositWithdrawRecordItem> mGatewayDepositWithdrawRecordsItem = new ArrayList<>();

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_deposit_withdraw_asset_icon)
        ImageView mAssetIcon;
        @BindView(R.id.item_deposit_withdraw_asset_symbol)
        TextView mAssetSymbol;
        @BindView(R.id.item_deposit_withdraw_in_out_symbol)
        ImageView mInOutSymbol;
        @BindView(R.id.item_deposit_withdraw_asset_amount)
        TextView mAssetAmount;
        @BindView(R.id.item_deposit_withdraw_update_time)
        TextView mAssetUpdateTime;
        @BindView(R.id.item_deposit_withdraw_status)
        TextView mAssetStatus;
        @BindView(R.id.item_deposit_withdraw_note)
        TextView mAssetNote;
        @BindView(R.id.item_deposit_withdraw_address)
        TextView mAssetAddress;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public DepositWithdrawRecordAdapter(Context context, List<GatewayNewDepositWithdrawRecordItem> gatewayDepositWithdrawRecordsItemList) {
        mContext = context;
        mGatewayDepositWithdrawRecordsItem = gatewayDepositWithdrawRecordsItemList;
    }

    public void setData(List<GatewayNewDepositWithdrawRecordItem> gatewayDepositWithdrawRecordsItemList) {
        mGatewayDepositWithdrawRecordsItem = gatewayDepositWithdrawRecordsItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_EMPTY) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_empty, parent, false);
            return new EmptyViewHolder(view);
        }
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deposit_withdraw_records_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mGatewayDepositWithdrawRecordsItem == null || mGatewayDepositWithdrawRecordsItem.size() == 0 ? 1 : mGatewayDepositWithdrawRecordsItem.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mGatewayDepositWithdrawRecordsItem == null || mGatewayDepositWithdrawRecordsItem.size() == 0 ? TYPE_EMPTY : TYPE_CONTENT;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.mTvEmpty.setText(mContext.getResources().getString(R.string.deposit_withdraw_records_no_record));
            emptyViewHolder.mIvImage.setImageResource(R.drawable.ic_no_records);
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;
        GatewayNewDepositWithdrawRecordItem item = mGatewayDepositWithdrawRecordsItem.get(position);
        AssetObject itemAssetObject = item.getItemAsset();
        loadImage(itemAssetObject.id.toString(), viewHolder.mAssetIcon);
        viewHolder.mAssetSymbol.setText(item.getRecord().getAsset());
//        viewHolder.mAssetAmount.setText(String.format("%." + itemAssetObject.precision + "f %s", item.getRecord().getTotalAmount() / Math.pow(10, itemAssetObject.precision), item.getRecord().getAsset()));
        viewHolder.mAssetAmount.setText(item.getRecord().getTotalAmount());
        viewHolder.mAssetUpdateTime.setText(DateUtils.formatToDate(PATTERN_MM_dd_HH_mm_ss, DateUtils.formatToMillis(item.getRecord().getCreatedAt())));
        viewHolder.mAssetStatus.setText(item.getRecord().getStatus());
        viewHolder.mAssetAddress.setText(item.getRecord().getOutAddr());
        if (TextUtils.isEmpty(item.getNote())) {
            viewHolder.mAssetNote.setVisibility(View.GONE);
        } else {
            viewHolder.mAssetNote.setVisibility(View.VISIBLE);
            viewHolder.mAssetNote.setText(item.getNote());
        }

        if (item.getRecord().getType().equals(Constant.WITHDRAW)) {
            viewHolder.mInOutSymbol.setImageResource(R.drawable.ic_sent_40_px);
        } else {
            viewHolder.mInOutSymbol.setImageResource(R.drawable.ic_income_40_px);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AntiShake.check(v.getId())) { return; }
                new IntentFactory()
                        .action(item.getRecord().getLink())
                        .checkLogin(true)
                        .intent(mContext);
            }
        });
    }

    private void loadImage(String quoteId, ImageView mCoinSymbol) {
        String quoteIdWithUnderLine = quoteId.replaceAll("\\.", "_");
        Picasso.get().load("https://app.cybex.io/icons/" + quoteIdWithUnderLine + "_grey.png").into(mCoinSymbol);
    }

    private String getStateString(String state) {
        switch (state) {
            case "done":
                return mContext.getResources().getString(R.string.deposit_withdraw_state_done);
            case "failed":
                return mContext.getResources().getString(R.string.deposit_withdraw_state_failed);
            case "pending":
                return mContext.getResources().getString(R.string.deposit_withdraw_state_pending);
            case "init":
                return mContext.getResources().getString(R.string.deposit_withdraw_state_init);
            case "new":
                return mContext.getResources().getString(R.string.deposit_withdraw_state_new);
            default:
                return mContext.getResources().getString(R.string.deposit_withdraw_state_init);
        }
    }
}
