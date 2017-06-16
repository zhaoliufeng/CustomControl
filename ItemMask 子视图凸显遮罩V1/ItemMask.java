package me.zhaoliufeng.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;

public class ItemMask extends View{

    private View mItemView;     //选中的子视图
    private Paint mPaint;
    private Paint mMenuPaint;
    private Path mPath;
    private Context mContext;
    private int mRadius = 0;    //当前菜单圆的半径
    private int mStatus = MASK_HIDE;       //当前状态
    private int mTouchX, mTouchY;
    private boolean showMenu = false;

    private static final int MASK_HIDE = 0;
    private static final int MASK_SHOW = 1;
    private static final int MASK_HIDING = 2;
    private static final int MASK_SHOWING = 3;

    public ItemMask(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public ItemMask(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    private void initView(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        mPaint.setAlpha(200);

        mMenuPaint = new Paint();
        mMenuPaint.setAntiAlias(true);
        mMenuPaint.setStyle(Paint.Style.FILL);
        mMenuPaint.setColor(Color.BLUE);

        mPath = new Path();

        setVisibility(GONE);
    }

    /**
     * @param itemView 设置子视图 开始动画
     */
    public void setItemView(View itemView){
        //避免重复添加
        if (itemView == mItemView)return;
        setVisibility(VISIBLE);
        mStatus = MASK_SHOW;
        mItemView = itemView;
        maskShow();
    }

    /**
     * @param x 触摸在view上的起点的x坐标 坐标计算方法 当前控件位置 + 触摸位置
     * @param y 触摸在view上的起点的y坐标
     */
    public void showMenu(int x, int y){
        showMenu = true;
        int[] loc = new int[2];
        mItemView.getLocationInWindow(loc);
        mTouchX = loc[0] + x;
        mTouchY = loc[1] + y - getStatusBarHeight();
        menuShow();
        Log.e("Quadrant", getQuadrant(mTouchX, mTouchY) + "");
    }

    private void maskShow(){
        //mask透明度动画
        ValueAnimator animator = ValueAnimator.ofInt(0, 200).setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPaint.setAlpha((int)animation.getAnimatedValue());
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mStatus = MASK_SHOW;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mStatus = MASK_SHOWING;
            }
        });
        animator.start();
    }

    private void maskHide(){
        ValueAnimator animator = ValueAnimator.ofInt(200, 0).setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPaint.setAlpha((int)animation.getAnimatedValue());
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setVisibility(GONE);
                showMenu = false;
                mTouchX = 0;
                mTouchY = 0;
                mItemView = null;
                mPath.reset();
                mStatus = MASK_HIDE;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mStatus = MASK_HIDING;
            }
        });
        animator.start();
    }

    private void menuShow(){
        ValueAnimator animator = ValueAnimator.ofInt(0, 150).setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int tempY = mTouchY;
            int tempR = mRadius;
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mTouchY = tempY - (int)animation.getAnimatedValue();
                mRadius = tempR + (int)animation.getAnimatedValue()/5;
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mStatus = MASK_SHOW;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mStatus = MASK_SHOWING;
            }
        });
        animator.start();
    }

    private void menuHide(){
        ValueAnimator animator = ValueAnimator.ofInt(0, 150).setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int tempY = mTouchY;
            int tempR = mRadius;
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mTouchY = tempY + (int)animation.getAnimatedValue();
                mRadius = tempR - (int)animation.getAnimatedValue()/5;
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mRadius = 0;
                mStatus = MASK_HIDE;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mStatus = MASK_HIDING;
            }
        });
        animator.setInterpolator(new AnticipateInterpolator());
        animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mItemView == null) return;
        //获取子视图在界面中的起点坐标位置
        final int x = getViewLocationXY(mItemView)[0];
        final int y = getViewLocationXY(mItemView)[1] - getStatusBarHeight();
        //获取子视图宽高
        float itemWidth = mItemView.getMeasuredWidth();
        float itemHeight = mItemView.getMeasuredHeight();

        mPath.moveTo(0, 0);
        mPath.lineTo(getScreenWidth(mContext), 0);
        //移动到子视图上方
        mPath.lineTo(x, 0);
        mPath.lineTo(x, y);
        mPath.lineTo(x, y + itemHeight);
        mPath.lineTo(x + itemWidth, y + itemHeight);
        mPath.lineTo(x + itemWidth, y);
        mPath.lineTo(x, y);
        // 闭合子视图区间
        mPath.lineTo(x, 0);
        mPath.lineTo(getScreenWidth(mContext), 0);
        mPath.lineTo(getScreenWidth(mContext), getScreenHeight(mContext));
        mPath.lineTo(0, getScreenHeight(mContext));
        mPath.close();
        canvas.drawPath(mPath, mPaint);

        //画菜单
        if (!showMenu)return;
        canvas.drawCircle(mTouchX, mTouchY, mRadius, mMenuPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (event.getX() > mTouchX - mRadius/2 && event.getX() < mTouchX + mRadius/2
                        && event.getY() > mTouchY - mRadius/2 && event.getY() < mTouchY + mRadius/2){
                    Log.e(this.getClass().getName(), "点在圆中了");
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                break;
            case MotionEvent.ACTION_UP:
                if (mItemView == null || mStatus != MASK_SHOW) break;
                int[] loc = getViewLocationXY(mItemView);
                final int x = loc[0];
                final int y = loc[1] - getStatusBarHeight();
                //获取子视图宽高
                final float itemWidth = mItemView.getMeasuredWidth();
                final float itemHeight = mItemView.getMeasuredHeight();
                //判断触摸点是否在子视图区间
                if (!(event.getX() > x && event.getX() < x + itemWidth
                        && event.getY() > y && event.getY() < y + itemHeight)){
                    maskHide();
                    menuHide();
                }
                break;
        }
        return true;
    }

    //获取view在屏幕中的坐标位置
    private int[] getViewLocationXY(View view){
        int[] loc = new int[2];
        view.getLocationInWindow(loc);
        return loc;
    }

    //获取状态栏高度
    private int getStatusBarHeight(){
        int statusBarHeight = -1;
        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    //获取屏幕的宽度
    private int getScreenWidth(Context context){
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getWidth();
    }

    //获取屏幕的高度
    private int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getHeight();
    }

    /**
     * 根据坐标点获取象限值 根据屏幕起点为左上方 1-4 象限分别为 左上 右上 右下 左下
     * @param x 坐标点x
     * @param y 坐标点y
     * @return  返回的象限值
     */
    private int getQuadrant(int x, int y){
        if (x < getScreenWidth(mContext) / 2 && y < getScreenHeight(mContext) / 2){
            return 1;
        }
        if (x < getScreenWidth(mContext) && x  > getScreenWidth(mContext) /2 && y < getScreenHeight(mContext) / 2){
            return 2;
        }
        if (x < getScreenWidth(mContext) && x  > getScreenWidth(mContext) /2 && y < getScreenHeight(mContext) && y > getScreenHeight(mContext) / 2){
            return 3;
        }
        if (x  < getScreenWidth(mContext) /2 && y < getScreenHeight(mContext) && y > getScreenHeight(mContext) / 2){
            return 4;
        }
        return 0;
    }
}
