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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

;

/**
 * Created by Administrator on 2018/6/1.
 */

public class MyCameraSuf extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = "sulin";
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private Context mContext;
    private Camera.Parameters parameters;
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

            mCamera = Camera.open();
        } catch (Exception e) {
            mCamera = null;
            Log.d("sulin", "openCamera: open相机失败");
        }
        initCamera();

    }
    private void initCamera(){
        if (mCamera != null) {
            try {
                    mCamera.setPreviewCallback(this);
                    mCamera.setDisplayOrientation(0);
                    if (parameters == null) {
                        parameters = mCamera.getParameters();
                    }
                    parameters.setPreviewFormat(ImageFormat.NV21);
                    parameters.setPictureSize(Const.PRE_WIDTH, Const.PRE_HEIGTH);
                    mCamera.setParameters(parameters);
                    mCamera.setDisplayOrientation(90);
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    mCamera.startPreview();
                    cameraStaus = true;
                if (camerType == 0) {
                    mProportionH = (float) mScreenHeight / (float) Const.PRE_WIDTH;
                    mProportionW = (float) mScreenWidth / (float) Const.PRE_HEIGTH;
                }else if(camerType==1) {
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
    }

  /*  @Override
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
        // canvas.drawRect(nFaceLeft, nFaceTop, nFaceRight, nFaceBottom,
        // mPaint);
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


        /**
         * 获取屏幕的宽高
         */
        //

        //
        // Log.i("test", mProportionW + "*11**" + mProportionH);

    }

    private int mScreenWidth;
    private int mScreenHeight;

    private void getScreenMetrix(Context context) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WM.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels + 48;
        Log.i("run", "屏幕分辨率：" + mScreenWidth + "*" + mScreenHeight);
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
}
