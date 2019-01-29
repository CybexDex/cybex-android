package com.cybexmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybex.provider.db.entity.Address;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.Types;
import com.cybexmobile.R;
import com.cybex.basemodule.adapter.viewholder.EmptyViewHolder;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybex.basemodule.utils.AssetUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommonSelectRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_CONTENT = 1;

    private List<T> mItems;
    private T mSelectedItem;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public CommonSelectRecyclerViewAdapter(Context context,
                                          T selectedItem,
                                          List<T> items) {
        mItems = items;
        mSelectedItem = selectedItem;
        mContext = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mOnItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if(viewType == TYPE_EMPTY){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_empty, parent, false);
            return new EmptyViewHolder(view);
        }
        view = LayoutInflater.from(mContext).inflate(R.layout.item_asset_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof EmptyViewHolder){
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.mTvEmpty.setText(mContext.getResources().getString(R.string.balance_page_no_asset));
            emptyViewHolder.mIvImage.setImageResource(R.drawable.ic_no_assert);
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;
        T item = mItems.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedItem = item;
                notifyDataSetChanged();
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick(item);
                }
            }
        });
        viewHolder.mTvSymbol.setSelected(item == mSelectedItem);
        if(item instanceof AccountBalanceObjectItem){
            AccountBalanceObjectItem accountBalanceObjectItem = (AccountBalanceObjectItem) item;
            AssetObject assetObject = accountBalanceObjectItem.assetObject;
            if (assetObject != null) {
                viewHolder.mTvSymbol.setText(AssetUtil.parseSymbol(assetObject.symbol));
            }
        } else if(item instanceof Address){
            Address address = (Address) item;
            viewHolder.mTvSymbol.setText(address.getNote());
        } else if (item instanceof Types.public_key_type) {
            Types.public_key_type public_key_type = (Types.public_key_type) item;
            viewHolder.mTvSymbol.setText(public_key_type.toString());
        }

    }

    @Override
    public int getItemCount() {
        return mItems == null || mItems.size() == 0 ? 1 : mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems == null || mItems.size() == 0 ? TYPE_EMPTY : TYPE_CONTENT;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_asset_select_tv_asset_symbol)
        TextView mTvSymbol;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public interface OnItemClickListener<T>{
        void onItemClick(T item);
    }
}
