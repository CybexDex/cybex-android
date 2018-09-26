package com.cybexmobile.activity.gateway.records;

import com.cybex.basemodule.base.IMvpView;
import com.cybexmobile.data.item.GatewayDepositWithdrawRecordsItem;

import java.util.List;

public interface DepositAndWithdrawTotalView extends IMvpView {
    void onLoadRecordsData(int loadMode, List<GatewayDepositWithdrawRecordsItem> gatewayDepositWithdrawRecordsItems);
    void onLoadAsset(List<String> assetList);
}
