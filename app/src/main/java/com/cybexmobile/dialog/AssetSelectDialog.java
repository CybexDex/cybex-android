package com.cybexmobile.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.adapter.AssetSelectRecyclerViewAdapter;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.cybexmobile.utils.Constant.INTENT_PARAM_ACCOUNT_BALANCE_ITEM;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_ACCOUNT_BALANCE_ITEMS;

public class AssetSelectDialog extends DialogFragment implements AssetSelectRecyclerViewAdapter.OnItemClickListener {

    @BindView(R.id.dialog_asset_select_tv_confirm)
    TextView mTvOrdinaryMarket;
    @BindView(R.id.dialog_asset_select_rv_asset)
    RecyclerView mRvAsset;

    private Unbinder mUnbinder;

    private OnAssetSelectedListener mAssetSelectedListener;
    private List<AccountBalanceObjectItem> mAccountBalanceObjectItems;
    private AccountBalanceObjectItem mSelectedAccountBalanceObjectItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_Dialog_Bottom);
        mSelectedAccountBalanceObjectItem = (AccountBalanceObjectItem) getArguments().getSerializable(INTENT_PARAM_ACCOUNT_BALANCE_ITEM);
        mAccountBalanceObjectItems = (List<AccountBalanceObjectItem>) getArguments().getSerializable(INTENT_PARAM_ACCOUNT_BALANCE_ITEMS);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        View view = inflater.inflate(R.layout.dialog_asset_select, window.findViewById(android.R.id.content), false);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        mUnbinder = ButterKnife.bind(this, view);
        mRvAsset.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AssetSelectRecyclerViewAdapter assetSelectRecyclerViewAdapter = new AssetSelectRecyclerViewAdapter(getContext(),
                mSelectedAccountBalanceObjectItem, mAccountBalanceObjectItems);
        assetSelectRecyclerViewAdapter.setOnItemClickListener(this);
        mRvAsset.setAdapter(assetSelectRecyclerViewAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

    }

    @OnClick(R.id.dialog_asset_select_tv_confirm)
    public void onSelectedConfirm(View view){
        if(mAssetSelectedListener != null){
            mAssetSelectedListener.onAssetSelected(mSelectedAccountBalanceObjectItem);
        }
        this.dismiss();
    }

    @Override
    public void onItemClick(AccountBalanceObjectItem accountBalanceObjectItem) {
        mSelectedAccountBalanceObjectItem = accountBalanceObjectItem;
    }

    public void setOnAssetSelectedListener(OnAssetSelectedListener assetSelectedListener){
        mAssetSelectedListener = assetSelectedListener;
    }

    public interface OnAssetSelectedListener {
        void onAssetSelected(AccountBalanceObjectItem accountBalanceObjectItem);
    }
}
