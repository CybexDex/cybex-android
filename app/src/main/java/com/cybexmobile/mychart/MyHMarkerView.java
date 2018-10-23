package com.cybexmobile.mychart;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cybexmobile.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

public class MyHMarkerView extends MarkerView {

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    private ImageView markerTv;
    public MyHMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        markerTv = findViewById(R.id.marker_tv);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
    }

    public void setTvWidth(int width){
        LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) markerTv.getLayoutParams();
        params.width=width;
        markerTv.setLayoutParams(params);
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
