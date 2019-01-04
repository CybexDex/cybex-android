package com.cybexmobile.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybex.basemodule.adapter.viewholder.EmptyViewHolder;
import com.cybexmobile.R;
import com.cybexmobile.activity.address.WithdrawAddressManagerActivity;
import com.cybexmobile.faucet.DepositAndWithdrawObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.utils.MyUtils;
import com.cybexmobile.shake.AntiShake;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DepositAndWithdrawAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_CONTENT = 1;
    private Context mContext;
    private String mName;
    private List<DepositAndWithdrawObject> mDataList;
    private List<DepositAndWithdrawObject> mOriginDataList;
    private BalanceFilter mFilter;
    private OnItemClickListener mOnItemClickListener;

    public DepositAndWithdrawAdapter(Context context, String name, List<DepositAndWithdrawObject> depositAndWithdrawObjectList) {
        mContext = context;
        mName = name;
        mDataList = depositAndWithdrawObjectList;
        mOriginDataList = depositAndWithdrawObjectList;
    }

    public void setDepositAndWithdrawItems(List<DepositAndWithdrawObject> depositAndWithdrawItems) {
        mOriginDataList = depositAndWithdrawItems;
        mDataList = depositAndWithdrawItems;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mAssetName;
        TextView mAssetFullName;
        TextView mAssetPrice;
        ImageView mAssetIcon;
        ImageView mAssetArrow;
        View mView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mAssetName = view.findViewById(R.id.gate_way_asset_name);
            mAssetFullName = view.findViewById(R.id.gate_way_asset_full_name);
            mAssetPrice = view.findViewById(R.id.gate_way_asset_price);
            mAssetIcon = view.findViewById(R.id.gate_way_asset_icon);
            mAssetArrow = view.findViewById(R.id.gate_way_asset_arrow);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) viewHolder;
            emptyViewHolder.mTvEmpty.setText(mContext.getResources().getString(R.string.deposit_withdraw_records_no_record));
            emptyViewHolder.mIvImage.setImageResource(R.drawable.ic_no_records);
            return;
        }
        ViewHolder holder = (ViewHolder) viewHolder;
        final DepositAndWithdrawObject depositAndWithdrawObject = mDataList.get(position);
        final AssetObject assetObject = depositAndWithdrawObject.getAssetObject();
        if (assetObject == null) {
            return;
        }
        if (mName.equals(WithdrawAddressManagerActivity.class.getName())) {
            holder.mAssetName.setText(MyUtils.removeJadePrefix(assetObject.symbol));
            holder.mAssetPrice.setText(String.valueOf(depositAndWithdrawObject.getCount()));
            loadImage(mDataList.get(position).getId(), holder.mAssetIcon);
        } else {
            holder.mAssetName.setText(MyUtils.removeJadePrefix(assetObject.symbol));
            holder.mAssetFullName.setText(String.format(" (%s)", depositAndWithdrawObject.getProjectName()));
            holder.mAssetPrice.setText(depositAndWithdrawObject.isEnable() ? "" : mContext.getResources().getString(R.string.gate_way_suspended));
            loadImage(depositAndWithdrawObject.getId(), holder.mAssetIcon);
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AntiShake.check(v.getId())) { return; }
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(depositAndWithdrawObject);
                }
            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_EMPTY) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_empty, parent, false);
            return new EmptyViewHolder(view);
        }
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deposit_withdraw, parent, false);
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
        return mDataList == null || mDataList.size() == 0 ? 1 : mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mDataList == null || mDataList.size() == 0 ? TYPE_EMPTY : TYPE_CONTENT;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new BalanceFilter();
        }
        return mFilter;
    }

    class BalanceFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterStr = constraint.toString();
            FilterResults results = new FilterResults();
            if (TextUtils.isEmpty(constraint)) {
                results.values = mOriginDataList;
                results.count = mOriginDataList.size();
                return results;
            }
            List<DepositAndWithdrawObject> filterDataList = new ArrayList<>();
            for (DepositAndWithdrawObject data : mOriginDataList) {
                if (AssetUtil.parseSymbol(data.assetObject.symbol.toLowerCase()).contains(filterStr.toLowerCase())) {
                    filterDataList.add(data);
                }
            }
            results.values = filterDataList;
            results.count = filterDataList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mDataList = (List<DepositAndWithdrawObject>)results.values;
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DepositAndWithdrawObject depositAndWithdrawObject);
    }
}
