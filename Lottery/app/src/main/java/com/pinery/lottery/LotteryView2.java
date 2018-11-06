package com.pinery.lottery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LotteryView2 extends RelativeLayout {

    /**
     * 用于回调动画过程和结果
     */
    public interface RotateListener {

        /**
         * 动画结束 返回当前位置 注意 位置是最上面是1 然后依次逆时针递增
         *
         * @param position
         * @param des      所指分区文字描述
         */
        void rotateEnd(int position, String des);

        /**
         * 动画进行中 返回动画中间量
         *
         * @param valueAnimator
         */
        void rotating(ValueAnimator valueAnimator);

        /**
         * 点击了按钮 但是没有旋转 调用者可以在这里处理一些逻辑 比如弹出对话框确定用户是否要抽奖
         *
         * @param goImg
         */
        void rotateBefore(ImageView goImg);
    }


    //当前的圆盘VIew
    private WheelSurfPanView mWheelSurfPanView;
    private OutPanView mOutPanView;
    //Context
    private Context mContext;
    //开始按钮
    private ImageView mStart;
    //动画回调监听
    private RotateListener rotateListener;

    public void setRotateListener(RotateListener rotateListener) {
        mWheelSurfPanView.setRotateListener(rotateListener);
        this.rotateListener = rotateListener;
    }

    public LotteryView2(Context context) {
        super(context);
        init(context, null);
    }

    public LotteryView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LotteryView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    //开始抽奖的图标
    private Integer mGoImgRes;

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        if (attrs != null) {
            //获得这个控件对应的属性。
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LotteryView);
            try {
                mGoImgRes = typedArray.getResourceId(R.styleable.LotteryView_goImg, 0);
            } finally { //回收这个对象
                typedArray.recycle();
            }
        }

        //添加圆盘视图
        mOutPanView = new OutPanView(mContext);
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mOutPanView.setLayoutParams(layoutParams);
        addView(mOutPanView);

        //添加圆盘视图
        mWheelSurfPanView = new WheelSurfPanView(mContext, attrs);
        layoutParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mWheelSurfPanView.setLayoutParams(layoutParams);
        addView(mWheelSurfPanView);

        //添加开始按钮
        mStart = new ImageView(mContext);
        //如果用户没有设置自定义的图标就使用默认的
        if (mGoImgRes == 0) {
            mStart.setImageResource(R.drawable.lottery_node);
        } else {
            mStart.setImageResource(mGoImgRes);
        }
        //给图片设置LayoutParams
        RelativeLayout.LayoutParams llStart =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llStart.addRule(RelativeLayout.CENTER_IN_PARENT);
        mStart.setLayoutParams(llStart);
        addView(mStart);

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //调用此方法是将主动权交个调用者 由调用者调用开始旋转的方法
                if (rotateListener != null)
                    rotateListener.rotateBefore((ImageView) v);
            }
        });
    }

    public void setConfig(Builder builder) {
        if (builder.mColors != null)
            mWheelSurfPanView.setmColors(builder.mColors);
        if (builder.mDeses != null)
            mWheelSurfPanView.setmDeses(builder.mDeses);
        if (builder.mHuanImgRes != 0)
            mWheelSurfPanView.setmHuanImgRes(builder.mHuanImgRes);
        if (builder.mIcons != null)
            mWheelSurfPanView.setmIcons(builder.mIcons);
        if (builder.mMainImgRes != 0)
            mWheelSurfPanView.setmMainImgRes(builder.mMainImgRes);
        if (builder.mMinTimes != 0)
            mWheelSurfPanView.setmMinTimes(builder.mMinTimes);
        if (builder.mTextColor != 0)
            mWheelSurfPanView.setmTextColor(builder.mTextColor);
        if (builder.mTextSize != 0)
            mWheelSurfPanView.setmTextSize(builder.mTextSize);
        if (builder.mType != 0)
            mWheelSurfPanView.setmType(builder.mType);
        if (builder.mVarTime != 0)
            mWheelSurfPanView.setmVarTime(builder.mVarTime);
        if (builder.mTypeNum != 0)
            mWheelSurfPanView.setmTypeNum(builder.mTypeNum);
        mWheelSurfPanView.show();
    }

    /**
     * 开始旋转
     *
     * @param pisition 旋转最终的位置 注意 从1 开始 而且是逆时针递增
     */
    public void startRotate(int pisition) {
//        if (mWheelSurfPanView != null) {
//            mWheelSurfPanView.startRotate(pisition);
//        }
//        if (mOutPanView != null) {
//            mOutPanView.startLuckLight();
//        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //视图是个正方形的 所以有宽就足够了 默认值是500 也就是WRAP_CONTENT的时候
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        // Children are just made to fill our space.
        final int childWidthSize = getMeasuredWidth();
        //高度和宽度一样
        heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);

        //onMeasure调用获取到当前视图大小之后，
        // 手动按照一定的比例计算出中间开始按钮的大小，
        // 再设置给那个按钮，免得造成用户传的图片不合适之后显示贼难看
        // 只设置一次
        if (isFirst) {
            isFirst = !isFirst;
            //获取中间按钮的大小
            ViewTreeObserver vto = mStart.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @TargetApi(Build.VERSION_CODES.KITKAT)
                @Override
                public void onGlobalLayout() {
                    mStart.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    float w = mStart.getMeasuredWidth();
                    float h = mStart.getMeasuredHeight();
                    //计算新的大小 默认为整个大小最大值的0.17 至于为什么是0.17  我只想说我乐意。。。。
                    int newW = (int) (((float) childWidthSize) * 0.17);
                    int newH = (int) (((float) childWidthSize) * 0.17 * h / w);
                    ViewGroup.LayoutParams layoutParams = mStart.getLayoutParams();
                    layoutParams.width = newW;
                    layoutParams.height = newH;
                    mStart.setLayoutParams(layoutParams);
                }
            });
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //记录当前是否是第一次回调onMeasure
    private boolean isFirst = true;


    //建造者模式
    public static final class Builder {
        //当前类型 1 自定义模式 2 暴力模式
        private int mType = 0;
        //最低圈数 默认值3 也就是说每次旋转都会最少转3圈
        private int mMinTimes = 0;
        //分类数量 如果数量为负数  通过代码设置样式
        private int mTypeNum = 0;
        //每个扇形旋转的时间
        private int mVarTime = 0;
        //文字描述集合
        private String[] mDeses;
        //自定义图标集合
        private List<Bitmap> mIcons;
        //背景颜色
        private Integer[] mColors;
        //整个旋转图的背景 只有类型为2时才需要
        private Integer mMainImgRes = 0;
        //GO图标
        private Integer mGoImgRes = 0;
        //圆环的图片引用
        private Integer mHuanImgRes = 0;
        //文字大小
        private float mTextSize = 0;
        //文字颜色
        private int mTextColor = 0;

        public final LotteryView2.Builder setmType(int mType) {
            this.mType = mType;
            return this;
        }

        public final LotteryView2.Builder setmTypeNum(int mTypeNum) {
            this.mTypeNum = mTypeNum;
            return this;
        }

        public final LotteryView2.Builder setmGoImgRes(int mGoImgRes) {
            this.mGoImgRes = mGoImgRes;
            return this;
        }

        public final LotteryView2.Builder setmMinTimes(int mMinTimes) {
            this.mMinTimes = mMinTimes;
            return this;
        }

        public final LotteryView2.Builder setmVarTime(int mVarTime) {
            this.mVarTime = mVarTime;
            return this;
        }

        public final LotteryView2.Builder setmDeses(String[] mDeses) {
            this.mDeses = mDeses;
            return this;
        }

        public final LotteryView2.Builder setmIcons(List<Bitmap> mIcons) {
            this.mIcons = mIcons;
            return this;
        }

        public final LotteryView2.Builder setmColors(Integer[] mColors) {
            this.mColors = mColors;
            return this;
        }

        public final LotteryView2.Builder setmMainImgRes(Integer mMainImgRes) {
            this.mMainImgRes = mMainImgRes;
            return this;
        }

        public final LotteryView2.Builder setmHuanImgRes(Integer mHuanImgRes) {
            this.mHuanImgRes = mHuanImgRes;
            return this;
        }

        public final LotteryView2.Builder setmTextSize(float mTextSize) {
            this.mTextSize = mTextSize;
            return this;
        }

        public final LotteryView2.Builder setmTextColor(int mTextColor) {
            this.mTextColor = mTextColor;
            return this;
        }

        public final Builder build() {
            return this;
        }
    }

    public class OutPanView extends View {
        private boolean isYellow = false;
        private Paint smallCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int radius;
        int CircleX;
        int CircleY;
        private int delayTime = 500;

        public OutPanView(Context context) {
            super(context);

            backgroundPaint.setColor(Color.rgb(255, 92, 93));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            drawOuterCircle(canvas);
        }

        private void drawOuterCircle(Canvas canvas) {
            final int paddingLeft = getPaddingLeft();
            final int paddingRight = getPaddingRight();
            final int paddingTop = getPaddingTop();
            final int paddingBottom = getPaddingBottom();

            int width = getWidth() - paddingLeft - paddingRight;
            int height = getHeight() - paddingTop - paddingBottom;

            int MinValue = Math.min(width, height);

            radius = MinValue / 2;
            CircleX = getWidth() / 2;
            CircleY = getHeight() / 2;

            canvas.drawCircle(CircleX, CircleY, radius, backgroundPaint);

            drawSmallCircle(canvas, isYellow);
        }

        private void drawSmallCircle(Canvas canvas, boolean FirstYellow) {
            int pointDistance = (radius + mWheelSurfPanView.mRadius)/2;
            for (int i = 0; i <= 360; i += 20) {
                int x = (int) (pointDistance * Math.sin(change(i))) + CircleX;
                int y = (int) (pointDistance * Math.cos(change(i))) + CircleY;

                if (FirstYellow) {
                    smallCirclePaint.setColor(Color.YELLOW);
                    canvas.drawCircle(x, y, dp2px(4), smallCirclePaint);
                } else {
                    smallCirclePaint.setColor(Color.WHITE);
                    canvas.drawCircle(x, y, dp2px(4), smallCirclePaint);
                }
                FirstYellow = !FirstYellow;
            }
        }

        /**
         * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
         * @param dpValue 虚拟像素
         * @return 像素
         */
        private int dp2px(float dpValue) {
            return (int) (0.5f + dpValue * getContext().getResources().getDisplayMetrics().density);
        }


        private double change(double a) {
            return a * Math.PI / 180;
        }

        private void startLuckLight() {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    isYellow = !isYellow;
                    invalidate();

                    if(mWheelSurfPanView.isRotating){
                        postDelayed(this, delayTime);
                    }
                }
            }, delayTime);
        }
    }

    private class WheelSurfPanView extends View {
        private boolean isRotating;

        private Context mContext;
        //记录视图的大小
        private int mWidth;
        //记录当前有几个分类
        private Paint mPaint;
        //文字画笔
        private Paint mTextPaint;
        //圆环图片
        private Bitmap mYuanHuan;
        //大图片
        private Bitmap mMain;

        //中心点横坐标
        private int mCenter;
        //绘制扇形的半径 减掉50是为了防止边界溢出  具体效果你自己注释掉-50自己测试
        private int mRadius;
        //每一个扇形的角度
        private float mAngle;

        private List<Bitmap> mListBitmap;

        //动画回调监听
        private RotateListener rotateListener;

        public RotateListener getRotateListener() {
            return rotateListener;
        }

        public void setRotateListener(RotateListener rotateListener) {
            this.rotateListener = rotateListener;
        }

        public WheelSurfPanView(Context context) {
            super(context);
            init(context, null);
        }

        public WheelSurfPanView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context, attrs);
        }

        public WheelSurfPanView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init(context, attrs);
        }

        //当前类型 1 自定义模式 2 暴力模式
        private int mType;
        //最低圈数 默认值3 也就是说每次旋转都会最少转3圈
        private int mMinTimes;
        //分类数量 如果数量为负数  通过代码设置样式
        private int mTypeNum = 6;
        //每个扇形旋转的时间
        private int mVarTime = 75;
        //文字描述集合
        private String[] mDeses;
        //自定义图标集合
        private Integer[] mIcons;
        //背景颜色
        private Integer[] mColors;
        //整个旋转图的背景 只有类型为2时才需要
        private Integer mMainImgRes;
        //圆环的图片引用
        private Integer mHuanImgRes;
        //文字大小
        private float mTextSize;
        //文字颜色
        private int mTextColor;

        public void setmType(int mType) {
            this.mType = mType;
        }

        public void setmMinTimes(int mMinTimes) {
            this.mMinTimes = mMinTimes;
        }

        public void setmVarTime(int mVarTime) {
            this.mVarTime = mVarTime;
        }

        public void setmTypeNum(int mTypeNum) {
            this.mTypeNum = mTypeNum;
        }

        public void setmDeses(String[] mDeses) {
            this.mDeses = mDeses;
        }

        public void setmIcons(List<Bitmap> mIcons) {
            this.mListBitmap = mIcons;
        }

        public void setmColors(Integer[] mColors) {
            this.mColors = mColors;
        }

        public void setmMainImgRes(Integer mMainImgRes) {
            this.mMainImgRes = mMainImgRes;
        }

        public void setmHuanImgRes(Integer mHuanImgRes) {
            this.mHuanImgRes = mHuanImgRes;
        }

        public void setmTextSize(float mTextSize) {
            this.mTextSize = mTextSize;
        }

        public void setmTextColor(int mTextColor) {
            this.mTextColor = mTextColor;
        }

        private void init(Context context, AttributeSet attrs) {
            mContext = context;
            setBackgroundColor(Color.TRANSPARENT);

            if (attrs != null) {
                //获得这个控件对应的属性。
                TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LotteryView);
                try {
                    mType = typedArray.getInteger(R.styleable.LotteryView_types, 1);
                    mVarTime = typedArray.getInteger(R.styleable.LotteryView_vartime, 0);
                    mMinTimes = typedArray.getInteger(R.styleable.LotteryView_minTimes, 3);
                    mTypeNum = typedArray.getInteger(R.styleable.LotteryView_typenum, 0);

                    if (mTypeNum == -1) {
                        //用代码去配置这些参数
                    } else {
                        if (mVarTime == 0)
                            mVarTime = 75;

                        if (mTypeNum == 0)
                            throw new RuntimeException("找不到分类数量mTypeNum");

                        //每一个扇形的角度
                        mAngle = (float) (360.0 / mTypeNum);

                        if (mType == 1) {
//                            mHuanImgRes = typedArray.getResourceId(R.styleable.wheelSurfView_huanImg, 0);
//                            if ( mHuanImgRes == 0 )
//                                mYuanHuan = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.yuanhuan);
//                            else {
//                                mYuanHuan = BitmapFactory.decodeResource(mContext.getResources(), mHuanImgRes);
//                            }

                            //文字大小
                            mTextSize = typedArray.getDimension(R.styleable.LotteryView_textSize, 14 * getScale());
                            //文字颜色 默认粉红色
                            mTextColor = typedArray.getColor(R.styleable.LotteryView_textColor, Color.parseColor("#ff00ff"));

                            //描述
                            int nameArray = typedArray.getResourceId(R.styleable.LotteryView_deses, -1);
                            //if (nameArray == -1) throw new RuntimeException("找不到描述");
                            if(nameArray > 0){
                                mDeses = context.getResources().getStringArray(nameArray);
                            }
                            //图片
                            int iconArray = typedArray.getResourceId(R.styleable.LotteryView_icons, -1);
                            if (iconArray == -1) throw new RuntimeException("找不到分类的图片资源");
                            String[] iconStrs = context.getResources().getStringArray(iconArray);
                            List<Integer> iconLists = new ArrayList<>();
                            for (int i = 0; i < iconStrs.length; i++) {
                                iconLists.add(context.getResources().getIdentifier(iconStrs[i], "drawable", context.getPackageName()));
                            }
                            mIcons = iconLists.toArray(new Integer[iconLists.size()]);
                            //颜色
                            int colorArray = typedArray.getResourceId(R.styleable.LotteryView_colors, -1);
                            if (colorArray == -1) throw new RuntimeException("找不到背景颜色");
                            String[] colorStrs = context.getResources().getStringArray(colorArray);
                            if (mIcons == null || colorStrs == null)
                                throw new RuntimeException("找不到描述或图片或背景颜色资源");
                            if (mIcons.length != mTypeNum || colorStrs.length != mTypeNum)
                                throw new RuntimeException("资源或描述或背景颜色的长度和mTypeNum不一致");
                            mColors = new Integer[mTypeNum];
                            //分析背景颜色
                            for (int i = 0; i < colorStrs.length; i++) {
                                try {
                                    mColors[i] = Color.parseColor(colorStrs[i]);
                                } catch (Exception e) {
                                    throw new RuntimeException("颜色值有误");
                                }
                            }
                            //加载分类图片 存放图片的集合
                            mListBitmap = new ArrayList<>();
                            for (int i = 0; i < mTypeNum; i++) {
                                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), mIcons[i]);
                                int ww = bitmap.getWidth();
                                int hh = bitmap.getHeight();
                                // 定义矩阵对象
                                Matrix matrix = new Matrix();
                                // 缩放原图
                                matrix.postScale(1f, 1f);
                                // 向左旋转45度，参数为正则向右旋转
                                matrix.postRotate(mAngle * i);
                                //bmp.getWidth(), 500分别表示重绘后的位图宽高
                                Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, ww, hh,
                                        matrix, true);
                                mListBitmap.add(dstbmp);
                            }
                            //文字画笔
                            mTextPaint = new Paint();
                            //设置填充样式
                            mTextPaint.setStyle(Paint.Style.STROKE);
                            //设置抗锯齿
                            mTextPaint.setAntiAlias(true);
                            //设置边界模糊
                            mTextPaint.setDither(true);
                            //设置画笔颜色
                            mTextPaint.setColor(mTextColor);
                            //设置字体大小
                            mTextPaint.setTextSize(mTextSize);
                        } else if (mType == 2) {
                            mMainImgRes = typedArray.getResourceId(R.styleable.LotteryView_mainImg, 0);
                            //直接大图
                            if (mMainImgRes == 0)
                                throw new RuntimeException("类型为2必须要传大图mMainImgRes");
                            mMain = BitmapFactory.decodeResource(mContext.getResources(), mMainImgRes);
                        } else {
                            throw new RuntimeException("类型type错误");
                        }
                    }
                } finally { //回收这个对象
                    typedArray.recycle();
                }
            }

            //其他画笔
            mPaint = new Paint();
            //设置填充样式
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            //设置抗锯齿
            mPaint.setAntiAlias(true);
            //设置边界模糊
            mPaint.setDither(true);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            //视图是个正方形的 所以有宽就足够了 默认值是500 也就是WRAP_CONTENT的时候
            int desiredWidth = 800;

            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);

            int width;

            //Measure Width
            if (widthMode == MeasureSpec.EXACTLY) {
                //Must be this size
                width = widthSize;
            } else if (widthMode == MeasureSpec.AT_MOST) {
                //Can't be bigger than...
                width = Math.min(desiredWidth, widthSize);
            } else {
                //Be whatever you want
                width = desiredWidth;
            }

            //将测得的宽度保存起来
            mWidth = width;

            mCenter = mWidth / 2;
            //绘制扇形的半径 减掉50是为了防止边界溢出  具体效果你自己注释掉-50自己测试
            mRadius = mWidth / 2 - 50;

            //MUST CALL THIS
            setMeasuredDimension(width, width);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mTypeNum == -1) {
                //先不管
            } else {
                if (mType == 1) {
                    // 计算初始角度
                    // 从最上面开始绘制扇形会好看一点
                    float startAngle = -mAngle / 2 - 90;

                    final int paddingLeft = getPaddingLeft();
                    final int paddingRight = getPaddingRight();
                    final int paddingTop = getPaddingTop();
                    final int paddingBottom = getPaddingBottom();
                    int width = getWidth() - paddingLeft - paddingRight;
                    int height = getHeight() - paddingTop - paddingBottom;

                    for (int i = 0; i < mTypeNum; i++) {
                        //设置绘制时画笔的颜色
                        mPaint.setColor(mColors[i]);
                        //画一个扇形
                        RectF rect = new RectF(mCenter - mRadius, mCenter - mRadius, mCenter
                                + mRadius, mCenter + mRadius);
                        canvas.drawArc(rect, startAngle, mAngle, true, mPaint);
                        mTextPaint.setColor(mTextColor);

                        if(mDeses != null && i < mDeses.length){
                            drawText(startAngle, mDeses[i], mRadius, mTextPaint, canvas);
                        }

                        int imgWidth = mRadius / 3;

                        int w = (int) (Math.abs(Math.cos(Math.toRadians(Math.abs(180 - mAngle * i)))) *
                                imgWidth + imgWidth * Math.abs(Math.sin(Math.toRadians(Math.abs(180 - mAngle * i)))));
                        int h = (int) (Math.abs(Math.sin(Math.toRadians(Math.abs(180 - mAngle * i)))) *
                                imgWidth + imgWidth * Math.abs(Math.cos(Math.toRadians(Math.abs(180 - mAngle * i)))));

                        float angle = (float) Math.toRadians(startAngle + mAngle / 2);

                        //确定图片在圆弧中 中心点的位置
                        float x = (float) (width / 2 + (mRadius / 2 + mRadius / 12) * Math.cos(angle));
                        float y = (float) (height / 2 + (mRadius / 2 + mRadius / 12) * Math.sin(angle));
                        // 确定绘制图片的位置
                        RectF rect1 = new RectF(x - w / 2, y - h / 2, x + w / 2, y + h / 2);
                        canvas.drawBitmap(mListBitmap.get(i), null, rect1, null);

                        //重置开始角度
                        startAngle = startAngle + mAngle;
                    }

                    //最后绘制圆环
//                    Rect mDestRect = new Rect(0, 0, mWidth, mWidth);
//                    canvas.drawBitmap(mYuanHuan, null, mDestRect, mPaint);
                } else {
                    //大圆盘
                    Rect mDestRect = new Rect(0, 0, mWidth, mWidth);
                    canvas.drawBitmap(mMain, null, mDestRect, mPaint);
                }
            }
        }

        //绘制文字
        private void drawText(float startAngle, String string, int radius, Paint textPaint, Canvas canvas) {
            //创建绘制路径
            Path circlePath = new Path();
            //范围也是整个圆盘
            RectF rect = new RectF(mCenter - radius, mCenter - radius, mCenter
                    + radius, mCenter + radius);
            //给定扇形的范围
            circlePath.addArc(rect, startAngle, mAngle);

            //圆弧的水平偏移
            float textWidth = textPaint.measureText(string);
            //圆弧的垂直偏移
            float hOffset = (float) (Math.sin(mAngle / 2 / 180 * Math.PI) * radius) - textWidth / 2;

            //绘制文字
            canvas.drawTextOnPath(string, circlePath, hOffset, radius / 4, textPaint);
        }

        //再一次onDraw
        public void show() {
            //做最后的准备工作 检查数据是否合理
            if (mType == 1) {
//                if ( mHuanImgRes == null || mHuanImgRes == 0 )
//                    mYuanHuan = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.yuanhuan);
//                else {
//                    mYuanHuan = BitmapFactory.decodeResource(mContext.getResources(), mHuanImgRes);
//                }
                //文字大小
                if (mTextSize == 0)
                    mTextSize = 14 * getScale();
                //文字颜色 默认粉红色
                if (mTextColor == 0)
                    mTextColor = Color.parseColor("#ff00ff");

                if (mListBitmap.size() != mDeses.length || mListBitmap.size() != mColors.length
                        || mDeses.length != mColors.length) {
                    throw new RuntimeException("Icons数量和Deses和Colors三者数量必须与mTypeNum一致");
                }
            } else {
                //直接大图
                if (mMainImgRes == null || mMainImgRes == 0)
                    throw new RuntimeException("类型为2必须要传大图mMainImgRes");
                mMain = BitmapFactory.decodeResource(mContext.getResources(), mMainImgRes);
            }

            if (mTextPaint == null) {
                //文字画笔
                mTextPaint = new Paint();
                //设置填充样式
                mTextPaint.setStyle(Paint.Style.STROKE);
                //设置抗锯齿
                mTextPaint.setAntiAlias(true);
                //设置边界模糊
                mTextPaint.setDither(true);
                //设置画笔颜色
                mTextPaint.setColor(mTextColor);
                //设置字体大小
                mTextPaint.setTextSize(mTextSize);
            }
            if (mTypeNum != 0)
                mAngle = (float) (360.0 / mTypeNum);
            if (mVarTime == 0)
                mVarTime = 75;

            //重绘
            invalidate();
        }

        private float getScale() {
            TextView textView = new TextView(mContext);
            textView.setTextSize(1);
            return textView.getTextSize();
        }
    }


}