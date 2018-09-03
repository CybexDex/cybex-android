package com.cybex.eto.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybex.eto.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EmptyViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.text_empty)
    public TextView mTvEmpty;
    @BindView(R.id.iv_img)
    public ImageView mIvImage;

    public EmptyViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
