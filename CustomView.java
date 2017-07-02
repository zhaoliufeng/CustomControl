package me.zhaoliufeng.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.TextView;

/**
 * 自定义控件 用于控件api调用顺序的实验
 */

public class CustomView extends TextView {

    private static final String TAG = "CustomView";
    public CustomView(Context context) {
        super(context);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setGravity(Gravity.CENTER);
        Log.i(TAG, "Constructor");
        this.append("\nConstructor\n");
        //获取视图树观察者 并添加布局改变监听
        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.i(TAG, "onGlobalLayout");
                append("\nonGlobalLayout\n");
            }
        });
    }

    /**
     * 加载布局文件
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i(TAG, "onFinishInflate");
        this.append("\nonFinishInflate\n");
    }

    /**
     * 加载完布局文件后 测量控件的宽高 在这之后getWidth不为0
     * @param widthMeasureSpec 宽度以及测量方式
     * @param heightMeasureSpec 高度以及测量方式
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        Log.i(TAG, "onMeasure; Width : " + width);
        this.append("\nonMeasure \n-> MeasureWidth : " + width + " -> getWidth() : " + getWidth() + "\n");
    }

    /**
     * 测量完成后改变view的宽高
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG, "onSizeChanged");
        this.append("\nonSizeChanged\n-> getWidth() : " + getWidth() + " -> oldw " + oldw + " -> w " + w + "\n" );
    }

    /**
     * 大小改变完成之后 开始摆放控件内其他视图的位置 触发视图树观察者的布局响应监听
     * @param changed 是否发生改变
     * @param left  左边起始位置
     * @param top   视图上边界起点
     * @param right 右边视图结束位置
     * @param bottom    下方视图结束位置
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.i(TAG, "onLayout");
        this.append("\nonLayout\n-> getWidth(): " + getWidth() +  " ltrb"  + left + " " + top + " " + right + " " + bottom + "\n");
    }

    /**
     * @param canvas    视图加载完成， 开始绘制内容
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "onDraw");
        this.append("\nonDraw\n");
    }

    /**
     * 控件加载完成 判断是否获取到焦点
     * @param hasWindowFocus 是否获取到焦点
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Log.i(TAG, "onWindowFocusChanged");
        this.append("\nonWindowFocusChanged\n-> getWidth() : " + getWidth() + "-> hasFocus " + hasFocus() + "\n");
    }

}
