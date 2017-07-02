package me.zhaoliufeng.customviews.BootPage;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import me.zhaoliufeng.customviews.R;

public class BootPage extends FrameLayout implements View.OnTouchListener {

    //View数组用于存储显示的视图
    private List<View> views = new ArrayList<>();
    //当前的页数
    private int mPageNum = 0;
    //页数指示器
    private Indicator indicator;
    //屏幕宽度 用于计算PageNumber
    private int screenWidth;
    //背景圆的颜色, 正常状态的中心圆颜色, 选中状态下中心圆的颜色
    private int roundCircleColor, centralCircleNormalColor, currentCircleColor;
    //背景圆的半径, 中心圆的半径, 圆间距
    private float roundCircleRadius, centralCircleRadius, interval;
    //是否显示指示器
    private boolean displayIndicator;
    //切换页面监听
    private OnPageChangeListener mOnPageChangeListener;

    public BootPage(Context context) {
        super(context);
        setWillNotDraw(false);
        indicator = new Indicator(getContext());
    }

    public BootPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        //读取XML控件属性参数
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.BootPage);
        roundCircleColor = arr.getColor(R.styleable.BootPage_indicator_roundCircleColor, 0x66000000);
        centralCircleNormalColor = arr.getColor(R.styleable.BootPage_indicator_centralCircleNormalColor, 0xffeeeeee);
        currentCircleColor = arr.getColor(R.styleable.BootPage_indicator_currentCircleColor, 0xFF50D795);
        roundCircleRadius = arr.getFloat(R.styleable.BootPage_roundCircleRadius, 10.0f);
        centralCircleRadius = arr.getFloat(R.styleable.BootPage_centralCircleRadius, 7.0f);
        interval = arr.getFloat(R.styleable.BootPage_interval, 50.0f);
        displayIndicator = arr.getBoolean(R.styleable.BootPage_displayIndicator, true);
        if (displayIndicator){
            indicator = new Indicator(getContext());
        }
    }

    public int getPageNum(){
        return mPageNum;
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < getChildCount(); i++){
            views.add(getChildAt(i));
            getChildAt(i).setOnTouchListener(this);
        }
        if (displayIndicator){
            indicator.setCircleCount(getChildCount());
            addView(indicator);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        for (int i = 0; i < views.size(); i++){
            views.get(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        if (displayIndicator) {
            //设置指示器大小 总高*0.2
            int realWidth = MeasureSpec.getSize(heightMeasureSpec);
            int tempHeightMeasure = MeasureSpec.makeMeasureSpec((int) (realWidth * 0.2), MeasureSpec.EXACTLY);
            indicator.measure(widthMeasureSpec, tempHeightMeasure);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        for (int i = 0; i < views.size(); i++){
            views.get(i).layout(views.get(i).getMeasuredWidth() * i, top, views.get(i).getMeasuredWidth() * (i+1), bottom);
        }
        if (displayIndicator){
            //设置指示器位置 屏幕距离底部 1/10
            indicator.layout(left, top + (int)(bottom*0.8), right,bottom - (int)(bottom*0.1));
            indicator.setColor(roundCircleColor, centralCircleNormalColor, currentCircleColor);
            indicator.setDistance(roundCircleRadius, centralCircleRadius, interval);
        }
        screenWidth = getWidth();
    }

    //菜单位移上一次的x轴位置
    float lastX;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                //移动的距离
                float distanceX = lastX - event.getRawX();
                //下次X的位置
                float nextX = views.get(0).getX() - distanceX;
                float nextLx = views.get(views.size()-1).getX() - distanceX;
                mPageNum = (int)(Math.abs(nextX)/this.getWidth());
                //如果第一个视图的x为0 则判断是第一页右移不做处理
                if (nextX > 0.0f){
                    //如果是左移 则不做处理
                    if (distanceX < 0){
                        break;
                    }
                }
                //如果最后一个视图的x为0 则判断是最后一页左移不做处理
                if (nextLx <= 0.0f){
                    //如果是左移 则不做处理
                    if (distanceX > 0){
                        break;
                    }
                }
                //视图位置减去移动距离 无论左右 即为当前位置 如 右移 5 - (-5) 左移 5 - 5
                for (int i = 0; i < views.size(); i++){
                    views.get(i).setX(views.get(i).getX() - distanceX);
                }
                lastX = event.getRawX();
                break;
            case MotionEvent.ACTION_UP:
                //判断手指离开时第当前视图的x位置大于屏幕的一般时,  左移
                if (views.get(mPageNum).getX() > - getWidth()/2){
                    for (int i = 0; i < views.size(); i++){
                        ObjectAnimator.ofFloat(views.get(i), "x", views.get(i).getX(), (i - mPageNum) * getWidth())
                                .setDuration(200)
                                .start();
                    }
                }else if (views.get(mPageNum).getX() <= -getWidth()/2){
                    for (int i = 0; i < views.size(); i++) {
                        ObjectAnimator.ofFloat(views.get(i), "x", views.get(i).getX(), (i-1- mPageNum) * getWidth())
                                .setDuration(200)
                                .start();
                    }
                }
                //指示器更新命令
                handler.sendEmptyMessageDelayed(0, 230);
                break;
        }
        return true;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    //动画之后完后判断现在在第几页（ 第一视图的x取绝对值 / 屏宽 ）及 第 i + 1页
                    mPageNum = (int)(Math.abs(views.get(0).getX())/screenWidth);
                    indicator.setSelectPosition(mPageNum);
                    if (mOnPageChangeListener != null)
                        mOnPageChangeListener.onPageChange(mPageNum);
                    break;
            }
        }
    };

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener){
        mOnPageChangeListener = onPageChangeListener;
    }
    public interface OnPageChangeListener{
        void onPageChange(int index);
    }
}
