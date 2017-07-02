package me.zhaoliufeng.customviews.NumTexView;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class NumTextView extends TextView {

    public NumTextView(Context context) {
        super(context);
        setText("0");
    }

    public NumTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setText("0");
    }

    public void setGrowNum(final String numStr) {
        ValueAnimator valueAnimator;
        try {
            int num = Integer.valueOf(numStr);
            valueAnimator = ValueAnimator.ofInt(Integer.valueOf(getText().toString()), num)
                    .setDuration(500);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Log.i("NumTextView", animation.getAnimatedValue().toString());
                    setText(animation.getAnimatedValue().toString());
                }
            });
        } catch (NumberFormatException e) {
            float num = Float.valueOf(numStr);
            valueAnimator = ValueAnimator.ofFloat(Float.valueOf(getText().toString()), num)
                    .setDuration(500);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setText(String.format("%.1f", animation.getAnimatedValue()));
                }
            });
        }
        valueAnimator.start();
    }
}
