package com.cybex.eto.activity.record;

import com.cybex.basemodule.base.IMvpView;
import com.cybex.provider.http.entity.EtoRecord;

import java.util.List;

public interface EtoRecordMvpView extends IMvpView {

    void onLoadEtoRecords(int mode, List<EtoRecord> etoRecords);
}
