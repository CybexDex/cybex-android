package com.cybexmobile.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cybexmobile.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EmptyViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.text_empty)
    public TextView mTvEmpty;

    public EmptyViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
