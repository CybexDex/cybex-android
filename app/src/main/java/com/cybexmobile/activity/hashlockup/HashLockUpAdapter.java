package com.cybexmobile.activity.hashlockup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybex.basemodule.adapter.viewholder.EmptyViewHolder;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.basemodule.utils.DateUtils;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.HtlcAdapterItemObject;
import com.cybexmobile.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.cybex.basemodule.utils.DateUtils.PATTERN_MM_dd_HH_mm_ss;

public class HashLockUpAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_CONTENT = 1;

    private List<HtlcAdapterItemObject> mItems;

    Context mContext;


    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_hash_lock_up_asset_icon)
        ImageView mAssetIcon;
        @BindView(R.id.item_hash_lock_up_asset_symbol)
        TextView mAssetSymbol;
        @BindView(R.id.item_hash_lock_up_asset_amount)
        TextView mAssetAmount;
        @BindView(R.id.item_hash_lock_up_initiator)
        TextView mInitiator;
        @BindView(R.id.item_hash_lock_up_receiver)
        TextView mReceiver;
        @BindView(R.id.item_hash_lock_up_end_time)
        TextView mEndTime;
        @BindView(R.id.item_hash_lock_up_hash_algorithm)
        TextView mHashAlgorithm;
        @BindView(R.id.item_hash_lock_up_hash)
        TextView mHash;
        @BindView(R.id.item_lock_up_asset_expand_layout)
        LinearLayout mExpandLayout;
        @BindView(R.id.item_hash_lock_up_expand_icon)
        ImageView mExpandIcon;
        @BindView(R.id.item_hash_lock_up_expand_icon_up)
        ImageView mExpandIconUp;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public HashLockUpAdapter(List<HtlcAdapterItemObject> items, Context context) {
        mContext = context;
        mItems = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_EMPTY) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_empty, parent, false);
            return new EmptyViewHolder(view);
        }
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hash_lock_up_item, parent, false);
        return new HashLockUpAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.mTvEmpty.setText(mContext.getResources().getString(R.string.deposit_withdraw_records_no_record));
            emptyViewHolder.mIvImage.setImageResource(R.drawable.ic_no_records);
            return;
        }

        HashLockUpAdapter.ViewHolder viewHolder = (HashLockUpAdapter.ViewHolder) holder;
        HtlcAdapterItemObject item = mItems.get(position);
        AssetObject itemAssetObject = item.getAssetObject();
        loadImage(itemAssetObject.id.toString(), viewHolder.mAssetIcon);
        viewHolder.mAssetSymbol.setText(AssetUtil.parseSymbol(itemAssetObject.symbol));
        viewHolder.mAssetAmount.setText(String.format("%." + itemAssetObject.precision + "f %s", item.getHtlcObject().transfer.amount / Math.pow(10, itemAssetObject.precision), AssetUtil.parseSymbol(itemAssetObject.symbol)));
        viewHolder.mEndTime.setText(DateUtils.formatToDate(PATTERN_MM_dd_HH_mm_ss, DateUtils.formatToMillis(item.getHtlcObject().conditions.time_lock.expiration)));
        viewHolder.mHash.setText((String) item.getHtlcObject().conditions.hash_lock.preimage_hash.get(1));
        viewHolder.mInitiator.setText(item.getFrom());
        viewHolder.mReceiver.setText(item.getTo());
        viewHolder.mHashAlgorithm.setText(getAlgorithm((double) item.getHtlcObject().conditions.hash_lock.preimage_hash.get(0)));
        viewHolder.mExpandIcon.setOnClickListener(
                v -> {
                    v.setVisibility(View.GONE);
                    viewHolder.mExpandLayout.setVisibility(View.VISIBLE);
                }
        );
        viewHolder.mExpandIconUp.setOnClickListener(
                v -> {
                    viewHolder.mExpandIcon.setVisibility(View.VISIBLE);
                    viewHolder.mExpandLayout.setVisibility(View.GONE);
                }
        );

    }

    @Override
    public int getItemViewType(int position) {
        return mItems == null || mItems.size() == 0 ? TYPE_EMPTY : TYPE_CONTENT;
    }

    @Override
    public int getItemCount() {
        return mItems == null || mItems.size() == 0 ? 1 : mItems.size();
    }

    private void loadImage(String quoteId, ImageView mCoinSymbol) {
        String quoteIdWithUnderLine = quoteId.replaceAll("\\.", "_");
        Picasso.get().load("https://app.cybex.io/icons/" + quoteIdWithUnderLine + "_grey.png").into(mCoinSymbol);
    }

    private String getAlgorithm(double number) {
        switch ((int) number) {
            case 0:
                return "Ripemd160";
            case 2:
                return "sha256";
            default:
                return "--";
        }
    }
}
