package com.cybexmobile.activity.transfer;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseActivity;
import com.cybexmobile.R;
import com.cybex.basemodule.BitsharesWalletWraper;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.MemoData;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.utils.DateUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.cybex.basemodule.constant.Constant.PREF_NAME;

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
    @BindView(R.id.transfer_details_tv_click_to_view)
    TextView mTvTransferClickToView;

    private String mUserName;

    private Operations.transfer_operation mTransferOperation;
    private AccountObject mFromAccount;
    private AccountObject mToAccount;
    private AssetObject mFeeAsset;
    private AssetObject mTransferAsset;
    private AccountObject mAccountObject;
    private String mTimestamp;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mTransferOperation = (Operations.transfer_operation) intent.getSerializableExtra(Constant.INTENT_PARAM_TRANSFER_OPERATION);
        mFromAccount = (AccountObject) intent.getSerializableExtra(Constant.INTENT_PARAM_TRANSFER_FROM_ACCOUNT);
        mToAccount = (AccountObject) intent.getSerializableExtra(Constant.INTENT_PARAM_TRANSFER_TO_ACCOUNT);
        mFeeAsset = (AssetObject) intent.getSerializableExtra(Constant.INTENT_PARAM_TRANSFER_FEE_ASSET);
        mTransferAsset = (AssetObject) intent.getSerializableExtra(Constant.INTENT_PARAM_TRANSFER_ASSET);
        mAccountObject = (AccountObject) intent.getSerializableExtra(Constant.INTENT_PARAM_TRANSFER_MY_ACCOUNT);
        mTimestamp = intent.getStringExtra(Constant.INTENT_PARAM_TIMESTAMP);
        setContentView(R.layout.activity_transfer_details);
        mUnbinder = ButterKnife.bind(this);
        mUserName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        setSupportActionBar(mToolbar);
        mTvTransferMemo.setMovementMethod(ScrollingMovementMethod.getInstance());
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
        if (BitsharesWalletWraper.getInstance().is_locked()) {
            CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mAccountObject, mUserName, new UnlockDialog.UnLockDialogClickListener() {
                @Override
                public void onUnLocked(String password) {
                    showMemoMessage(mTransferOperation.memo);
                }
            });
        } else {
            showMemoMessage(mTransferOperation.memo);
        }

    }

    private void initViewData(){
        /**
         * fix bug：CYM-518
         * 解决转入转出状态错误
         */
        if(mFromAccount != null && mToAccount != null && mAccountObject != null){
            if(mFromAccount.id.equals(mAccountObject.id)){
                mTvTransferToFromAccountName.setText(mToAccount.name);
                mTvTransferToFrom.setText(getResources().getString(R.string.text_to));
                mIvTransferAction.setImageResource(R.drawable.ic_outcome_24_px);
                mTvTransferAction.setText(getResources().getString(R.string.text_out));
                mTvTransferAmount.setTextColor(getResources().getColor(R.color.font_color_white_dark));
                if(mTransferAsset != null){
                    mTvTransferAmount.setText(String.format("-%s %s",
                            AssetUtil.formatNumberRounding( mTransferOperation.amount.amount / Math.pow(10, mTransferAsset.precision), mTransferAsset.precision),
                            AssetUtil.parseSymbol(mTransferAsset.symbol)));
                }
            } else if(mToAccount.id.equals(mAccountObject.id)){
                mTvTransferToFromAccountName.setText(mFromAccount.name);
                mTvTransferToFrom.setText(getResources().getString(R.string.text_from));
                mIvTransferAction.setImageResource(R.drawable.ic_income_24_px);
                mTvTransferAction.setText(getResources().getString(R.string.text_in));
                mTvTransferAmount.setTextColor(getResources().getColor(R.color.primary_color_orange));
                if(mTransferAsset != null){
                    mTvTransferAmount.setText(String.format("+%s %s",
                            AssetUtil.formatNumberRounding( mTransferOperation.amount.amount / Math.pow(10, mTransferAsset.precision), mTransferAsset.precision),
                            AssetUtil.parseSymbol(mTransferAsset.symbol)));
                }
            }
        }
        if(mTransferOperation != null){
            if(mTransferOperation.memo == null){
                mTvTransferMemo.setText(getResources().getString(R.string.text_none));
                mTvTransferClickToView.setVisibility(View.GONE);
            }
            try {
                Iterator it = mTransferOperation.extensions.iterator();
                if(!it.hasNext()){
                    mTvTransferVestingPeriod.setText(getResources().getString(R.string.text_none));
                } else {
                    Iterator its = ((List)it.next()).iterator();
                    its.next();
                    Map map = (Map) its.next();
                    /**
                     * fix bug:CYM-557
                     * 解决锁定期时间显示错误的问题
                     */
                    mTvTransferVestingPeriod.setText(parseTime((int) ((double) map.get("vesting_period"))));
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
        mTvTransferTime.setText(DateUtils.formatToDate(DateUtils.PATTERN_MM_dd_HH_mm_ss, DateUtils.formatToMillis(mTimestamp)));
    }

    private String parseTime(int time){
        if(time <= 0){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        int day = time / DateUtils.DAY_IN_SECOND;
        if(day > 0){
            sb.append(day).append(getResources().getString(R.string.text_day));
        }
        int hours = (time % DateUtils.DAY_IN_SECOND) / DateUtils.HOUR_IN_SECOND;
        if(hours > 0){
            sb.append(hours).append(getResources().getString(R.string.text_hours));
        }
        int minutes = ((time % DateUtils.DAY_IN_SECOND) % DateUtils.HOUR_IN_SECOND) / DateUtils.MINUTE_IN_SECOND;
        if(minutes > 0){
            sb.append(minutes).append(getResources().getString(R.string.text_minutes));
        }
        int seconds = ((time % DateUtils.DAY_IN_SECOND) % DateUtils.HOUR_IN_SECOND) % DateUtils.MINUTE_IN_SECOND;
        if(seconds > 0){
            sb.append(seconds).append(getResources().getString(R.string.text_seconds));
        }
        return sb.toString();
    }

    private void showMemoMessage(MemoData memo) {
        if(memo == null){
            return;
        }
        String memomessage = BitsharesWalletWraper.getInstance().getMemoMessage(memo);
        mTvTransferMemo.setText(memomessage);
        mTvTransferClickToView.setVisibility(View.GONE);
    }

}
