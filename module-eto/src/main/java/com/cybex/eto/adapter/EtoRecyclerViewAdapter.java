package com.cybex.eto.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybex.eto.R2;
import com.cybex.basemodule.adapter.viewholder.EmptyViewHolder;
import com.cybex.eto.R;
import com.cybex.provider.http.entity.EtoProject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EtoRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_CONTENT = 1;

    private Context mContext;
    private List<EtoProject> mEtoProjects;
    private OnItemClickListener mOnItemClickListener;

    public EtoRecyclerViewAdapter(Context context, List<EtoProject> etoProjects){
        mContext = context;
        mEtoProjects = etoProjects;
    }

    public void setData(List<EtoProject> etoProjects){
        mEtoProjects = etoProjects;
        notifyDataSetChanged();
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
        view = LayoutInflater.from(mContext).inflate(R.layout.item_eto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof EmptyViewHolder){
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.mTvEmpty.setText(mContext.getResources().getString(R.string.text_no_transfer_records));
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;
        EtoProject etoProject = mEtoProjects.get(position);
        viewHolder.mTvName.setText(etoProject.getName());
        viewHolder.mTvKeywords.setText(etoProject.getAdds_keyword());
        viewHolder.mTvStatus.setText(etoProject.getStatus());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick(mEtoProjects.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEtoProjects == null || mEtoProjects.size() == 0 ? 1 : mEtoProjects.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mEtoProjects == null || mEtoProjects.size() == 0 ? TYPE_EMPTY : TYPE_CONTENT;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.item_eto_iv_logo)
        ImageView mIvLogo;
        @BindView(R2.id.item_eto_tv_status)
        TextView mTvStatus;
        @BindView(R2.id.item_eto_tv_name)
        TextView mTvName;
        @BindView(R2.id.item_eto_tv_keywords)
        TextView mTvKeywords;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(EtoProject etoProject);
    }
}
