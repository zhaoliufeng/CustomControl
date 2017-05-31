package me.zhaoliufeng.vehiclemanagesystem.View.UIControl;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
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
        try {
            int num = Integer.valueOf(numStr);
            ValueAnimator valueAnimator = ValueAnimator.ofInt(Integer.valueOf(getText().toString()), num)
                    .setDuration(500);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setText(animation.getAnimatedValue().toString());
                }
            });
            valueAnimator.start();
        } catch (NumberFormatException e) {
            float num = Float.valueOf(numStr);
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(Float.valueOf(getText().toString()), num)
                    .setDuration(500);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setText(String.format("%.1f", animation.getAnimatedValue()));
                }
            });
            valueAnimator.start();
        }
    }
}
