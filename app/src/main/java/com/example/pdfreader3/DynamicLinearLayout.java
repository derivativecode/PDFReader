package com.example.pdfreader3;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

public class DynamicLinearLayout extends RelativeLayout {
    public DynamicLinearLayout(Context context) {
        super(context);
    }

    public DynamicLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DynamicLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
