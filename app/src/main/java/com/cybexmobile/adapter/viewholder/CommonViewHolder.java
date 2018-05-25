package com.cybexmobile.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CommonViewHolder extends RecyclerView.ViewHolder {

    private SparseArray<View> mViews;

    public CommonViewHolder(View itemView) {
        super(itemView);
        mViews = new SparseArray<>();
    }

    public <T extends View> T getView(int viewId){
        View view = mViews.get(viewId);
        if(view == null){
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    public void setText(int viewId, CharSequence text){
        ((TextView)getView(viewId)).setText(text);
    }

    public void setVisibility(int viewId, int visibility){
        getView(viewId).setVisibility(visibility);
    }

    public void setProgress(int viewId, int progress){
        ((ProgressBar)getView(viewId)).setProgress(progress);
    }


}
