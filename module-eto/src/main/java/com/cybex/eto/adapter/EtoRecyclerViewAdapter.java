package com.cybex.eto.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybex.eto.R2;
import com.cybex.basemodule.adapter.viewholder.EmptyViewHolder;
import com.cybex.basemodule.utils.DateUtils;
import com.cybex.eto.R;
import com.cybex.provider.http.entity.EtoProject;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EtoRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("");

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
            emptyViewHolder.mTvEmpty.setText(mContext.getResources().getString(R.string.text_no_eto));
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;
        EtoProject etoProject = mEtoProjects.get(position);
        viewHolder.mTvName.setText(etoProject.getName());
        if(Locale.getDefault().getLanguage().equals("zh")){
            viewHolder.mTvKeywords.setText(etoProject.getAdds_keyword());
            Picasso.get().load(etoProject.getAdds_logo()).into(viewHolder.mIvLogo);
        } else {
            viewHolder.mTvKeywords.setText(etoProject.getAdds_keyword__lang_en());
            Picasso.get().load(etoProject.getAdds_logo__lang_en()).into(viewHolder.mIvLogo);
        }
        String status = etoProject.getStatus();
        if(status.equals(EtoProject.Status.PRE)){
            viewHolder.mTvStatus.setText(mContext.getResources().getString(R.string.text_coming));
            viewHolder.mTvTimeLabel.setText(mContext.getResources().getString(R.string.text_start_of_distance));
            viewHolder.mTvTime.setText(parseTime((int) (DateUtils.timeDistance(System.currentTimeMillis(), etoProject.getStart_at())/1000)));
        } else if(status.equals(EtoProject.Status.OK)){
            viewHolder.mTvStatus.setText(mContext.getResources().getString(R.string.text_in_progress));
            viewHolder.mTvTimeLabel.setText(mContext.getResources().getString(R.string.text_end_of_distance));
            viewHolder.mTvTime.setText(parseTime((int) (DateUtils.timeDistance(System.currentTimeMillis(), etoProject.getEnd_at())/1000)));
        } else if(status.equals(EtoProject.Status.FINISH)){
            viewHolder.mTvStatus.setText(mContext.getResources().getString(R.string.text_ended));
            viewHolder.mTvTimeLabel.setText(mContext.getResources().getString(R.string.text_finish_of_distance));
            viewHolder.mTvTime.setText(parseTime((int) (DateUtils.timeDistance(etoProject.getStart_at(), etoProject.getFinish_at())/1000)));
        } else {
            viewHolder.mTvStatus.setText(mContext.getResources().getString(R.string.text_ended));
            viewHolder.mTvTimeLabel.setText(mContext.getResources().getString(R.string.text_finish_of_distance));
            viewHolder.mTvTime.setText(parseTime((int) (DateUtils.timeDistance(etoProject.getStart_at(), etoProject.getFinish_at())/1000)));
        }
        viewHolder.mPb.setProgress((int)(etoProject.getCurrent_percent() * 100));
        viewHolder.mTvProgress.setText(String.format("%s%%", etoProject.getCurrent_percent() * 100));
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
        @BindView(R2.id.item_eto_tv_time_label)
        TextView mTvTimeLabel;
        @BindView(R2.id.item_eto_tv_time)
        TextView mTvTime;
        @BindView(R2.id.item_eto_pb)
        ProgressBar mPb;
        @BindView(R2.id.item_eto_tv_progress)
        TextView mTvProgress;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(EtoProject etoProject);
    }

    private String parseTime(int time){
        if(time <= 0){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        int day = time / DateUtils.DAY_IN_SECOND;
        if(day > 0){
            sb.append(day).append(mContext.getResources().getString(R.string.text_day));
        }
        int hours = (time % DateUtils.DAY_IN_SECOND) / DateUtils.HOUR_IN_SECOND;
        if(hours > 0){
            sb.append(hours).append(mContext.getResources().getString(R.string.text_hours));
        }
        int minutes = ((time % DateUtils.DAY_IN_SECOND) % DateUtils.HOUR_IN_SECOND) / DateUtils.MINUTE_IN_SECOND;
        if(minutes > 0){
            sb.append(minutes).append(mContext.getResources().getString(R.string.text_minutes));
        }
        int seconds = ((time % DateUtils.DAY_IN_SECOND) % DateUtils.HOUR_IN_SECOND) % DateUtils.MINUTE_IN_SECOND;
        if(seconds > 0){
            sb.append(seconds).append(mContext.getResources().getString(R.string.text_seconds));
        }
        return sb.toString();
    }
}
