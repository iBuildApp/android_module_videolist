package com.ibuildapp.romanblack.VideoPlugin.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;


public class VideoImageView extends ImageView{
    public VideoImageView(Context context) {
        super(context);
        setBackgroundColor(Color.parseColor("#4d000000"));
    }

    public VideoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.parseColor("#4d000000"));
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
