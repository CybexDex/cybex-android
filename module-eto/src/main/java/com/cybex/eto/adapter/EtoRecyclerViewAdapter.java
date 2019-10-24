package com.cybex.eto.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.cybex.basemodule.adapter.viewholder.EmptyViewHolder;
import com.cybex.basemodule.transform.CircleTransform;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.basemodule.utils.DateUtils;
import com.cybex.eto.R;
import com.cybex.eto.utils.PicassoImageLoader;
import com.cybex.provider.http.entity.EtoBanner;
import com.cybex.provider.http.entity.EtoProject;
import com.squareup.picasso.Picasso;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

public class EtoRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_LOADING = -1;
    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_DEFAULT = 1;
    private final static int TYPE_HEADER = 2;
    private final static int TYPE_FOOTER = 3;

    private Context mContext;
    private List<EtoProject> mEtoProjects;
    private List<EtoBanner> mEtoBanners;
    private OnItemClickListener mOnItemClickListener;

    public EtoRecyclerViewAdapter(Context context, List<EtoProject> etoProjects, List<EtoBanner> etoBanners){
        mContext = context;
        mEtoProjects = etoProjects;
        mEtoBanners = etoBanners;
    }

    public void setData(List<EtoProject> etoProjects){
        mEtoProjects = etoProjects;
        notifyItemRangeChanged(1, etoProjects.size());
    }

    public void setHeaderData(List<EtoBanner> etoBanners){
        mEtoBanners = etoBanners;
        notifyItemChanged(0);
    }

    public List<EtoProject> getData(){
        return mEtoProjects;
    }

    public void notifyProjectItem(EtoProject etoProject){
//        notifyItemChanged(mEtoProjects.indexOf(etoProject) + 1, etoProject);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if(viewType == TYPE_HEADER){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_eto_header, parent, false);
            return new HeaderViewHolder(view);
        }
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
        if(holder instanceof HeaderViewHolder){
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            if(mEtoBanners != null && mEtoBanners.size() > 0){
                headerViewHolder.mBanner.setImages(mEtoBanners);
                headerViewHolder.mBanner.start();
                headerViewHolder.mBanner.setOnBannerListener(new OnBannerListener() {
                    @Override
                    public void OnBannerClick(int position) {
                        EtoBanner etoBanner = mEtoBanners.get(position);
                        if(mEtoProjects == null || mEtoProjects.size() == 0){
                            return;
                        }
                        for(EtoProject etoProject : mEtoProjects){
                            if (etoBanner.getId() == null) {
                                continue;
                            }
                            if(!etoProject.getId().equals(etoBanner.getId())){
                                continue;
                            }
                            if(mOnItemClickListener != null){
                                mOnItemClickListener.onItemClick(etoProject);
                                break;
                            }
                        }
                    }
                });
            }
            return;
        }
        if(holder instanceof FooterViewHolder){
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;
        EtoProject etoProject = mEtoProjects.get(position - 1);
        viewHolder.mTvName.setText(etoProject.getName());
        if(Locale.getDefault().getLanguage().equals("zh")){
            viewHolder.mTvKeywords.setText(etoProject.getAdds_keyword());
            Picasso.get().load(etoProject.getAdds_logo_mobile()).transform(new CircleTransform()).into(viewHolder.mIvLogo);
        } else {
            viewHolder.mTvKeywords.setText(etoProject.getAdds_keyword__lang_en());
            Picasso.get().load(etoProject.getAdds_logo_mobile__lang_en()).transform(new CircleTransform()).into(viewHolder.mIvLogo);
        }
        String status = etoProject.getStatus();
        if(status.equals(EtoProject.Status.PRE)){
            viewHolder.mTvStatus.setText(mContext.getResources().getString(R.string.text_coming));
            viewHolder.mTvTimeLabel.setText(mContext.getResources().getString(R.string.text_start_of_distance));
            viewHolder.mTvTime.setText(parseTime((int) (DateUtils.timeDistance(System.currentTimeMillis(), DateUtils.formatToMillsETO(etoProject.getStart_at()))/1000), false));
            viewHolder.mTvStatus.setTextColor(mContext.getResources().getColor(R.color.primary_color_orange));
        } else if(status.equals(EtoProject.Status.OK)){
            viewHolder.mTvStatus.setText(mContext.getResources().getString(R.string.text_in_progress));
            viewHolder.mTvTimeLabel.setText(mContext.getResources().getString(R.string.text_end_of_distance));
            viewHolder.mTvTime.setText(parseTime((int) (DateUtils.timeDistance(System.currentTimeMillis(), DateUtils.formatToMillsETO(etoProject.getEnd_at()))/1000), false));
            viewHolder.mTvProgress.setTextColor(mContext.getResources().getColor(R.color.primary_color_orange));
            viewHolder.mPb.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.bg_progress_bar));
            viewHolder.mTvStatus.setTextColor(mContext.getResources().getColor(R.color.primary_color_orange));
        } else if(status.equals(EtoProject.Status.FINISH)){
            if (TextUtils.isEmpty(etoProject.getT_total_time()) && TextUtils.isEmpty(etoProject.getFinish_at())) {
                viewHolder.mTvStatus.setText(mContext.getResources().getString(R.string.text_ended));
                viewHolder.mTvTimeLabel.setText("");
                viewHolder.mTvTime.setText("");
                viewHolder.mIvProgress.setVisibility(View.GONE);
            } else {
                viewHolder.mTvStatus.setText(mContext.getResources().getString(R.string.text_ended));
                viewHolder.mTvTimeLabel.setText(mContext.getResources().getString(R.string.text_finish_of_distance));
                viewHolder.mTvTime.setText(parseTime(TextUtils.isEmpty(etoProject.getT_total_time()) ?
                        (int) (DateUtils.timeDistance(etoProject.getStart_at(), etoProject.getFinish_at()) / 1000) : Integer.parseInt(etoProject.getT_total_time()), true));
                viewHolder.mIvProgress.setVisibility(View.VISIBLE);
            }
            viewHolder.mTvProgress.setTextColor(mContext.getResources().getColor(R.color.font_color_white_dark));
            viewHolder.mPb.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.bg_progress_full));
            viewHolder.mTvStatus.setTextColor(mContext.getResources().getColor(R.color.font_color_white_dark));
        } else {
            viewHolder.mTvStatus.setText(mContext.getResources().getString(R.string.text_ended));
            viewHolder.mTvTimeLabel.setText(mContext.getResources().getString(R.string.text_finish_of_distance));
            viewHolder.mTvTime.setText(parseTime(TextUtils.isEmpty(etoProject.getT_total_time()) ?
                    (int) (DateUtils.timeDistance(etoProject.getStart_at(), etoProject.getFinish_at())/1000) : Integer.parseInt(etoProject.getT_total_time()), true));
            viewHolder.mTvProgress.setTextColor(mContext.getResources().getColor(R.color.font_color_white_dark));
            viewHolder.mPb.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.bg_progress_full));
            viewHolder.mTvStatus.setTextColor(mContext.getResources().getColor(R.color.font_color_white_dark));
        }
        float progress = new BigDecimal(String.valueOf(etoProject.getCurrent_percent()))
                .multiply(new BigDecimal(String.valueOf(1000))).floatValue();
        if (progress > 1000) {
            viewHolder.mPb.setProgress(1000);
            progress = 1000;
        } else {
            viewHolder.mPb.setProgress((int) progress);
        }
        viewHolder.mTvProgress.setText(String.format(Locale.US, "%s%%", AssetUtil.formatNumberRounding(progress / 10, 2, RoundingMode.DOWN)));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick(mEtoProjects.get(position - 1));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEtoProjects == null || mEtoProjects.size() == 0 ? 2 : mEtoProjects.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return TYPE_HEADER;
        }
        return mEtoProjects == null || mEtoProjects.size() == 0 ? TYPE_EMPTY : TYPE_DEFAULT;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mIvLogo;
        TextView mTvStatus;
        TextView mTvName;
        TextView mTvKeywords;
        TextView mTvTimeLabel;
        TextView mTvTime;
        ProgressBar mPb;
        TextView mTvProgress;
        ImageView mIvProgress;

        public ViewHolder(View itemView) {
            super(itemView);
            mIvLogo = itemView.findViewById(R.id.item_eto_iv_logo);
            mTvStatus = itemView.findViewById(R.id.item_eto_tv_status);
            mTvName = itemView.findViewById(R.id.item_eto_tv_name);
            mTvKeywords = itemView.findViewById(R.id.item_eto_tv_keywords);
            mTvTimeLabel = itemView.findViewById(R.id.item_eto_tv_time_label);
            mTvTime = itemView.findViewById(R.id.item_eto_tv_time);
            mPb = itemView.findViewById(R.id.item_eto_pb);
            mTvProgress = itemView.findViewById(R.id.item_eto_tv_progress);
            mIvProgress = itemView.findViewById(R.id.item_eto_iv_progress);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private Banner mBanner;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            mBanner = itemView.findViewById(R.id.eto_banner);
            mBanner.isAutoPlay(true);
            mBanner.setDelayTime(3000);
            mBanner.setImageLoader(new PicassoImageLoader());
            mBanner.setIndicatorGravity(BannerConfig.CENTER);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(EtoProject etoProject);
    }

    /**
     * 结束精确到秒
     * @param time
     * @param isFinish
     * @return
     */
    private String parseTime(int time, boolean isFinish){
        StringBuffer sb = new StringBuffer();
        if(time <= 0){
            sb.append(0).append(mContext.getResources().getString(R.string.text_day)).append(" ");
            sb.append(0).append(mContext.getResources().getString(R.string.text_hours)).append(" ");
            sb.append(0).append(mContext.getResources().getString(R.string.text_minutes)).append(" ");
            if(isFinish){
                sb.append(0).append(mContext.getResources().getString(R.string.text_seconds)).append(" ");
            }
            return sb.toString();
        }
        if(isFinish || time >= DateUtils.MINUTE_IN_SECOND){
            int day = time / DateUtils.DAY_IN_SECOND;
            sb.append(day).append(mContext.getResources().getString(R.string.text_day)).append(" ");
            int hours = (time % DateUtils.DAY_IN_SECOND) / DateUtils.HOUR_IN_SECOND;
            sb.append(hours).append(mContext.getResources().getString(R.string.text_hours)).append(" ");
            int minutes = ((time % DateUtils.DAY_IN_SECOND) % DateUtils.HOUR_IN_SECOND) / DateUtils.MINUTE_IN_SECOND;
            sb.append(minutes).append(mContext.getResources().getString(R.string.text_minutes)).append(" ");
            if(isFinish){
                int seconds = ((time % DateUtils.DAY_IN_SECOND) % DateUtils.HOUR_IN_SECOND) % DateUtils.MINUTE_IN_SECOND;
                sb.append(seconds).append(mContext.getResources().getString(R.string.text_seconds)).append(" ");
            }
        } else {
            sb.append(mContext.getResources().getString(R.string.text_less_than_minute));
        }
        return sb.toString();
    }
}
