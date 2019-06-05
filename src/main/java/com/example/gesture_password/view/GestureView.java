package com.example.gesture_password.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gesture_password.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 手势设置、验证
 *
 * @author hanxw
 * @time 2018/9/20 14:06
 */
public class GestureView extends LinearLayout {

    /**
     * 手势初始化设置状态
     */
    private static final int STATE_SET = 100;
    /**
     * 手势登录验证状态
     */
    private static final int STATE_VERIFY = 101;
    /**
     * 设置一个参数记录当前是出于初始化阶段还是使用阶段，默认为设置状态
     */
    private int mStateFlag = STATE_SET;

    /**
     * 记录剩余几次验证次数
     */
    private int mVerifyNum;
    /**
     * 验证次数 默认5次
     */
    private int mVerifyTime;
    /**
     * 最小点的位数 默认4次
     */
    private int minPointNum;
    /**
     * 点之间的间隔
     */
    private int mPointDis;

    /**
     * 画笔
     */
    private Paint mGesturePaint;
    /**
     * 提示信息的控件
     */
    private TextView mMessageView;
    /**
     * 绘制点的集合(x、y轴坐标)
     */
    private List<GestureBean> mLockPointList = new ArrayList<>();
    /**
     * 绘制提示点的集合(x、y轴坐标)
     */
    private List<GestureBean> mHintLockPointList = new ArrayList<>();
    /**
     * 记录选择点位置的集合。例如：1,4,6,7
     */
    private List<Integer> mSelectedPointList = new ArrayList<>();
    /**
     * 选择点的集合(x、y轴坐标)
     */
    private List<GestureBean> mLockSelectedPointList = new ArrayList<>();

    private Context mContext;
    /**
     * 提示点的控件
     */
    private LockHintView mLockHintView;
    /**
     * 绘制点的控件
     */
    private LockView mLockView;

