package com.runvision.myview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.runvision.bean.ImageStack;
import com.runvision.core.Const;
import com.runvision.thread.FaceFramTask;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by Administrator on 2018/6/1.
 */

public class MyCameraSuf extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = "sulin";
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private int mWidth;
    private int mHeight;
    private Context mContext;
    private Camera.Parameters parameters;
    private Camera.Size previewSize;
    private FaceFramTask task;
    private static byte[] mCameraData = null;
    public static ExecutorService exec = Executors.newFixedThreadPool(10);
    private boolean cameraStaus = false;

    public static byte[] getmCameraData() {
        return mCameraData;
    }

    private ImageStack imgStack = new ImageStack(480, 640);

    public ImageStack getImgStack() {
        return imgStack;
    }

    /**
     * 设置相机用于  0代表抓拍人脸用，1代表注册人脸模版用
     */
    private int camerType = 0;

    public void setCameraType(int type) {
        this.camerType = type;
    }

    public int getCamerType() {
        return camerType;
    }

    public MyCameraSuf(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSurfaceHolder = getHolder();
        mContext = context;
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        getScreenMetrix(context);
        init();
        setWillNotDraw(false);
    }

    public void openCamera() {
        if (cameraStaus) {
            return;
        }
        releaseCamera();
        try {
            // G68A 开启前置摄像头
           mCamera = Camera.open(0);
        } catch (Exception e) {
            mCamera = null;
            Log.d("lichao", "openCamera: open相机失败");
        }
        initCamera();

    }
    private void initCamera(){
        if (mCamera != null) {
            try {
                    mCamera.setPreviewCallback(this);
                    if (parameters == null) {
                        parameters = mCamera.getParameters();
                    }
                    DisplayMetrics metrics = new DisplayMetrics();
                    WindowManager WM = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                    WM.getDefaultDisplay().getMetrics(metrics);
                    previewSize = getBestSupportedSize(parameters.getSupportedPreviewSizes(), metrics);
                    // 预览适配
//                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                    //设置相机预览格式
                    parameters.setPreviewFormat(ImageFormat.NV21);
                    parameters.setPictureSize(Const.PRE_WIDTH, Const.PRE_HEIGTH);
                    mCamera.setParameters(parameters);
                    //设置预览方向
                    mCamera.setDisplayOrientation(270);
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    mCamera.startPreview();
                    cameraStaus = true;
                if (camerType == 0) {
                    mProportionH = (float) mScreenHeight / (float) Const.PRE_WIDTH;
                    mProportionW = (float) mScreenWidth / (float) Const.PRE_HEIGTH;
                } else if (camerType == 1) {
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(480, 640);
                    this.setLayoutParams(lp);
                    mProportionH = (float)640/(float) 640;
                    mProportionW = (float)480/(float) 480;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated...:"+cameraStaus);
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera();
        Log.i(TAG, "surfaceChanged..."+cameraStaus);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed..."+cameraStaus);
        releaseCamera();
    }

    public synchronized void releaseCamera() {
        if (!cameraStaus) {
            return;
        }

        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamera = null;
        }
        cameraStaus = false;
    }


    /**
     * 相机的实时流
     *
     * @param bytes
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        imgStack.pushImageInfo(bytes, System.currentTimeMillis());
        mCameraData = bytes;

        if (mCameraData == null) {
            return;
        }
    }

    /*@Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //Log.d(TAG, "onPreviewFrame: ...........");
        mCameraData = data;

        if (mCameraData == null) {
            return;
        }

        if (!Const.afdInit) {
            return;
        }
        if (task != null) {
            switch (task.getStatus()) {
                case RUNNING:
                    return;
                case PENDING:
                    task.cancel(false);
                    break;
                default:
                    break;
            }
        }
        task = new FaceFramTask(this);
        task.execute();
    }*/

    private Paint mPaint;
    private float nFaceLeft, nFaceTop, nFaceRight, nFaceBottom; // 人脸坐标
    private float mProportionH = 0;
    private float mProportionW = 0;
    private int width_offset = 0;
    private int mWeith = 40;
    // 人脸坐标

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.GREEN);
        // canvas.drawRect(nFaceLeft, nFaceTop, nFaceRight, nFaceBottom, mPaint);
        if(camerType==1){
            mScreenWidth=480;
        }
        float startX = mScreenWidth - nFaceRight;
        float startY = nFaceTop;
        float endX = mScreenWidth - nFaceLeft;
        float endY = nFaceBottom;

        if (endX - startX < 160) {
            mWeith = 20;
        } else {
            mWeith = 30;
        }

        // 左上
        canvas.drawLine(startX, startY, startX, startY + mWeith, mPaint);
        canvas.drawLine(startX, startY, startX + mWeith, startY, mPaint);
        // 左下
        canvas.drawLine(startX, endY, startX, endY - mWeith, mPaint);
        canvas.drawLine(startX, endY, startX + mWeith, endY, mPaint);
        // 右下
        canvas.drawLine(endX, endY, endX, endY - mWeith, mPaint);
        canvas.drawLine(endX, endY, endX - mWeith, endY, mPaint);
        // 右上
        canvas.drawLine(endX, startY, endX, startY + mWeith, mPaint);
        canvas.drawLine(endX, startY, endX - mWeith, startY, mPaint);
    }

    private void init() {
        mPaint = new Paint();
        // 设置画笔为抗锯齿
        mPaint.setAntiAlias(true);
        // 设置颜色为
        mPaint.setColor(Color.GREEN);
        /**
         * 画笔样式分三种： 1.Paint.Style.STROKE：描边 2.Paint.Style.FILL_AND_STROKE：描边并填充
         * 3.Paint.Style.FILL：填充
         */
        mPaint.setStyle(Paint.Style.STROKE);
        /**
         * 设置描边的粗细，单位：像素px 注意：当setStrokeWidth(0)的时候描边宽度并不为0而是只占一个像素
         */
        mPaint.setStrokeWidth(3);

    }

    private int mScreenWidth;
    private int mScreenHeight;

    private void getScreenMetrix(Context context) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WM.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels + 48;
        Log.i("lichao", "屏幕分辨率W*H：" + mScreenWidth + "*" + mScreenHeight);
    }

    // 可能需要修改的
    public void setFacePamaer(Rect rect) {

        if (rect.top == 0 && rect.left == 0 && rect.right == 0 && rect.bottom == 0) {
            this.nFaceLeft = 0;
            this.nFaceTop = 0;
            this.nFaceRight = 0;
            this.nFaceBottom = 0;
        } else {
            this.nFaceLeft = rect.left * mProportionW;
            this.nFaceTop = rect.top * mProportionH;
            this.nFaceRight = rect.right * mProportionW;
            this.nFaceBottom = rect.bottom * mProportionH;
        }
        postInvalidate();
    }

    /**
     * 通过对比得到与宽高比最接近的预览尺寸（如果有相同尺寸，优先选择）
     * @param isPortrait 是否竖屏
     * @param surfaceWidth 需要被进行对比的原宽
     * @param surfaceHeight 需要被进行对比的原高
     * @param preSizeList 需要对比的预览尺寸列表
     * @return 得到与原宽高比例最接近的尺寸
     */
    public static  Camera.Size getCloselyPreSize(boolean isPortrait, int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {
        int reqTmpWidth;
        int reqTmpHeight;
        // 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
        if (isPortrait) {
            reqTmpWidth = surfaceHeight;
            reqTmpHeight = surfaceWidth;
        } else {
            reqTmpWidth = surfaceWidth;
            reqTmpHeight = surfaceHeight;
        }
        //先查找preview中是否存在与surfaceview相同宽高的尺寸
        for(Camera.Size size : preSizeList){
            if((size.width == reqTmpWidth) && (size.height == reqTmpHeight)){
                return size;
            }
        }

        // 得到与传入的宽高比最接近的size
        float reqRatio = ((float) reqTmpWidth) / reqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : preSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;
    }

    /**
     * 得到相机合适的预览尺寸
     * @param sizes
     * @param metrics
     * @return
     */
    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, DisplayMetrics metrics) {
        Camera.Size bestSize = sizes.get(0);
        float screenRatio = (float) metrics.widthPixels / (float) metrics.heightPixels;
        if (screenRatio > 1) {
            screenRatio = 1 / screenRatio;
        }

        for (Camera.Size s : sizes) {
            if (Math.abs((s.height / (float) s.width) - screenRatio) < Math.abs(bestSize.height /
                    (float) bestSize.width - screenRatio)) {
                bestSize = s;
            }
        }
        return bestSize;
    }

}
