package me.zhaoliufeng.customviews.DashBoardView;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import me.zhaoliufeng.customviews.R;

/**
 * ，仿汽车速度仪表盘
 */
public class DashboardView extends View {

    private int mRadius; // 扇形半径
    private int mStartAngle = 150; // 起始角度
    private int mSweepAngle = 240; // 绘制角度 30 + 180 + 30
    private int mMin = 0; // 最小值
    private int mMax = 180; // 最大值
    private int mSection = 9; // 值域（mMax-mMin）等分份数
    private int mPortion = 2; // 一个mSection等分份数
    private String mHeaderText = "km/h"; // 速度码表表头
    private String motorHeaderText = "*100 rpm"; // 电机表头
    private int mVelocity = mMin; // 实时速度
    private int mStrokeWidth; // 画笔宽度
    private int mLength1; // 长刻度的相对圆弧的长度
    private int mLength2; // 刻度读数顶部的相对圆弧的长度
    private int mPLRadius; // 指针长半径
    private int mPSRadius; // 指针短半径

    private int mPadding;
    private float mCenterX, mCenterY; // 圆心坐标
    private Paint mPaint;
    private RectF mRectFArc;
    private RectF mRectFInnerArc;
    private Rect mRectText;
    private String[] mTexts;
    private int mColor1 = 0xFFECEFF1;
    private int mColor2 = 0xFF455A64;
    private int viewStyle;
    private final static int BigView = 0;

    public DashboardView(Context context) {
        this(context, null);
    }

