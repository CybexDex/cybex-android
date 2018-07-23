package com.cybexmobile.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.activity.transfer.TransferRecordsActivity;
import com.cybexmobile.adapter.viewholder.EmptyViewHolder;

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

    public TransferRecordsRecyclerViewAdapter(Context context, List<TransferRecordsActivity.TransferHistoryItem> transferHistoryItems){
        mContext = context;
        mTransferRecords = transferHistoryItems;
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
        viewHolder.mTvAccountName.setText(transferHistoryItem.transferOperation.from.toString());
        viewHolder.mTvAmount.setText(transferHistoryItem.transferOperation.amount.amount + "");
        viewHolder.mTvDate.setText("");
        viewHolder.mTvStatus.setText("");
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick();
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

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnItemClickListener{
        void onItemClick();
    }
}
