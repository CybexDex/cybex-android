package com.cybex.basemodule.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybex.basemodule.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EmptyViewHolder extends RecyclerView.ViewHolder {

    @BindView(R2.id.text_empty)
    public TextView mTvEmpty;
    @BindView(R2.id.iv_img)
    public ImageView mIvImage;

    public EmptyViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
