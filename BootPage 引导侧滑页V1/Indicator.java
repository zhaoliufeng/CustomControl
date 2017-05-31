package me.zhaoliufeng.myviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.LinearLayout;


public class Indicator extends LinearLayout {

    //背景圆画笔
    Paint roundCirclePaint = new Paint();
    //中心圆画笔
    Paint centralCirclePaint = new Paint();
    //绘制圆的数量
    int circleCount = 0;
    //当前所在的位置
    int currentPosition = 0;
    //背景圆半径
    float roundCircleRadius = 10.0f;
    //中心圆半径
    float centralCircleRadius = 7.0f;
    //圆间隔
    float interval = 50.0f;
    //正常状态下中心圆的颜色, 选中圆时中间圆的颜色
    private int centralCircleNormalColor, currentCircleColor;

    public Indicator(Context context) {
        super(context);
        setWillNotDraw(false);
        roundCirclePaint.setAntiAlias(true);
        centralCirclePaint.setAntiAlias(true);
    }

    public Indicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        roundCirclePaint.setAntiAlias(true);
        centralCirclePaint.setAntiAlias(true);
    }

    //设置数据圆个数
    void setCircleCount(int circleCount){
        this.circleCount = circleCount;
    }

    //设置当前的圆的位置
    void setSelectPosition(int currentPosition){
        this.currentPosition = currentPosition;
        invalidate();
    }

    //设置颜色
    void setColor(int roundCircleColor, int centralCircleNormalColor, int currentCircleColor){
        this.centralCircleNormalColor = centralCircleNormalColor;
        this.currentCircleColor = currentCircleColor;
        roundCirclePaint.setColor(roundCircleColor);
    }

    void setDistance(float r, float sr, float dis){
        this.roundCircleRadius = r;
        this.centralCircleRadius = sr;
        this.interval = dis;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //获取比例
        float mRelativePxInWidth = getWidth() / 720f;
        roundCircleRadius = roundCircleRadius * mRelativePxInWidth;
        centralCircleRadius = centralCircleRadius * mRelativePxInWidth;
        interval = interval * mRelativePxInWidth;
        //当圆数量是偶数时, 需要算上中间间隔的距离/2 首个圆起点X坐标 =  屏宽/2 - 圆所占的距离 - 间隔所占的距离
        if (circleCount%2 == 0){
            //获取第一个点的中心坐标 间隔50 半径20
            float fX = getWidth()/2 - circleCount*roundCircleRadius - (circleCount/2 - 1)*interval;
            for (int i = 0; i < circleCount; i++){
                if (currentPosition == i){
                    centralCirclePaint.setColor(currentCircleColor);
                }else {
                    centralCirclePaint.setColor(centralCircleNormalColor);
                }
                canvas.drawCircle(fX + i*(interval + 2*roundCircleRadius), getHeight()/2 , roundCircleRadius, roundCirclePaint);
                canvas.drawCircle(fX + i*(interval + 2*roundCircleRadius), getHeight()/2, centralCircleRadius, centralCirclePaint);
            }
        }else {//当圆数量是奇数时, 需要算上中间圆的半径
            float fX = getWidth()/2 - (circleCount-1)*roundCircleRadius - ((circleCount-1)/2 - 1)*interval - 2*roundCircleRadius;
            for (int i = 0; i < circleCount; i++){
                if (currentPosition == i){
                    centralCirclePaint.setColor(currentCircleColor);
                }else {
                    centralCirclePaint.setColor(centralCircleNormalColor);
                }
                canvas.drawCircle(fX + i*(interval + 2*roundCircleRadius), getHeight()/2 , roundCircleRadius, roundCirclePaint);
                canvas.drawCircle(fX + i*(interval + 2*roundCircleRadius), getHeight()/2, centralCircleRadius, centralCirclePaint);
            }
        }

    }
}
