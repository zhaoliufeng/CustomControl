package me.zhaoliufeng.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;

public class ItemMask extends View{

    private View mItemView;     //选中的子视图
    private Paint mPaint;
    private List<Paint> mMenuPaints;
    private Paint mTitlePaint;
    private Path mPath;
    private Context mContext;
    private int mRadius;    //当前菜单圆的半径
    private int mStatus = MASK_HIDE;       //当前状态
    private int mTouchX, mTouchY;
    private int mMenuStartX, mMenuStartY;
    private boolean showMenu = false;
    private int quadrant;   // 点击所在的象限
    private String[] mTitles;
    private OnMenuSelectListener mOnMenuSelectListener;
	 private int mOffsetY;   //视图需要偏移的量

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

        mTitlePaint = new Paint();
        mTitlePaint.setAntiAlias(true);
        mTitlePaint.setColor(Color.BLACK);
        mTitlePaint.setTextAlign(Paint.Align.CENTER);

        mMenuPaints = new ArrayList<>();
        mPath = new Path();

        setVisibility(GONE);
    }

    /**
     * @param itemView 设置子视图 开始动画
     */
    public void setItemView(View itemView, int offsetY){
        //避免重复添加
        if (itemView == mItemView)return;
        setVisibility(VISIBLE);
        mStatus = MASK_SHOW;
        mItemView = itemView;
		mOffsetY = offsetY;
        maskShow();
        if (showMenu){
            int[] loc = new int[2];
            mItemView.getLocationInWindow(loc);
            mMenuStartX = loc[0] + mTouchX;
            mMenuStartY = mTouchY;
            quadrant = getQuadrant(mMenuStartX, mMenuStartY);
            menuShow();
        }
    }

    /**当父容器是listView时 传递进来的坐标既是listView中的触摸位置 不用计算当前子view的高度
     * @param x 触摸在view上的起点的x坐标 坐标计算方法 当前控件位置 + 触摸位置
     * @param y 触摸在view上的起点的y坐标
     */
    public void showMenu(int x, int y, String[] titles){
        showMenu = true;
        mTouchX = x;
        mTouchY = y;

        if (titles.length > 3)
            Log.e("ItemMask", "菜单最大3个");

        mTitles = titles;
        for (String ignored : titles) {
            mMenuPaints.add(new Paint());
        }
        for (Paint paint : mMenuPaints){
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
        }
    }

    /**
     * 遮罩层显示
     */
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

    /**
     * 遮罩层关闭
     */
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

    /**
     * 菜单显示
     */
    private void menuShow(){
        ValueAnimator animator = ValueAnimator.ofInt(0, 150).setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int tempY = mMenuStartY;
            int tempR = mRadius;
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (quadrant == 1 || quadrant == 2){
                    mMenuStartY = tempY + (int)animation.getAnimatedValue();
                }else {
                    mMenuStartY = tempY - (int)animation.getAnimatedValue();
                }
                mRadius = tempR + (int)animation.getAnimatedValue()/3;
                mTitlePaint.setTextSize(mRadius/2);
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
        animator.setInterpolator(new OvershootInterpolator());
        animator.start();
    }

    /**
     * 菜单关闭
     */
    private void menuHide(){
        ValueAnimator animator = ValueAnimator.ofInt(0, 150).setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int tempY = mMenuStartY;
            int tempR = mRadius;
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 判断点击时的区间 如果是点击在屏幕上部 则菜单向上收起 反之向下收起
                if (quadrant == 1 || quadrant == 2){
                    mMenuStartY = tempY - (int)animation.getAnimatedValue();
                }else {
                    mMenuStartY = tempY + (int)animation.getAnimatedValue();
                }
                // 圆的半径以及文字的大小随动画改变
                mRadius = tempR - (int)animation.getAnimatedValue()/3;
                mTitlePaint.setTextSize(mRadius/2);
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mItemView == null) return;
        //获取子视图在界面中的起点坐标位置
        final int x = getViewLocationXY(mItemView)[0];
        final int y = getViewLocationXY(mItemView)[1] - getStatusBarHeight() - mOffsetY;
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
        for (int i = 0; i < mTitles.length; i++){
            //判断区间 选择执行动画
            if (quadrant == 1 || quadrant == 4){
                canvas.drawCircle(mMenuStartX + i * mRadius*2.3f, mMenuStartY, mRadius, mMenuPaints.get(i));
            }else {
                canvas.drawCircle(mMenuStartX - i * mRadius*2.3f, mMenuStartY, mRadius, mMenuPaints.get(i));
            }
        }

        //画文字
        Paint.FontMetrics fontMetrics = mTitlePaint.getFontMetrics();
        float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
        float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom
        int baseLineY = (int) (mMenuStartY - top/2 - bottom/2);//基线中间点的y轴计算公式


        for (int i = 0; i < mTitles.length; i++){
            if (quadrant == 1 || quadrant == 4){
                canvas.drawText(mTitles[i], mMenuStartX + i * mRadius*2.3f, baseLineY, mTitlePaint);
            }else {
                canvas.drawText(mTitles[i], mMenuStartX - i * mRadius*2.3f, baseLineY, mTitlePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < mTitles.length; i++){
                    if (quadrant == 1 || quadrant == 4){
                        if (event.getX() >  (mMenuStartX + i * mRadius*2.3f) - mRadius/2 && event.getX() < (mMenuStartX + i * mRadius*2.3f) + mRadius/2
                                && event.getY() > mMenuStartY - mRadius/2 && event.getY() < mMenuStartY + mRadius/2){
                            Log.e(this.getClass().getName(), mTitles[i]);
                            if (mOnMenuSelectListener != null)
                                mOnMenuSelectListener.onMenuSelect(i);
                            return true;
                        }
                    }else {
                        if (event.getX() >  (mMenuStartX - i * mRadius*2.3f) - mRadius/2 && event.getX() < (mMenuStartX - i * mRadius*2.3f) + mRadius/2
                                && event.getY() > mMenuStartY - mRadius/2 && event.getY() < mMenuStartY + mRadius/2){
                            Log.e(this.getClass().getName(), mTitles[i]);
                            if (mOnMenuSelectListener != null)
                                mOnMenuSelectListener.onMenuSelect(i);
                            return true;
                        }
                    }
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

    public void setOnMenuSelectListener(OnMenuSelectListener onMenuSelectListener){
            mOnMenuSelectListener = onMenuSelectListener;
    }

    public interface OnMenuSelectListener{
        void onMenuSelect(int index);
    }
}
