package com.cybexmobile.mychart;

import android.content.Context;
import android.widget.TextView;

import com.cybex.basemodule.utils.AssetUtil;
import com.cybexmobile.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

public class MyLeftMarkerView extends MarkerView {

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    private TextView markerTv;
    private float num;
    private int precision;

    public MyLeftMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        markerTv = findViewById(R.id.marker_tv);
        markerTv.setTextSize(10);
    }

    public void setData(float num, int precision){
        this.num=num;
        this.precision = precision;
    }
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        markerTv.setText(AssetUtil.formatNumberRounding(Double.parseDouble(String.valueOf(num)), this.precision));
    }

    @Override
    public int getXOffset(float xpos) {
        return 0;
    }

    @Override
    public int getYOffset(float ypos) {
        return 0;
    }
}
