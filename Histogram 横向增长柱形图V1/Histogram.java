package me.zhaoliufeng.vehiclemanagesystem.View.Control;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import me.zhaoliufeng.vehiclemanagesystem.R;

public class Histogram extends View {

    private Paint mPaint;
    //间隔宽度
    private float spetaWidth;
    //字间隔
    private float wordSpeta;
    //视图右移距离
    private float rightMoveSpeta;
    //最大值
    private int maxNumber;
    //最大的柱状图长度
    private static int maxLength = 400;
    //动画速度
    private int animationSpeed;
    //数据源
    private int[] dataSource = new int[0];
    //柱形图标题
    private String[] titles;
    //最大值的下标
    private int maxFlag;
    //竖线上下间距
    private int letRoundRectSpeta;
    //每个柱状图的最大长度
    private int[] rectLength;

    public Histogram(Context context) {
        super(context);
        initView();
    }

    public Histogram(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.Histogram);

        //读取界面配置参数
        spetaWidth = arr.getFloat(R.styleable.Histogram_histogramSpetaWidth, 30);
        wordSpeta = arr.getFloat(R.styleable.Histogram_histogramWordSpeta, 30);
        rightMoveSpeta = arr.getFloat(R.styleable.Histogram_histogramRightMoveSpeta,50);
        animationSpeed = arr.getInt(R.styleable.Histogram_histogram_animation_speed, 20);
    }

    private void initView(){
        //初始化画笔
        mPaint = new Paint();
        //设置抗锯齿
        mPaint.setAntiAlias(true);
        //设置圆角笔触风格
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        letRoundRectSpeta = 20;
    }

    public void setHistogramData(int[] dataSource, String[] titles){
        //判断每个数据是否都有标题
        if (!(dataSource.length == titles.length)){
            throw new RuntimeException("数据标题数量不相符, 请检查你的源数据，源标题");
        }

        //判断 找出数组中最大的值
        for (int i = 0; i < dataSource.length; i++){
            if (maxNumber < dataSource[i]){
                maxNumber = dataSource[i];
                maxFlag = i;
            }
        }
        //根据屏幕宽度计算最大长度 = 屏宽 * 0.6
        maxLength = (int)(getWidth()*0.6);

        this.titles = titles;
        this.dataSource = dataSource;
        //计算每个柱状图的最大长度
        this.rectLength = new int[dataSource.length];
        for (int i = 0; i < dataSource.length; i++){
            rectLength[i] = (int)((dataSource[i]/(float)maxNumber) * maxLength);
        }

        //计算所需柱状图数量
        int sn = dataSource.length*2 - 1;
        spetaWidth = (getHeight() - 2*(letRoundRectSpeta+15))/sn; //上下各自留出20 + 15的区域
        Log.i("Histogram 初始化位置信息", "柱状图之间的距离 " + spetaWidth + "\n单个柱状图高度 " + getHeight() + "\n所需柱状间隔数量 " + sn);
        Log.i("Histogram", "柱状图数据加载完成, 执行动画");

        startAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //左侧竖线宽度
        int letRoundRectWidth = 7;
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_light));

        //绘制左边竖线
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(rightMoveSpeta, letRoundRectSpeta, rightMoveSpeta + letRoundRectWidth, getHeight() - letRoundRectSpeta, 10, 10, mPaint);
        }else {
            canvas.drawRoundRect(new RectF(rightMoveSpeta, letRoundRectSpeta, rightMoveSpeta + letRoundRectWidth, getHeight() - letRoundRectSpeta), 10, 10, mPaint);
        }

        //计算最上层的柱形图y轴起点
        float topY;
        //如果是偶数
        if (dataSource.length%2 == 0){
            topY = getHeight()/2 - (spetaWidth/2 + (dataSource.length/2)*spetaWidth + ((dataSource.length/2)-1)*spetaWidth);
        }else {
            topY = getHeight()/2 - (spetaWidth/2 + (dataSource.length-1)/2*spetaWidth + (dataSource.length-1)/2*spetaWidth);
        }
        for (int i = 0; i < dataSource.length; i++){
            //绘制车辆在线数量柱形图
            canvas.drawRect(rightMoveSpeta, topY+(i*(2*spetaWidth)), rectLength[i] + rightMoveSpeta, topY+(i*(2*spetaWidth))+spetaWidth, mPaint);
            //绘制文字
            mPaint.setTextSize(25);
            String text = titles[i];
            canvas.drawText(text, rectLength[i] + rightMoveSpeta + wordSpeta, topY+(i*(2*spetaWidth))+spetaWidth - 5, mPaint);
            String number = String.valueOf(dataSource[i]);
            canvas.drawText(number, rectLength[i] + rightMoveSpeta + wordSpeta*2 + 140, topY+(i*(2*spetaWidth))+spetaWidth - 5, mPaint);
        }
    }

    private int temp[];
    //是否正在播放动画标识变量
    private boolean isPayingAnimation = false;
    private void startAnimation(){
        //找到数组中的最大值 用于判断动画是否结束
        temp = new int[dataSource.length];
        for (int i = 0; i < dataSource.length; i++){
            //复制一份数据
            temp[i] = rectLength[i];
            //清零数据源
            rectLength[i] = 0;
        }
        //开启新线程执行动画
        new Thread(new Runnable() {
            @Override
            public void run() {
                    while (rectLength[maxFlag] < maxLength){
                        try {
                            Thread.sleep(animationSpeed);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!isPayingAnimation){
                            //handler的发送数据延迟,延迟越高动画越慢
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
            //更新数据并刷新界面
            for (int i = 0; i < dataSource.length; i++){
                if (rectLength[i] < temp[i]){
                    rectLength[i]+=10;
                }
            }
            isPayingAnimation = false;
        }
    };
}
