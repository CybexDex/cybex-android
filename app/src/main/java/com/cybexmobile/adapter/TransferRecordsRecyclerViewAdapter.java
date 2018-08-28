package com.cybexmobile.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybex.provider.db.entity.Address;
import com.cybexmobile.R;
import com.cybexmobile.activity.transfer.TransferRecordsActivity;
import com.cybexmobile.adapter.viewholder.EmptyViewHolder;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.Operations;
import com.cybexmobile.utils.AssetUtil;
import com.cybexmobile.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransferRecordsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_CONTENT = 1;

    private Context mContext;
    private List<TransferRecordsActivity.TransferHistoryItem> mTransferRecords = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    private AccountObject mAccountObject;

    public TransferRecordsRecyclerViewAdapter(Context context, AccountObject accountObject, List<TransferRecordsActivity.TransferHistoryItem> transferHistoryItems){
        mContext = context;
        mTransferRecords = transferHistoryItems;
        mAccountObject = accountObject;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if(viewType == TYPE_EMPTY){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_empty, parent, false);
            return new EmptyViewHolder(view);
        }
        view = LayoutInflater.from(mContext).inflate(R.layout.item_transfer_records, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof EmptyViewHolder){
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.mTvEmpty.setText(mContext.getResources().getString(R.string.text_no_transfer_records));
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;
        TransferRecordsActivity.TransferHistoryItem transferHistoryItem = mTransferRecords.get(position);
        Operations.transfer_operation transferOperation = transferHistoryItem.transferOperation;
        AccountObject fromAccount = transferHistoryItem.fromAccount;
        AccountObject toAccount = transferHistoryItem.toAccount;
        AssetObject transferAsset = transferHistoryItem.transferAsset;
        Address address = transferHistoryItem.address;
        /**
         * fix bug：CYM-518
         * 解决转入转出状态错误
         */
        if(fromAccount != null && toAccount != null && mAccountObject != null){
            if(toAccount.id.equals(mAccountObject.id)){
                viewHolder.mTvAccountName.setText(address == null ? fromAccount.name : address.getNote());
                viewHolder.mTvAmount.setTextColor(mContext.getResources().getColor(R.color.primary_color_orange));
                viewHolder.mTvStatus.setText(mContext.getResources().getString(R.string.text_received));
                viewHolder.mIvTransferAction.setImageResource(R.drawable.ic_transfer_in);
                if(transferAsset != null){
                    viewHolder.mTvAmount.setText(String.format("+%s %s",
                            AssetUtil.formatNumberRounding( transferOperation.amount.amount / Math.pow(10, transferAsset.precision), transferAsset.precision),
                            AssetUtil.parseSymbol(transferAsset.symbol)));
                }
            } else if(fromAccount.id.equals(mAccountObject.id)){
                viewHolder.mTvAccountName.setText(address == null ? toAccount.name : address.getNote());
                viewHolder.mTvAmount.setTextColor(mContext.getResources().getColor(R.color.font_color_white_dark));
                viewHolder.mTvStatus.setText(mContext.getResources().getString(R.string.text_sent));
                viewHolder.mIvTransferAction.setImageResource(R.drawable.ic_transfer_out);
                if(transferAsset != null){
                    viewHolder.mTvAmount.setText(String.format("-%s %s",
                            AssetUtil.formatNumberRounding( transferOperation.amount.amount / Math.pow(10, transferAsset.precision), transferAsset.precision),
                            AssetUtil.parseSymbol(transferAsset.symbol)));
                }
            }
        }
        if(transferHistoryItem.block != null){
            viewHolder.mTvDate.setText(DateUtils.formatToDate(DateUtils.PATTERN_MM_dd_HH_mm_ss, DateUtils.formatToMillis(transferHistoryItem.block.timestamp)));
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick(transferHistoryItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTransferRecords == null || mTransferRecords.size() == 0 ? 1 : mTransferRecords.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mTransferRecords == null || mTransferRecords.size() == 0 ? TYPE_EMPTY : TYPE_CONTENT;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_transfer_records_tv_account_name)
        TextView mTvAccountName;
        @BindView(R.id.item_transfer_records_tv_amount)
        TextView mTvAmount;
        @BindView(R.id.item_transfer_records_tv_date)
        TextView mTvDate;
        @BindView(R.id.item_transfer_records_tv_status)
        TextView mTvStatus;
        @BindView(R.id.item_transfer_records_iv_transfer_action)
        ImageView mIvTransferAction;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(TransferRecordsActivity.TransferHistoryItem transferHistoryItem);
    }
}