    public GestureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        getTypedArrayData(context, attrs);
        setView(context);
        initPaint();
    }

    private void getTypedArrayData(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.gesture_view_attrs);
        mStateFlag = ta.getInt(R.styleable.gesture_view_attrs_state, STATE_VERIFY);
        minPointNum = ta.getInt(R.styleable.gesture_view_attrs_min_point, 4);
        mVerifyTime = ta.getInt(R.styleable.gesture_view_attrs_verify_time, 5);
        mVerifyNum = mVerifyTime;
        mPointDis = ta.getInt(R.styleable.gesture_view_attrs_point_dis, 30);
        ta.recycle();
    }

    private void setView(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.WHITE);
        if (mStateFlag == STATE_SET) {
            clearGestureKey(context);

            // 显示提示点
            LayoutParams hintLockParams = new LayoutParams(LayoutParams.MATCH_PARENT, 300);
            hintLockParams.gravity = Gravity.CENTER;
            mLockHintView = new LockHintView(context);
            addView(mLockHintView, hintLockParams);

            TextView resetView = new TextView(context);
            resetView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLockHintView.clearHintPoint();
                    mLockView.clearPoint();
                    mLockView.invalidate();
                }
            });
            resetView.setText("重置手势密码");
            resetView.setTextColor(getResources().getColor(R.color.bak_blue));
            resetView.setTextSize(22);
            resetView.setGravity(Gravity.CENTER_HORIZONTAL);
            LayoutParams resetParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            resetParams.gravity = Gravity.CENTER_HORIZONTAL;
            resetParams.bottomMargin = 30;
            addView(resetView, resetParams);
        }

        boolean isVerify = (mStateFlag == STATE_VERIFY);
        mMessageView = new TextView(context);
        mMessageView.setGravity(Gravity.CENTER_HORIZONTAL);
        mMessageView.setTextSize(isVerify ? 22 : 18);
        mMessageView.setTextColor(isVerify ? getResources().getColor(R.color.bak_blue) : Color.GRAY);
        mMessageView.setText(isVerify ? "输入手势密码" : "绘制手势密码");
        LayoutParams messageParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        messageParams.gravity = Gravity.CENTER_HORIZONTAL;
        addView(mMessageView, messageParams);

        LayoutParams lockParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lockParams.gravity = Gravity.CENTER;
        mLockView = new LockView(context);
        addView(mLockView, lockParams);
    }

    private void initPaint() {
        mGesturePaint = new Paint();
        mGesturePaint.setAntiAlias(true);
        mGesturePaint.setDither(true);
        mGesturePaint.setStrokeWidth(10);
        mGesturePaint.setStyle(Paint.Style.STROKE);
        mGesturePaint.setColor(Color.parseColor("#3691ff"));
        mGesturePaint.setTextSize(30);
    }

    /**
     * 提示的点
     */
    class LockHintView extends View {

        private int mWidthSize;
        private int mHeightSize;
        /**
         * 未选中小图标，选中小图标
         */
        private Bitmap unSelectedBitmapSmall, selectedBitmapSmall;

        /**
         * 绘制图片的半径
         */
        private int mSmallRadius;

        /**
         * 记录绘制点的位置
         */
        private List<Integer> mHintPointList = new ArrayList<>();

        public LockHintView(Context context) {
            super(context);
            mSmallRadius = mPointDis;
            unSelectedBitmapSmall = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_finger_unselected_new);
            unSelectedBitmapSmall = Bitmap.createScaledBitmap(unSelectedBitmapSmall, mSmallRadius * 2, mSmallRadius * 2, true);

            selectedBitmapSmall = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_finger_selected_small);
            selectedBitmapSmall = Bitmap.createScaledBitmap(selectedBitmapSmall, mSmallRadius * 2, mSmallRadius * 2, true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvasSelectedSmall(canvas);
            canvasBitmap(canvas, unSelectedBitmapSmall, mWidthSize, mHeightSize, mPointDis, false);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            //宽度的大小与模式
            mWidthSize = MeasureSpec.getSize(widthMeasureSpec);
            // 高度的大小与模式
            mHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        }

        private void canvasSelectedSmall(Canvas canvas) {
            if (mSelectedPointList != null && mSelectedPointList.size() > 0) {
                mHintPointList.clear();
                mHintPointList.addAll(mSelectedPointList);
                for (int i : mSelectedPointList) {
                    GestureBean bean = mHintLockPointList.get(i);
                    canvas.drawBitmap(selectedBitmapSmall, bean.x - mSmallRadius, bean.y - mSmallRadius, mGesturePaint);
                }
            }
        }

        /**
         * 获取第一次绘制点的位置
         *
         * @return
         */
        protected List<Integer> getHintPointList() {
            return mHintPointList;
        }

        /**
         * 清空提示点(重置状态)
         */
        protected void clearHintPoint() {
            if (mHintPointList != null) {
                mHintPointList.clear();
            }
            if (mSelectedPointList != null) {
                mSelectedPointList.clear();
            }
            invalidate();
        }
    }

    /**
     * 手势密码的点
     */
    class LockView extends View {

        /**
         * 当前手指点
         */
        private float currentX, currentY;

        private int mWidthSize;
        private int mHeightSize;
        /**
         * 未选中图标，选中图标
         */
        private Bitmap unSelectedBitmap, selectedBitmap;
        /**
         * 绘制图片的半径
         */
        private int mRadius;
        /**
         * 绘制路径
         */
        private Path mLintPath = new Path();

        /**
         * 是否需要重新绘制提示点
         */
        private boolean isInvalidateHintLock = true;

        /**
         * 清空点(重置状态)
         */
        protected void clearPoint() {
            isInvalidateHintLock = true;
            invalidate();
        }

        public LockView(Context context) {
            super(context);
            mRadius = mPointDis * 2;
            unSelectedBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_finger_unselected);
            unSelectedBitmap = Bitmap.createScaledBitmap(unSelectedBitmap, mRadius * 2, mRadius * 2, true);

            selectedBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_finger_selected);
            selectedBitmap = Bitmap.createScaledBitmap(selectedBitmap, mRadius * 2, mRadius * 2, true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mLockSelectedPointList != null && mLockSelectedPointList.size() > 0) {

                for (int i = 0; i < mLockSelectedPointList.size(); i++) {
                    GestureBean bean = mLockSelectedPointList.get(i);

                    // 绘制选中的图片
                    canvas.drawBitmap(selectedBitmap, bean.x - mRadius, bean.y - mRadius, mGesturePaint);

                    if (i == 0) {
                        // 移动绘制线的起点
                        mLintPath.moveTo(bean.x, bean.y);
                    }
                    // 绘制连线
                    mLintPath.lineTo(bean.x, bean.y);
                    canvas.drawPath(mLintPath, mGesturePaint);
                }
            }

            if (mStateFlag == STATE_SET
                    && mSelectedPointList != null
                    && mSelectedPointList.size() > 0
                    && mLockHintView != null
                    && isInvalidateHintLock) {
                // 绘制提示点
                mLockHintView.invalidate();
            }
            canvasBitmap(canvas, unSelectedBitmap, mWidthSize, mHeightSize, mPointDis * 3, true);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            mWidthSize = MeasureSpec.getSize(widthMeasureSpec);
            mHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // x轴的边界值
            float confineX;
            // y轴的边界值
            float confineY;
            currentX = event.getX();
            currentY = event.getY();
            int lockPointHeight = mLockPointList.size();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLockSelectedPointList.clear();
                    mSelectedPointList.clear();
                    for (int i = 0; i < lockPointHeight; i++) {
                        GestureBean gestureBean = mLockPointList.get(i);
                        confineX = Math.abs(currentX - gestureBean.x);
                        confineY = Math.abs(currentY - gestureBean.y);
                        if (confineX <= mRadius &&
                                confineX > 0 &&
                                confineY <= mRadius &&
                                confineY > 0) {
                            mLockSelectedPointList.add(new GestureBean(gestureBean.x, gestureBean.y));
                            mSelectedPointList.add(i);
                        }
                    }
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    for (int i = 0; i < lockPointHeight; i++) {
                        GestureBean gestureBean = mLockPointList.get(i);
                        confineX = Math.abs(currentX - gestureBean.x);
                        confineY = Math.abs(currentY - gestureBean.y);
                        if (confineX <= mRadius &&
                                confineX > 0 &&
                                confineY <= mRadius &&
                                confineY > 0) {
                            if (!mLockSelectedPointList.contains(new GestureBean(gestureBean.x, gestureBean.y))) {
                                // 当滑动的点没记录过时，才添加该点
                                mLockSelectedPointList.add(new GestureBean(gestureBean.x, gestureBean.y));
                                mSelectedPointList.add(i);
                            }
                        }
                    }
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    if (mLockSelectedPointList.size() == 0) {
                        break;
                    }

                    // 清空选择大点的集合
                    mLockSelectedPointList.clear();
                    // 清除绘制路线中的内容
                    mLintPath.reset();

                    /**
                     * 1.当提示点的控件不为空
                     * 2.提示点保存的位置不能为空
                     * 3.绘制点的位置不能为空
                     * 4.提示点的信息已经绘制过
                     */
                    if (mStateFlag == STATE_SET
                            && !isInvalidateHintLock
                            && mLockHintView != null
                            && mLockHintView.getHintPointList() != null
                            && mSelectedPointList != null) {
                        if (mLockHintView.getHintPointList().equals(mSelectedPointList)) {
                            putGestureSharedPreferences(mSelectedPointList);
                            //showToast("设置手势密码成功");
                            if (mGestureSetListener != null) {
                                mGestureSetListener.onSetSucceed(mSelectedPointList);
                            }
                        } else {
                            showToast("两次手势密码不一致");
                        }
                    }
                    isInvalidateHintLock = false;
                    // 判断绘制的点 （0,,minPointNum) 区间内，则提报错信息
                    if (mSelectedPointList != null
                            && mSelectedPointList.size() < minPointNum
                            && mSelectedPointList.size() > 0) {
                        if (mStateFlag == STATE_SET
                                && mLockHintView != null
                                && mLockHintView.getHintPointList() != null
                                && mLockHintView.getHintPointList().size() < minPointNum) {
                            // 1.当提示点的控件不为空
                            // 2.提示点保存位置位数大小 < 4 时，则重置提示点的状态，以及清空提示点绘制的图形
                            isInvalidateHintLock = true;
                            mLockHintView.clearHintPoint();
                            showToast("绘制点不能小于" + minPointNum + "位");
                        }

                        if (mStateFlag == STATE_VERIFY) {
                            --mVerifyNum;
                            if (mVerifyNum > 0) {
                                showToast("输入错误，还有" + mVerifyNum + "机会");
                            } else {
                                showToast("手势密码输入错误" + mVerifyTime + "次");
                                if (mGestureVerifyListener != null) {
                                    mGestureVerifyListener.onVerifyFailed(false);
                                }
                            }
                        }
                        invalidate();
                        break;
                    }

                    if (mStateFlag == STATE_VERIFY) {
                        List<Integer> pointLists = getGestureSharedPreferences();
                        if (pointLists == null) {
                            //showToast("需要设置手势密码");
                            if (mGestureVerifyListener != null) {
                                mGestureVerifyListener.onVerifyFailed(true);
                            }
                        } else if (mSelectedPointList != null) {
                            if (mSelectedPointList.equals(pointLists) && mVerifyNum > 0) {
                                mVerifyNum = mVerifyTime;
                                //showToast("手势密码验证成功");
                                if (mGestureVerifyListener != null) {
                                    mGestureVerifyListener.onVerifySucceed(pointLists);
                                }
                            } else if (mSelectedPointList.size() >= 1) {
                                --mVerifyNum;
                                if (mVerifyNum <= 0) {
                                    showToast("手势密码输入错误" + mVerifyTime + "次");
                                    if (mGestureVerifyListener != null) {
                                        mGestureVerifyListener.onVerifyFailed(false);
                                    }
                                } else {
                                    showToast("输入错误，还有" + mVerifyNum + "机会");
                                }
                            }
                        }
                    }
                    invalidate();
                    break;

                default:
                    break;
            }

            return true;
        }
    }

    /**
     * 绘制 3*3 点矩阵
     *
     * @param canvas
     * @param bitmap     绘制图片
     * @param withSize   宽度
     * @param heightSize 高度
     * @param pointDis   点之间距离
     * @param isLock     是否是手势密码的点
     */
    private void canvasBitmap(Canvas canvas, Bitmap bitmap, int withSize, int heightSize, int pointDis, boolean isLock) {
        if (isLock) {
            mLockPointList.clear();
        } else {
            mHintLockPointList.clear();
        }
        // 图片点的宽度
        int bitmapWidth = bitmap.getWidth();
        // 图片点的高度
        int bitmapHeight = bitmap.getHeight();
        // 平移画布的初始宽度
        int translateWidth = withSize / 2 - bitmapWidth * 3 / 2 - pointDis;
        // 平移画布的初始高度
        int translateHeight = heightSize / 2 - bitmapHeight * 3 / 2 - pointDis;

        // 画点，并记录点的位置(该点的位置为对应图片的中心)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                float pointX = translateWidth + bitmapWidth * j + bitmapWidth / 2 + pointDis * j;
                float pointY = translateHeight + bitmapHeight * i + bitmapHeight / 2 + pointDis * i;
                // 画中心点
                //canvas.drawPoint(pointX, pointY, mGesturePaint);

                // 保存点的x,y轴坐标
                if (isLock) {
                    mLockPointList.add(new GestureBean(pointX, pointY));
                } else {
                    mHintLockPointList.add(new GestureBean(pointX, pointY));
                }
            }
        }

        // 平移画布
        canvas.translate(translateWidth, translateHeight);
        // 画圆
        for (int i = 0; i < 3; i++) {
            if (i != 0) {
                canvas.translate(-pointDis * 2, pointDis);
            }
            for (int j = 0; j < 3; j++) {
                if (j != 0) {
                    canvas.translate(pointDis, 0);
                }
                canvas.drawBitmap(bitmap, bitmap.getWidth() * j, bitmap.getHeight() * i, mGesturePaint);
            }
        }
    }

    /**
     * toast 提示信息
     *
     * @param message
     */
    private void showToast(String message) {
        if (mContext != null) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取手势输入点的位置
     *
     * @return
     */
    private List<Integer> getGestureSharedPreferences() {
        SharedPreferences spf = mContext.getSharedPreferences(SP_GESTURE_NAME, Activity.MODE_PRIVATE);
        String pointPosition = spf.getString(SP_GESTURE_KEY, "");
        if (TextUtils.isEmpty(pointPosition)) {
            return null;
        }
        String newPointPosition = pointPosition.substring(1, pointPosition.length() - 1).trim();
        if (TextUtils.isEmpty(newPointPosition)) {
            return null;
        }
        String[] positionStrs = newPointPosition.split(",");
        List<Integer> newPositionList = new ArrayList<>();
        for (String positionStr : positionStrs) {
            newPositionList.add(Integer.valueOf(positionStr.trim()));
        }
        return decrypt(newPositionList);
    }

    /**
     * 解密
     *
     * @param gestureLists
     * @return
     */
    private List<Integer> decrypt(List<Integer> gestureLists) {
        List<Integer> newGestures = new ArrayList<>();
        int length = gestureLists.size();
        for (int i = 0; i < length; i++) {
            int oldGestureStr = (gestureLists.get(i) - i - (i % 2 == 0 ? 1 : 2)) / (i + 1);
            newGestures.add(oldGestureStr);
        }
        return newGestures;
    }

    private static final String SP_GESTURE_NAME = "gesture_sp";
    private static final String SP_GESTURE_KEY = "point_position_key";

    /**
     * 加密，混淆设置手势密码的位置
     *
     * @param gestureLists
     * @return
     */
    private List<Integer> encrypt(List<Integer> gestureLists) {
        List<Integer> newGestures = new ArrayList<>();
        for (int i = 0; i < gestureLists.size(); i++) {
            int oldGestureStr = gestureLists.get(i) * (i + 1) + i + (i % 2 == 0 ? 1 : 2);
            newGestures.add(oldGestureStr);
        }
        return newGestures;
    }

    /**
     * 保存手势输入点的位置
     *
     * @param gestureLists
     */
    private boolean putGestureSharedPreferences(List<Integer> gestureLists) {
        SharedPreferences spf = mContext.getSharedPreferences(SP_GESTURE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor edit = spf.edit();
        edit.putString(SP_GESTURE_KEY, encrypt(gestureLists).toString().trim());
        return edit.commit();
    }

    /**
     * 清除 本地保存的密码 中的所有数据
     *
     * @param context 上下文
     */
    public static boolean clearGestureKey(@NonNull Context context) {
        SharedPreferences spf = context.getSharedPreferences(SP_GESTURE_NAME, Activity.MODE_PRIVATE);
        return spf.edit().clear().commit();
    }

    /**
     * 获取本地是否保存了手势密码的状态
     *
     * @return true:密码已存在；false:密码不存在
     */
    public static boolean getGestureKeyState(@NonNull Context context) {
        SharedPreferences spf = context.getSharedPreferences(SP_GESTURE_NAME, Activity.MODE_PRIVATE);
        String pointPosition = spf.getString(SP_GESTURE_KEY, "");
        if (TextUtils.isEmpty(pointPosition)) {
            return false;
        }
        return true;
    }

    /**
     * 手势设置回调接口
     */
    private IGestureSetListener mGestureSetListener;

    public void setOnGestureSetListener(IGestureSetListener listener) {
        this.mGestureSetListener = listener;
    }

    /**
     * 手势设置回调接口
     */
    public interface IGestureSetListener {
        /**
         * 手势设置成功
         *
         * @param pointPos 设置点的位置
         */
        void onSetSucceed(List<Integer> pointPos);
    }

    /**
     * 手势验证回调接口
     */
    private IGestureVerifyListener mGestureVerifyListener;

    public void setOnGestureVerifyListener(IGestureVerifyListener listener) {
        this.mGestureVerifyListener = listener;
        if (mStateFlag == STATE_VERIFY
                && getGestureSharedPreferences() == null
                && mGestureVerifyListener != null) {
            mGestureVerifyListener.onVerifyFailed(true);
        }
    }

    /**
     * 手势验证回调接口
     */
    public interface IGestureVerifyListener {
        /**
         * 手势验证成功
         *
         * @param pointPos 输入点的位置
         */
        void onVerifySucceed(List<Integer> pointPos);

        /**
         * 手势验证失败
         *
         * @param isSetState 是否需要设置手势 <p>true：需要设置手势密码，false：不需要设置手势密码</p>
         */
        void onVerifyFailed(boolean isSetState);
    }


    /**
     * 存储手势坐标
     */
    static class GestureBean {
        private float x;
        private float y;

        @Override
        public String toString() {
            return "GestureBean{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }

        GestureBean(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        @Override
        public boolean equals(Object o) {
            return ((GestureBean) o).getX() == x && ((GestureBean) o).getY() == y;
        }
    }
}