package me.zhaoliufeng.vehiclemanagesystem.View.Control;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import me.zhaoliufeng.vehiclemanagesystem.R;


public class LineChart extends View{

    private float[] data;

    private Paint linePaint; // 轴线画笔

    private Paint recPaint; // 绘制柱状图阴影背景的画笔

    private Paint dataPaint; // 绘制数据柱状图的画笔

    private Paint textPaint; // 绘制坐标系的画笔

    private String[] xTitleString ; // x轴刻度
    private String[] yTitleString; // y轴刻度
    //动画速度
    private int animationSpeed;

    private int num = -1; // 画多少条柱子，因为存在刚开机数据不足24条的情况

    public LineChart(Context context) {
        super(context);
    }

    public LineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.LineChart);

        //读取界面配置参数
        animationSpeed = arr.getInt(R.styleable.LineChart_linechart_animation_speed, 20);
    }

    private void init() {
        linePaint = new Paint();
        recPaint = new Paint();
        dataPaint = new Paint();
        textPaint = new Paint();

        linePaint.setColor(Color.parseColor("#dbdde4"));  //设置坐标轴的颜色为白色
        recPaint.setColor(Color.parseColor("#f2f5fc"));
        dataPaint.setColor(getResources().getColor(R.color.colorAccent));
        textPaint.setColor(Color.parseColor("#000000"));
        textPaint.setTextSize(9);

        linePaint.setAntiAlias(true);
        recPaint.setAntiAlias(true);
        dataPaint.setAntiAlias(true);
        textPaint.setAntiAlias(true);
    }

    public void setData(float[] data, String[] xTitle, String[] yTitle) {
        this.num = xTitle.length;
        this.data = data;
        this.xTitleString = xTitle;
        this.yTitleString = yTitle;
        startAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(this.data == null || this.xTitleString == null || this.num < 0 ) {
            return;
        }
        //////////////////////////////测量校准数值/////////////////////////////////
        //根据原型图得出，图中每px高度在实际中的相对尺寸
        float mRelativePxInHeight = getHeight() / 470f;
        //根据原型图得出，图中每px宽度在实际中的相对尺寸
        float mRelativePxInWidth = getWidth() / 670f;
        //计算柱之间的间隔
        //最左侧位置100 * mRelativePxInWidth,最右侧位置630 ePxInWidth,
        float totalWidth = 630 - 50;
        // 柱子与之子之间的间隔
        int mDist = (int) (totalWidth / (xTitleString.length + 1));
        //测量字体
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        //获得下方高度
        int decent = (int) metrics.descent;

        //////////////////////////////开始绘制/////////////////////////////////
        //开始绘制y轴刻度
        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTextSize(15*mRelativePxInWidth);

        //绘制y纵坐标
        for (int i = 0; i < yTitleString.length; i++) {
            canvas.drawText(yTitleString[i], 44 * mRelativePxInWidth, (42 + i * 78) * mRelativePxInHeight + decent, textPaint);
            canvas.drawLine(50 * mRelativePxInWidth, (42 + i * 78) * mRelativePxInHeight + decent, 630 * mRelativePxInWidth, (42 + i * 78) * mRelativePxInHeight + decent, linePaint);
        }

        //开始绘制x轴刻度
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(10*mRelativePxInWidth);

        for (int i = 0; i < xTitleString.length; i++) {
            //绘制白色刻度竖线
            canvas.drawLine((50 + (i+1) * mDist) * mRelativePxInWidth, (42 + (yTitleString.length - 1) * 78) * mRelativePxInHeight + decent, (50 + (i+1) * mDist) * mRelativePxInWidth, (42 + (yTitleString.length - 1) * 78 + 5) * mRelativePxInHeight + decent, linePaint);
            //绘制x轴刻度文字
            canvas.drawText(xTitleString[i], (50 + (i+1) * mDist) * mRelativePxInWidth, (42 + (yTitleString.length - 1) * 78 + 15) * mRelativePxInHeight + decent, textPaint);
        }

        //  绘制暗色背景圆角矩形
        for (int i = 0; i < num; i++) {
            RectF rectF = new RectF();
            rectF.left = 45 * mRelativePxInWidth + (i+1) * mDist * mRelativePxInWidth;
            rectF.right = 55 * mRelativePxInWidth +(i+1) * mDist * mRelativePxInWidth;
            rectF.top = 49 * mRelativePxInHeight ;
            rectF.bottom = (42 + (yTitleString.length - 1) * 78) * mRelativePxInHeight + decent ;
            canvas.drawRoundRect(rectF, 10, 10, recPaint);
        }

        for (int i = 0; i < num; i++) {
            RectF rectF = new RectF();
            float value = data[i];
            //高度百分比
            float hf = 1.0f - value/100.0f;
            rectF.left = 45 * mRelativePxInWidth + (i+1) * mDist * mRelativePxInWidth;
            rectF.right = 55 * mRelativePxInWidth +(i+1) * mDist * mRelativePxInWidth;
            rectF.bottom = (42 + (yTitleString.length - 1) * 78) * mRelativePxInHeight + decent ;
            rectF.top = 49 * mRelativePxInHeight;
            //计算data柱的高度 = bottom - (top + (bottom-top))*高度百分比
            rectF.top = rectF.top + (rectF.bottom - rectF.top)*hf;
            canvas.drawRoundRect(rectF, 10, 10, dataPaint);
        }
    }

    //最大值的下标
    int flag = 0;
    float maxValue = 0;
    float temp[];
    private boolean isPayingAnimation = false;

    private void startAnimation(){
        //找到数组中的最大值 用于判断动画是否结束
        temp = new float[num];
        for (int i = 0; i < num; i++){
            if (data[i] > maxValue){
                maxValue = data[i];
                flag = i;
            }
            //复制一份数据
            temp[i] = data[i];
            //清零数据源
            data[i] = 0;
        }
        Log.i("LineChart", "maxValue -> " + maxValue);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (data[flag] < maxValue){
                    try {
                        Thread.sleep(animationSpeed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!isPayingAnimation){
                        handler.sendEmptyMessageDelayed(0, animationSpeed);
                        isPayingAnimation = true;
                    }
                }
            }
        }).start();
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            invalidate();
            for (int i = 0; i<num; i++){
                if (data[i] < temp[i]){
                    data[i]++;
                }
            }
            isPayingAnimation = false;
        }
    };
}
