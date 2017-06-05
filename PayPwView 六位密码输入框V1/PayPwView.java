package me.zhaoliufeng.myviews;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**   ┏┓　　　┏┓
    ┏┛┻━━━┛┻┓
    ┃　　　　　　　┃
    ┃　　　━　　　┃
    ┃　┳┛　┗┳　┃
    ┃　　　　　　　┃
    ┃　　　┻　　　┃
    ┃　　　　　　　┃
    ┗━┓　　　┏━┛
        ┃　　　┃   神兽保佑
        ┃　　　┃   代码无BUG！
        ┃　　　┗━━━┓
        ┃　　　　　　　┣┓
        ┃　　　　　　　┏┛
        ┗┓┓┏━┳┓┏┛
          ┃┫┫　┃┫┫
          ┗┻┛　┗┻┛  **/

public class PayPwView extends EditText implements View.OnFocusChangeListener {

    private Paint mLinePaint;    //线画笔
    private Paint mCirlePaint;   //圆画笔
    private Paint mBorderPaint;  //边框画笔
    private Paint mRectPaint;   //当前输入边框画笔
    private boolean mIsDel;  //是否是删除操作
    private int mPassWordLength;    //已输入的密码长度
    private float mPCurrX;     //密码输入框当前位置
    private float mPartWidth;   //一块区域的宽
    private int mWidth;
    private int mHeight;
    private int mPwNumber; //密码最大长度
    private OnFinishInputListener mOnFinishInputListener;

    private static final int PASSWORD_CIRLE_RADIUS = 5; //密码圆半径
    private static final int DEVIATION_VALUE = 2;   //偏移的值

    public PayPwView(Context context) {
        super(context);
        initView();
    }

    public PayPwView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.PayPwView);

        //读取界面配置参数
        mPwNumber = arr.getInt(R.styleable.PayPwView_pw_num, 6);
    }

    //初始化视图
    private void initView(){
        setFocusable(true);
        setCursorVisible(false);
        //初始化画笔
        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.FILL);
        mBorderPaint.setColor(Color.WHITE);

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.parseColor("#EEEEEE"));
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(4);

        mCirlePaint = new Paint();
        mCirlePaint.setColor(Color.BLACK);
        mCirlePaint.setAntiAlias(true);
        mCirlePaint.setStrokeWidth(4);

        mRectPaint = new Paint();
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setColor(Color.parseColor("#00A0E9"));
        mRectPaint.setAntiAlias(true);
        mRectPaint.setStrokeWidth(5);

        this.setOnFocusChangeListener(this);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        //绘制边框
        RectF rectF = new RectF();
        rectF.top = 0;
        rectF.bottom = mHeight;
        rectF.left = DEVIATION_VALUE;
        rectF.right = mWidth - DEVIATION_VALUE;
        canvas.drawRoundRect(rectF, 10, 10, mBorderPaint);

        //绘制竖线
        //获取一份区间的间隔
        mPartWidth = mWidth / mPwNumber;
        for (int i = 1; i < mPwNumber ; i++){
            canvas.drawLine(mPartWidth * i, DEVIATION_VALUE, mPartWidth * i, mHeight - DEVIATION_VALUE, mLinePaint);
        }

        //绘制圆
        for (int i = 0; i < mPassWordLength; i++){
            canvas.drawCircle(mPartWidth/2 + mPartWidth * i, mHeight/2, PASSWORD_CIRLE_RADIUS, mCirlePaint);
        }

        drawRect(canvas, mPartWidth);
    }

    private void drawRect(Canvas canvas, float partWidth){
        //绘制边框
        RectF rectF = new RectF();
        rectF.top = DEVIATION_VALUE;
        rectF.bottom = mHeight - DEVIATION_VALUE;

        rectF.left = mPCurrX + DEVIATION_VALUE;
        rectF.right = mPCurrX + partWidth - DEVIATION_VALUE;
        canvas.drawRoundRect(rectF, 10, 10, mRectPaint);
    }

    private void moveToNext(){
        if (mIsDel){
            //当输入框中有6位密码 做删减后 长度即为5 此时密码输入提示框位置不变
            if (mPassWordLength <  mPwNumber - 1){
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(mPassWordLength + 1, mPassWordLength).setDuration(200);
                valueAnimator.addUpdateListener(animation -> {
                    mPCurrX = (float) animation.getAnimatedValue() * mPartWidth;
                    invalidate();
                });
                valueAnimator.start();
            }
        }else {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(mPassWordLength - 1, mPassWordLength).setDuration(200);
            valueAnimator.addUpdateListener(animation -> {
                mPCurrX = (float) animation.getAnimatedValue() * mPartWidth;
                invalidate();
            });
            valueAnimator.start();
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (text.toString().length() <= mPwNumber){
            mIsDel = lengthBefore >= lengthAfter;
            mPassWordLength = text.toString().length();
            if (text.toString().length() == mPwNumber){
                if (this.mOnFinishInputListener != null){
                    this.mOnFinishInputListener.onFinishInput(text.toString());
                }
                return;
            }
            super.onTextChanged(text, start, lengthBefore, lengthAfter);
            moveToNext();
            invalidate();
        }else{
            this.setText(text.toString().substring(0, mPwNumber));
            //重新设置文本后 光标会移动到起始位置 需要重设光标位置
            this.setSelection(mPwNumber);
        }
    }

    public void setPwNumber(int num){
        this.mPwNumber = num;
    }

    public void setOnFinishInputListener(OnFinishInputListener onFinishInputListener){
        this.mOnFinishInputListener = onFinishInputListener;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus){
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(300);
            valueAnimator.addUpdateListener(animation -> {
                mRectPaint.setAlpha((int)((float)valueAnimator.getAnimatedValue() * 255));
                invalidate();
            });
            valueAnimator.start();
        }else {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(1, 0).setDuration(300);
            valueAnimator.addUpdateListener(animation -> {
                mRectPaint.setAlpha((int)((float)valueAnimator.getAnimatedValue() * 255));
                invalidate();
            });
            valueAnimator.start();
        }
    }

    public interface OnFinishInputListener{
        void onFinishInput(String pw);
    }
}
