package com.cybexmobile.activity.gateway.records;

import com.cybex.basemodule.base.IMvpView;
import com.cybex.provider.http.gateway.entity.GatewayNewDepositWithdrawRecordItem;
import com.cybexmobile.data.item.GatewayDepositWithdrawRecordsItem;

import java.util.List;

public interface DepositAndWithdrawTotalView extends IMvpView {
    void onLoadRecordsData(int loadMode, List<GatewayNewDepositWithdrawRecordItem> gatewayDepositWithdrawRecordsItems);
    void onLoadAsset(List<String> assetList);
}