    public DashboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.DashboardView);
        viewStyle = arr.getInt(R.styleable.DashboardView_DashBoardViewStyle, 0);
    }

    public DashboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mStrokeWidth = dp2px(3);
        mLength1 = dp2px(8) + mStrokeWidth;
        mLength2 = mLength1 + dp2px(4);

        //初始化画笔
        mPaint = new Paint();
        //设置抗锯齿
        mPaint.setAntiAlias(true);
        //设置圆角笔触风格
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mRectFArc = new RectF();
        mRectFInnerArc = new RectF();
        mRectText = new Rect();

        mTexts = new String[mSection + 1]; // 需要显示mSection + 1个刻度读数
        int n = (mMax - mMin) / mSection; //一个区间间隔数值

        for (int i = 0; i < mTexts.length; i++) {
            //生成刻度读数数值 存入mTests数组
            mTexts[i] = String.valueOf(mMin + i * n);
        }
    }

    //测量 确定高度
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //设置View主体内推距离
        mPadding = Math.max(
                Math.max(getPaddingLeft(), getPaddingTop()),
                Math.max(getPaddingRight(), getPaddingBottom())
        );
        setPadding(mPadding, mPadding, mPadding, mPadding);

        //获取可用屏幕宽度
        int width = resolveSize(dp2px(260), widthMeasureSpec);
        //扇形半径 = ( 控件宽 - ( 内推距离 + 画笔粗细 ) * 2 ) / 2;
        mRadius = (width - (mPadding + mStrokeWidth) * 2) / 2;
        // 设置View大小
        setMeasuredDimension(width, width-30);

        mCenterX = mCenterY = getMeasuredWidth() / 2f;
        mRectFArc.set(
                getPaddingLeft() + mStrokeWidth,
                getPaddingTop() + mStrokeWidth,
                getMeasuredWidth() - getPaddingRight() - mStrokeWidth,
                getMeasuredWidth() - getPaddingBottom() - mStrokeWidth
        );

        mPaint.setTextSize(sp2px(16));
        mPaint.getTextBounds("0", 0, "0".length(), mRectText);
        mRectFInnerArc.set(
                getPaddingLeft() + mLength2 + mRectText.height() + dp2px(30),
                getPaddingTop() + mLength2 + mRectText.height() + dp2px(30),
                getMeasuredWidth() - getPaddingRight() - mLength2 - mRectText.height() - dp2px(30),
                getMeasuredWidth() - getPaddingBottom() - mLength2 - mRectText.height() - dp2px(30)
        );

        mPLRadius = mRadius - dp2px(30);
        if (viewStyle == BigView){
            mPSRadius = dp2px(25);
        }else {
            mPSRadius = dp2px(13);
            mPLRadius += dp2px(6);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /**
         * 画圆弧
         */
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(mColor1);
        canvas.drawArc(mRectFArc, mStartAngle, mSweepAngle, false, mPaint);

        /**
         * 画长刻度
         * 画好起始角度的一条刻度后通过canvas绕着原点旋转来画剩下的长刻度
         */
        double cos = Math.cos(Math.toRadians(mStartAngle - 180));
        double sin = Math.sin(Math.toRadians(mStartAngle - 180));
        float x0 = (float) (mPadding + mStrokeWidth + mRadius * (1 - cos));
        float y0 = (float) (mPadding + mStrokeWidth + mRadius * (1 - sin));
        float x1 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1) * cos);
        float y1 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1) * sin);

        canvas.save();
        canvas.drawLine(x0, y0, x1, y1, mPaint);
        float angle = mSweepAngle * 1f / mSection;
        for (int i = 0; i < mSection; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            canvas.drawLine(x0, y0, x1, y1, mPaint);
        }
        canvas.restore();

        /**
         * 画短刻度 中间刻度
         * 同样采用canvas的旋转原理
         */
        canvas.save();
        mPaint.setStrokeWidth(mStrokeWidth / 2f);
        float x2 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - 2 * mLength1 / 3f) * cos);
        float y2 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - 2 * mLength1 / 3f) * sin);
        canvas.drawLine(x0, y0, x2, y2, mPaint);
        angle = mSweepAngle * 1f / (mSection * mPortion);
        for (int i = 1; i < mSection * mPortion; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            if (i % mPortion == 0) { // 避免与长刻度画重合
                continue;
            }
            canvas.drawLine(x0, y0, x2, y2, mPaint);
        }
        canvas.restore();

        /**
         * 画长刻度读数
         */
        if (viewStyle == BigView){
            mPaint.setTextSize(sp2px(16));
        }else {
            mPaint.setTextSize(sp2px(10));
        }
        mPaint.setStyle(Paint.Style.FILL);
        float α;
        float[] p;
        angle = mSweepAngle * 1f / mSection;
        for (int i = 0; i <= mSection; i++) {
            α = mStartAngle + angle * i;
            p = getCoordinatePoint(mRadius - mLength2, α);
            if (α % 360 > 135 && α % 360 < 225) {
                mPaint.setTextAlign(Paint.Align.LEFT);
            } else if ((α % 360 >= 0 && α % 360 < 45) || (α % 360 > 315 && α % 360 <= 360)) {
                mPaint.setTextAlign(Paint.Align.RIGHT);
            } else {
                mPaint.setTextAlign(Paint.Align.CENTER);
            }
            mPaint.getTextBounds(mHeaderText, 0, mTexts[i].length(), mRectText);
            int txtH = mRectText.height();
            if (i <= 1 || i >= mSection - 1) {
                canvas.drawText(mTexts[i], p[0], p[1] + txtH / 2, mPaint);
            } else if (i == 3) {
                canvas.drawText(mTexts[i], p[0] + txtH / 2, p[1] + txtH, mPaint);
            } else if (i == mSection - 3) {
                canvas.drawText(mTexts[i], p[0] - txtH / 2, p[1] + txtH, mPaint);
            } else {
                canvas.drawText(mTexts[i], p[0], p[1] + txtH, mPaint);
            }
        }

        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setShader(null);

        /**
         * 画表头 km/h
         * 没有表头就不画
         */
        if (!TextUtils.isEmpty(mHeaderText)) {
            if (viewStyle == BigView){
                mPaint.setTextSize(sp2px(16));
                mPaint.setTextAlign(Paint.Align.CENTER);
                mPaint.getTextBounds(mHeaderText, 0, mHeaderText.length(), mRectText);
                canvas.drawText(mHeaderText, mCenterX, mCenterY - mRectText.height() * 2, mPaint);
            }else {
                mPaint.setTextSize(sp2px(8));
                mPaint.setTextAlign(Paint.Align.CENTER);
                mPaint.getTextBounds(motorHeaderText, 0, mHeaderText.length(), mRectText);
                canvas.drawText(motorHeaderText, mCenterX, mCenterY - mRectText.height() * 2, mPaint);
            }
        }

        /**
         * 画指针
         */
        float θ = mStartAngle + mSweepAngle * (mVelocity - mMin) / (mMax - mMin); // 指针与水平线夹角
        mPaint.setColor(mColor2);
        int r = mRadius / 8;
        canvas.drawCircle(mCenterX, mCenterY, r, mPaint);
        mPaint.setStrokeWidth(r / 3);
        mPaint.setColor(mColor1);
        float[] p1 = getCoordinatePoint(mPLRadius, θ);
        canvas.drawLine(p1[0], p1[1], mCenterX, mCenterY, mPaint);

        //指针尾
        float[] p2 = getCoordinatePoint(mPSRadius, θ + 180);
        canvas.drawLine(mCenterX, mCenterY, p2[0], p2[1], mPaint);

        /**
         * 画实时度数值
         */
        if (viewStyle == BigView){
            mPaint.setColor(mColor1);
            mPaint.setStrokeWidth(dp2px(2));
            int xOffset = dp2px(22);
            if (mVelocity >= 100) {
                drawDigitalTube(canvas, mVelocity / 100, -xOffset);
                drawDigitalTube(canvas, (mVelocity - 100) / 10, 0);
                drawDigitalTube(canvas, mVelocity % 100 % 10, xOffset);
            } else if (mVelocity >= 10) {
                drawDigitalTube(canvas, -1, -xOffset);
                drawDigitalTube(canvas, mVelocity / 10, 0);
                drawDigitalTube(canvas, mVelocity % 10, xOffset);
            } else {
                drawDigitalTube(canvas, -1, -xOffset);
                drawDigitalTube(canvas, -1, 0);
                drawDigitalTube(canvas, mVelocity, xOffset);
            }
        }
    }

    /**
     * 数码管样式
     */
    //        1
    //      ——
    //   2 |    | 3
    //      —— 4
    //   5 |    | 6
    //      ——
    //        7
    private void drawDigitalTube(Canvas canvas, int num, int xOffset) {
        float x = mCenterX + xOffset;
        float y = mCenterY + dp2px(40);
        //数码管大小
        int lx = dp2px(3);
        int ly = dp2px(6);
        int gap = dp2px(2);

        // 1
        mPaint.setAlpha(num == -1 || num == 1 || num == 4 ? 25 : 255);
        canvas.drawLine(x - lx, y, x + lx, y, mPaint);
        // 2
        mPaint.setAlpha(num == -1 || num == 1 || num == 2 || num == 3 || num == 7 ? 25 : 255);
        canvas.drawLine(x - lx - gap, y + gap, x - lx - gap, y + gap + ly, mPaint);
        // 3
        mPaint.setAlpha(num == -1 || num == 5 || num == 6 ? 25 : 255);
        canvas.drawLine(x + lx + gap, y + gap, x + lx + gap, y + gap + ly, mPaint);
        // 4
        mPaint.setAlpha(num == -1 || num == 0 || num == 1 || num == 7 ? 25 : 255);
        canvas.drawLine(x - lx, y + gap * 2 + ly, x + lx, y + gap * 2 + ly, mPaint);
        // 5
        mPaint.setAlpha(num == -1 || num == 1 || num == 3 || num == 4 || num == 5 || num == 7
                || num == 9 ? 25 : 255);
        canvas.drawLine(x - lx - gap, y + gap * 3 + ly,
                x - lx - gap, y + gap * 3 + ly * 2, mPaint);
        // 6
        mPaint.setAlpha(num == -1 || num == 2 ? 25 : 255);
        canvas.drawLine(x + lx + gap, y + gap * 3 + ly,
                x + lx + gap, y + gap * 3 + ly * 2, mPaint);
        // 7
        mPaint.setAlpha(num == -1 || num == 1 || num == 4 || num == 7 ? 25 : 255);
        canvas.drawLine(x - lx, y + gap * 4 + ly * 2, x + lx, y + gap * 4 + ly * 2, mPaint);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }


    public float[] getCoordinatePoint(int radius, float angle) {
        float[] point = new float[2];

        double arcAngle = Math.toRadians(angle); //将角度转换为弧度
        if (angle < 90) {
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 90) {
            point[0] = mCenterX;
            point[1] = mCenterY + radius;
        } else if (angle > 90 && angle < 180) {
            arcAngle = Math.PI * (180 - angle) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 180) {
            point[0] = mCenterX - radius;
            point[1] = mCenterY;
        } else if (angle > 180 && angle < 270) {
            arcAngle = Math.PI * (angle - 180) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        } else if (angle == 270) {
            point[0] = mCenterX;
            point[1] = mCenterY - radius;
        } else {
            arcAngle = Math.PI * (360 - angle) / 180.0;
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        }

        return point;
    }

    public int getVelocity() {
        return mVelocity;
    }

    public void setVelocity(int velocity) {
        //值不变 或是超出数值范围 不作处理
        if (mVelocity == velocity || velocity < mMin || velocity > mMax) {
            return;
        }

        mVelocity = velocity;
        postInvalidate();
    }
}
