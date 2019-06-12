package com.cybex.eto.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybex.basemodule.adapter.viewholder.EmptyViewHolder;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.basemodule.utils.DateUtils;
import com.cybex.eto.R;
import com.cybex.eto.R2;
import com.cybex.provider.graphene.chain.Asset;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.entity.EtoRecord;
import com.cybex.provider.http.entity.NewEtoRecord;
import com.squareup.picasso.Picasso;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.cybex.basemodule.utils.DateUtils.PATTERN_MM_dd_HH_mm;
import static com.cybex.basemodule.utils.DateUtils.PATTERN_yyyy_MM_dd_HH_mm_ss;

public class EtoRecordsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_EMPTY = 0;
    private final static int TYPE_CONTENT = 1;

    private Context mContext;
    private List<NewEtoRecord> mEtoRecords;

    public EtoRecordsRecyclerViewAdapter(Context context, List<NewEtoRecord> etoRecords){
        mContext = context;
        mEtoRecords = etoRecords;
    }

    public void setData(List<NewEtoRecord> etoRecords){
        mEtoRecords = etoRecords;
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
        view = LayoutInflater.from(mContext).inflate(R.layout.item_eto_records, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof EmptyViewHolder){
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.mTvEmpty.setText(mContext.getResources().getString(R.string.text_no_record));
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;
        NewEtoRecord etoRecord = mEtoRecords.get(position);
        viewHolder.mTvEtoName.setText(etoRecord.getExchangeName());
        viewHolder.mTvJoinAmount.setText(String.format("%s%s", AssetUtil.fmt(etoRecord.getPayAmount() / Math.pow(10, etoRecord.getPayAssetObject().precision)), AssetUtil.parseSymbol(etoRecord.getPayAssetObject().symbol)));
        viewHolder.mTvReceiveAmount.setText(String.format("%s%s", AssetUtil.fmt(etoRecord.getReceiveAmount() / Math.pow(10, etoRecord.getReceiveAssetObject().precision)), AssetUtil.parseSymbol(etoRecord.getReceiveAssetObject().symbol)));
        viewHolder.mTvTime.setText(DateUtils.formatToDate(PATTERN_MM_dd_HH_mm, DateUtils.formatToMillis(etoRecord.getOccurence())));
//        if(TextUtils.isEmpty(etoRecord.getReason())){
//            viewHolder.mTvEtoOperateResult.setText(mContext.getResources().getString(R.string.text_received_success));
//            viewHolder.mTvEtoOperateResult.setTextColor(mContext.getResources().getColor(R.color.primary_color_orange));
//        } else {
//            switch (etoRecord.getReason()) {
//                case EtoRecord.Reason.REASON_1:
//                case EtoRecord.Reason.REASON_2:
//                case EtoRecord.Reason.REASON_3:
//                case EtoRecord.Reason.REASON_4:
//                case EtoRecord.Reason.REASON_5:
//                case EtoRecord.Reason.REASON_6:
//                case EtoRecord.Reason.REASON_7:
//                case EtoRecord.Reason.REASON_8:
//                case EtoRecord.Reason.REASON_9:
//                case EtoRecord.Reason.REASON_10:
//                case EtoRecord.Reason.REASON_11:
//                case EtoRecord.Reason.REASON_15:
//                    viewHolder.mTvEtoOperateResult.setText(mContext.getResources().getString(R.string.text_invalid_subscription));
//                    viewHolder.mTvEtoOperateResult.setTextColor(mContext.getResources().getColor(R.color.font_color_white_dark));
//                    break;
//                case EtoRecord.Reason.REASON_12:
//                case EtoRecord.Reason.REASON_13:
//                case EtoRecord.Reason.REASON_14:
//                    viewHolder.mTvEtoOperateResult.setText(mContext.getResources().getString(R.string.text_subscription_partly_valid));
//                    viewHolder.mTvEtoOperateResult.setTextColor(mContext.getResources().getColor(R.color.primary_color_orange));
//                    break;
//                case EtoRecord.Reason.REASON_101:
//                    viewHolder.mTvEtoOperateResult.setText(mContext.getResources().getString(R.string.text_refund));
//                    viewHolder.mTvEtoOperateResult.setTextColor(mContext.getResources().getColor(R.color.font_color_white_dark));
//                    break;
//            }
//        }

    }

    @Override
    public int getItemCount() {
        return mEtoRecords == null || mEtoRecords.size() == 0 ? 1 : mEtoRecords.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mEtoRecords == null || mEtoRecords.size() == 0 ? TYPE_EMPTY : TYPE_CONTENT;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mTvEtoName;
        TextView mTvJoinAmount;
        TextView mTvReceiveAmount;
        TextView mTvTime;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvEtoName = itemView.findViewById(R.id.item_eto_records_tv_eto_name);
            mTvJoinAmount = itemView.findViewById(R.id.text_join_eto_amount);
            mTvReceiveAmount = itemView.findViewById(R.id.text_receive_token_amount);
            mTvTime = itemView.findViewById(R.id.text_eto_record_time);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(EtoProject etoProject);
    }

}
