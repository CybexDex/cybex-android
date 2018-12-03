package com.cybexmobile.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybexmobile.R;
import com.cybexmobile.activity.gateway.deposit.DepositActivity;
import com.cybexmobile.activity.gateway.withdraw.WithdrawActivity;
import com.cybexmobile.activity.address.WithdrawAddressManageListActivity;
import com.cybexmobile.activity.address.WithdrawAddressManagerActivity;
import com.cybexmobile.data.item.OpenOrderItem;
import com.cybexmobile.faucet.DepositAndWithdrawObject;
import com.cybexmobile.fragment.DepositItemFragment;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.utils.MyUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DepositAndWithdrawAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_CONTENT = 1;
    private Activity mContext;
    private String mName;
    private List<DepositAndWithdrawObject> mDataList;
    private List<DepositAndWithdrawObject> mOriginDataList;
    private BalanceFilter mFilter;


    public DepositAndWithdrawAdapter(Activity context, String name, List<DepositAndWithdrawObject> depositAndWithdrawObjectList) {
        mContext = context;
        mName = name;
        mDataList = depositAndWithdrawObjectList;
        mOriginDataList = depositAndWithdrawObjectList;
    }

    public void setDepositAndWithdrawItems(List<DepositAndWithdrawObject> depositAndWithdrawItems) {
        mOriginDataList = depositAndWithdrawItems;
        mDataList = depositAndWithdrawItems;
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
        AssetObject assetObject = mDataList.get(position).getAssetObject();
        AccountBalanceObject accountBalanceObject = mDataList.get(position).getAccountBalanceObject();
        if (assetObject != null) {
            if (mName.equals(WithdrawAddressManagerActivity.class.getName())) {
                holder.mAssetName.setText(MyUtils.removeJadePrefix(assetObject.symbol));
                loadImage(mDataList.get(position).getId(), holder.mAssetIcon);
                holder.mAssetPrice.setText(String.valueOf(mDataList.get(position).getCount()));
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, WithdrawAddressManageListActivity.class);
                        intent.putExtra("assetName", MyUtils.removeJadePrefix(assetObject.symbol));
                        intent.putExtra("assetId", mDataList.get(position).getId());
                        mContext.startActivity(intent);
                    }
                });
            } else {
                holder.mAssetName.setText(MyUtils.removeJadePrefix(assetObject.symbol));
                loadImage(mDataList.get(position).getId(), holder.mAssetIcon);

                if (mDataList.get(position).isEnable()) {
                    holder.mAssetPrice.setText("");
                } else {
                    holder.mAssetPrice.setText(mContext.getResources().getString(R.string.gate_way_suspended));
                }
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mName.equals(DepositItemFragment.class.getName())) {
                            if (mDataList.get(position).isEnable()) {
                                Intent intent = new Intent(mContext, DepositActivity.class);
                                intent.putExtra("assetName", MyUtils.removeJadePrefix(assetObject.symbol));
                                intent.putExtra("assetId", mDataList.get(position).getId());
                                intent.putExtra("isEnabled", mDataList.get(position).isEnable());
                                intent.putExtra("enMsg", mDataList.get(position).getEnMsg());
                                intent.putExtra("cnMsg", mDataList.get(position).getCnMsg());
                                intent.putExtra("enInfo", mDataList.get(position).getEnInfo());
                                intent.putExtra("cnInfo", mDataList.get(position).getCnInfo());
                                intent.putExtra("assetObject", mDataList.get(position).getAssetObject());
                                mContext.startActivity(intent);
                            } else {
                                if (!mDataList.get(position).getCnMsg().equals("") && !mDataList.get(position).getEnMsg().equals("")) {
                                    if (Locale.getDefault().getLanguage().equals("zh")) {
                                        ToastMessage.showDepositWithdrawToastMessage(mContext, mDataList.get(position).getCnMsg());
                                    } else {
                                        ToastMessage.showDepositWithdrawToastMessage(mContext, mDataList.get(position).getEnMsg());
                                    }
                                }
                            }
                        } else {
                            if (mDataList.get(position).isEnable()) {
                                Intent intent = new Intent(mContext, WithdrawActivity.class);
                                intent.putExtra("assetName", MyUtils.removeJadePrefix(assetObject.symbol));
                                intent.putExtra("assetId", mDataList.get(position).getId());
                                intent.putExtra("isEnabled", mDataList.get(position).isEnable());
                                intent.putExtra("enMsg", mDataList.get(position).getEnMsg());
                                intent.putExtra("cnMsg", mDataList.get(position).getCnMsg());
                                intent.putExtra("enInfo", mDataList.get(position).getEnInfo());
                                intent.putExtra("cnInfo", mDataList.get(position).getCnInfo());
                                intent.putExtra("assetObject", mDataList.get(position).getAssetObject());
                                if (accountBalanceObject != null) {
                                    intent.putExtra("availableAmount", accountBalanceObject.balance / Math.pow(10, assetObject.precision));
                                }
                                mContext.startActivity(intent);
                            } else {
                                if (!mDataList.get(position).getCnMsg().equals("") && !mDataList.get(position).getEnMsg().equals("")) {
                                    if (Locale.getDefault().getLanguage().equals("zh")) {
                                        ToastMessage.showDepositWithdrawToastMessage(mContext, mDataList.get(position).getCnMsg());
                                    } else {
                                        ToastMessage.showDepositWithdrawToastMessage(mContext, mDataList.get(position).getEnMsg());
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
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
}
