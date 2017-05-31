package me.zhaoliufeng.vehiclemanagesystem.View.Control;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.zhy.android.percent.support.PercentFrameLayout;

import me.zhaoliufeng.vehiclemanagesystem.R;


public class DrawerPlusLayout extends PercentFrameLayout implements View.OnTouchListener, View.OnClickListener {

    //左侧菜单偏移距离
    private float leftMenuOffsetDistance;
    //左侧菜单宽度与屏宽的比例
    private float leftMenuWidthScale;
    //左侧菜单顶部向下偏移量
    private float leftMenuTopOffsetDistance;
    //侧拉布局
    private FrameLayout leftContentLayout;
    //中间视图
    private FrameLayout midLayout;
    //蒙板视图
    private View maskView;
    //菜单位移上一次的x轴位置
    float lastX;
    //菜单打开监听
    private DrawerStatusListener drawerStatusListener;

    public DrawerPlusLayout(Context context) {
        super(context);
    }

    public DrawerPlusLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.DrawerPlusLayout);
        leftMenuOffsetDistance = arr.getDimension(R.styleable.DrawerPlusLayout_leftMenuOffsetDistance, 30);
        leftMenuWidthScale = arr.getFloat(R.styleable.DrawerPlusLayout_leftMenuWidthScale, 0.55f);
        leftMenuTopOffsetDistance = arr.getDimension(R.styleable.DrawerPlusLayout_leftMenuTopOffsetDistance, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        leftContentLayout = (FrameLayout)getChildAt(1);
        maskView = new View(getContext());
        midLayout = (FrameLayout)getChildAt(0);

        leftContentLayout.setOnTouchListener(this);

        maskView.setBackgroundColor(0x88000000);
        maskView.setAlpha(0.0f);
        maskView.setVisibility(GONE);
        addView(maskView);
        maskView.setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        maskView.measure(widthMeasureSpec, heightMeasureSpec);
        midLayout.measure(widthMeasureSpec, heightMeasureSpec);
        int realWidth = MeasureSpec.getSize(widthMeasureSpec);
        int tempWidthMeasure = MeasureSpec.makeMeasureSpec((int)(realWidth*leftMenuWidthScale), MeasureSpec.EXACTLY);
        leftContentLayout.measure(tempWidthMeasure, heightMeasureSpec - (int)leftMenuTopOffsetDistance);
        bringChildToFront(leftContentLayout);
    }

    //重新布局，获取到了界面的高度后
    public void onReLayout(int distance){
        leftMenuTopOffsetDistance = distance;
        layout(left, top, right, bottom);
    }

    private int left, top, right, bottom;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        left = l;
        top = t;
        right = r;
        bottom = b;

        //设置布局视图位置
        midLayout.layout(l, t, r, b);
        //左抽屉视图的位置 偏移 leftMenuOffsetDistance 个dp
        leftContentLayout.layout(-leftContentLayout.getMeasuredWidth() + (int) leftMenuOffsetDistance, t + (int)leftMenuTopOffsetDistance, l + (int) leftMenuOffsetDistance, b + (int)leftMenuTopOffsetDistance);
        maskView.layout(l, t + (int)leftMenuTopOffsetDistance, r, b + (int)leftMenuTopOffsetDistance);
    }

    private void setMaskAlpha(float x, View view){
        float alpha =  x / (float)(view.getWidth()*0.25);
        view.setAlpha(alpha);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastX = motionEvent.getRawX();
                maskView.setVisibility(VISIBLE);
                break;
            case MotionEvent.ACTION_MOVE:
                float distanceX = lastX - motionEvent.getRawX();
                //下一次的位置 用于判断是否越界
                float nextX = view.getX() - distanceX;
                if (nextX > 0){
                    nextX = 0;
                }
                ObjectAnimator.ofFloat(view, "x", view.getX(), nextX)
                        .setDuration(0)
                        .start();
                lastX = motionEvent.getRawX();
                if (lastX <= view.getWidth()*0.5){
                    setMaskAlpha(Math.abs(lastX), maskView);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (lastX > 0.5*view.getWidth()){
                    maskView.setAlpha(1.0f);
                    ObjectAnimator.ofFloat(view, "x", view.getX(), 0)
                            .setDuration(100)
                            .start();
                    drawerStatusListener.onDrawerStatusChange(true);
                }else{
                    maskView.setAlpha(0.0f);
                    ObjectAnimator.ofFloat(view, "x", view.getX(), -view.getWidth() + leftMenuOffsetDistance)
                            .setDuration(100)
                            .start();
                    drawerStatusListener.onDrawerStatusChange(false);
                }
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        view.setAlpha(0.0f);
        view.setVisibility(GONE);
        ObjectAnimator.ofFloat(leftContentLayout, "x", leftContentLayout.getX(), - leftContentLayout.getWidth() + leftMenuOffsetDistance)
                .setDuration(200)
                .start();
        drawerStatusListener.onDrawerStatusChange(false);
    }

    public void setDrawerStatusListener(DrawerStatusListener drawerStatusListener){
        this.drawerStatusListener = drawerStatusListener;
    }

    public interface DrawerStatusListener{
        void onDrawerStatusChange(boolean open);
    }
}
