package com.cybexmobile.activity.transfer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.graphene.chain.AccountObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.BlockHeader;
import com.cybexmobile.graphene.chain.Operations;
import com.cybexmobile.utils.AssetUtil;
import com.cybexmobile.utils.Constant;
import com.cybexmobile.utils.DateUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class TransferDetailsActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.transfer_details_iv_transfer_action)
    ImageView mIvTransferAction;
    @BindView(R.id.transfer_details_tv_transfer_action)
    TextView mTvTransferAction;
    @BindView(R.id.transfer_details_tv_transfer_to_or_from)
    TextView mTvTransferToFrom;
    @BindView(R.id.transfer_details_tv_transfer_to_or_from_account_name)
    TextView mTvTransferToFromAccountName;
    @BindView(R.id.transfer_details_tv_transfer_amount)
    TextView mTvTransferAmount;
    @BindView(R.id.transfer_details_tv_transfer_fee)
    TextView mTvTransferFee;
    @BindView(R.id.transfer_details_tv_transfer_memo)
    TextView mTvTransferMemo;
    @BindView(R.id.transfer_details_tv_transfer_time)
    TextView mTvTransferTime;
    @BindView(R.id.transfer_details_tv_vesting_period)
    TextView mTvTransferVestingPeriod;

    private Operations.transfer_operation mTransferOperation;
    private BlockHeader mBlock;
    private AccountObject mFromAccount;
    private AccountObject mToAccount;
    private AssetObject mFeeAsset;
    private AssetObject mTransferAsset;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mTransferOperation = (Operations.transfer_operation) intent.getSerializableExtra(Constant.INTENT_PARAM_TRANSFER_OPERATION);
        mBlock = (BlockHeader) intent.getSerializableExtra(Constant.INTENT_PARAM_TRANSFER_BLOCK);
        mFromAccount = (AccountObject) intent.getSerializableExtra(Constant.INTENT_PARAM_TRANSFER_FROM_ACCOUNT);
        mToAccount = (AccountObject) intent.getSerializableExtra(Constant.INTENT_PARAM_TRANSFER_TO_ACCOUNT);
        mFeeAsset = (AssetObject) intent.getSerializableExtra(Constant.INTENT_PARAM_TRANSFER_FEE_ASSET);
        mTransferAsset = (AssetObject) intent.getSerializableExtra(Constant.INTENT_PARAM_TRANSFER_ASSET);
        setContentView(R.layout.activity_transfer_details);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        initViewData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @OnClick(R.id.transfer_details_tv_click_to_view)
    public void onClickToViewClick(View view){

    }

    private void initViewData(){
        if(mFromAccount != null || mToAccount != null){
            if(mFromAccount != null){
                mTvTransferToFromAccountName.setText(mFromAccount.name);
                mTvTransferToFrom.setText(getResources().getString(R.string.text_from));
            }
            if(mToAccount != null){
                mTvTransferToFromAccountName.setText(mToAccount.name);
                mTvTransferToFrom.setText(getResources().getString(R.string.text_to));
            }
            mIvTransferAction.setImageResource(mFromAccount == null ? R.drawable.ic_sent_40_px : R.drawable.ic_income_40_px);
            mTvTransferAction.setText(getResources().getString(mFromAccount == null ? R.string.text_out : R.string.text_in));
            if(mTransferAsset != null){
                mTvTransferAmount.setText(String.format(mFromAccount == null ? "-%s %s" : "+%s %s",
                        AssetUtil.formatNumberRounding( mTransferOperation.amount.amount / Math.pow(10, mTransferAsset.precision), mTransferAsset.precision),
                        AssetUtil.parseSymbol(mTransferAsset.symbol)));
            }
        }
        if(mTransferOperation != null){
            try {
                Iterator it = mTransferOperation.extensions.iterator();
                if(!it.hasNext()){
                    mTvTransferVestingPeriod.setText(getResources().getString(R.string.text_none));
                } else {
                    Iterator its = ((Set) it.next()).iterator();
                    its.next();
                    Map map = (Map) its.next();
                    mTvTransferVestingPeriod.setText(String.format("%s%s", map.get("vesting_period"),
                            getResources().getString(R.string.text_minutes)));
                }
            } catch (Exception e){
                e.printStackTrace();
                mTvTransferVestingPeriod.setText(getResources().getString(R.string.text_none));
            }
            if(mFeeAsset != null){
                mTvTransferFee.setText(String.format("%s %s",
                        AssetUtil.formatNumberRounding( mTransferOperation.fee.amount / Math.pow(10, mFeeAsset.precision), mFeeAsset.precision),
                        AssetUtil.parseSymbol(mFeeAsset.symbol)));
            }
        }
        if(mBlock != null){
            mTvTransferTime.setText(DateUtils.formatToDate(DateUtils.PATTERN_MM_dd_HH_mm_ss, DateUtils.formatToMillis(mBlock.timestamp)));
        }

    }

}
