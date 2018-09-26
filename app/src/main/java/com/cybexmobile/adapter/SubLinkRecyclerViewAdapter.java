package com.cybexmobile.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.http.entity.SubLink;
import com.cybex.provider.market.Order;
import com.cybexmobile.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubLinkRecyclerViewAdapter extends RecyclerView.Adapter<SubLinkRecyclerViewAdapter.ViewHolder> {

    private Context mContext;
    private List<SubLink> mSubLinks;
    private OnItemClickListener mListener;

    public SubLinkRecyclerViewAdapter(Context context, List<SubLink> subLinks) {
        mContext = context;
        mSubLinks = subLinks;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sub_link, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        SubLink subLink = mSubLinks.get(position);
        holder.mTitle.setText(subLink.getTitle());
        holder.mDescription.setText(subLink.getDesc());
        Picasso.get().load(subLink.getIcon()).into(holder.mIvIcon);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onItemClick(subLink);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSubLinks.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_sub_link_tv_title)
        TextView mTitle;
        @BindView(R.id.item_sub_link_tv_description)
        TextView mDescription;
        @BindView(R.id.item_sub_link_iv_icon)
        ImageView mIvIcon;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(SubLink subLink);
    }
}
