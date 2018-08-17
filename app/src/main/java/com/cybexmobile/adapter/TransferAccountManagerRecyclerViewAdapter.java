package com.cybexmobile.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybex.database.entity.Address;
import com.cybexmobile.R;
import com.cybexmobile.activity.transfer.TransferRecordsActivity;
import com.cybexmobile.adapter.viewholder.EmptyViewHolder;
import com.cybexmobile.graphene.chain.AccountObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.Operations;
import com.cybexmobile.utils.AssetUtil;
import com.cybexmobile.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransferAccountManagerRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_CONTENT = 1;

    private Context mContext;
    private List<Address> mTransferAddresses = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    public TransferAccountManagerRecyclerViewAdapter(Context context, List<Address> addresses){
        mContext = context;
        mTransferAddresses = addresses;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mOnItemClickListener = listener;
    }

    public void setAddresses(List<Address> addresses){
        mTransferAddresses = addresses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if(viewType == TYPE_EMPTY){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_empty, parent, false);
            return new EmptyViewHolder(view);
        }
        view = LayoutInflater.from(mContext).inflate(R.layout.item_transfer_account_manager, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof EmptyViewHolder){
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.mTvEmpty.setText(mContext.getResources().getString(R.string.text_no_transfer_addresses));
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;
        Address address = mTransferAddresses.get(position);
        viewHolder.mTvAddress.setText(address.getAddress());
        viewHolder.mTvLabel.setText(address.getLabel());
        if (mTransferAddresses.get(position).getMemo() != null) {
            viewHolder.mTvMemo.setVisibility(View.VISIBLE);
            viewHolder.mTvMemo.setText(mTransferAddresses.get(position).getMemo());
        } else {
            viewHolder.mTvMemo.setVisibility(View.GONE);
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick(address);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTransferAddresses == null || mTransferAddresses.size() == 0 ? 1 : mTransferAddresses.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mTransferAddresses == null || mTransferAddresses.size() == 0 ? TYPE_EMPTY : TYPE_CONTENT;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_transfer_account_manager_tv_label)
        TextView mTvLabel;
        @BindView(R.id.item_transfer_account_manager_tv_address)
        TextView mTvAddress;
        @BindView(R.id.item_transfer_account_manager_tv_memo)
        TextView mTvMemo;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(Address address);
    }
}
