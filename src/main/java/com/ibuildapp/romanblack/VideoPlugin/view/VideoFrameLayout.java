package com.ibuildapp.romanblack.VideoPlugin.view;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class VideoFrameLayout extends FrameLayout{
    public VideoFrameLayout(Context context) {
        super(context);
    }

    public VideoFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = (parentWidth/16)*9;
        heightMeasureSpec = (widthMeasureSpec/16)*9;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
    }
}
