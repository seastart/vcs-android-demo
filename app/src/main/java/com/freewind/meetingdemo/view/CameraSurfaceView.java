package com.freewind.meetingdemo.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * author superK
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {

    private static final String TAG = "CameraSurfaceView";

    private Context mContext;
    private SurfaceHolder holder;
    private Camera mCamera;

    private int mScreenWidth;
    private int mScreenHeight;

    private int width;
    private int height;

    SurfaceTexture mSurfaceTexture;

    private boolean aBoolean = true;

    public int cameraPosition = Camera.CameraInfo.CAMERA_FACING_FRONT;//Camera.CameraInfo.CAMERA_FACING_BACK = 0代表后置摄像头，Camera.CameraInfo.CAMERA_FACING_FRONT = 1 代表前置摄像头

    public interface PreView{
        void onPreView(byte[] data, int width, int height, int format, int angle);
    }

    PreView preView;

    public void setPreView(PreView preView) {
        this.preView = preView;
    }

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
//        getScreenMetrix(context);
        mSurfaceTexture = new SurfaceTexture(10);

        handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        initHolder();
    }

    private void getScreenMetrix(Context context) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WM.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;
    }

    private void initHolder() {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        if (holder == null){
            holder = getHolder();//获得surfaceHolder引用
            holder.addCallback(this);
//            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//给mHolder设置缓存信息了，这个在Android 3.0之后就是自动设置的了，所以我们可以忽略这句代码了
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        Log.e("surface", "surfaceCreated");
        if (mCamera == null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    openCamera();
                }
            });
            thread.setName("CameraThread");
            thread.start();
        }
    }

    private void firstOpenCamera(){
        cameraPosition = Camera.CameraInfo.CAMERA_FACING_FRONT;
        mCamera = Camera.open();//开启相机
        startPreview();
    }

    Camera.PreviewCallback previewCallback;
    int count = 0;

    private HandlerThread handlerThread;
    private Handler handler;

    public void release(){
        if (handlerThread != null){
            handler.removeCallbacksAndMessages(null);
            handlerThread.quit();
        }
    }

    private void startPreview(){
        if (mCamera == null){
            return;
        }
        try {
            if (aBoolean){
                previewCallback = new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCamera.addCallbackBuffer(data);
                                if (preView != null){
                                    preView.onPreView(data, mScreenWidth, mScreenHeight, ImageFormat.NV21, getDeviceRotation());
                                }
                                benchmarkFPS();
                            }
                        });
////                    FileSaveUtil.getInstance().saveYUV(data, count);
//
//                    Camera.Size size = camera.getParameters().getPreviewSize();
//                    YuvImage image = new YuvImage(data,ImageFormat.NV21, size.width, size.height, null);
//                    Bitmap bmp = null;
////                    Log.d("eric", "count : " + count);
////                        test data if it's valid eric
//                    if(count < 5) {
//                        Log.d("eric", "enabled");
//                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
//                        bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
////                        FileSaveUtil.getInstance().saveBitmap(bmp, "sssssssssssssss_" + count, count);
//                        Log.d("eric", "saveBitmap");
//                        try {
//                            stream.close();
//                        }catch (IOException e) {
//                            Log.d("eric", "eeeeeeeeeeeeeeeeeeeeeee " + e.getMessage());
//                        }
//                    }
//
//                    if(count <= 10) {
////                        FileSaveUtil.getInstance().saveYUV(data, count);
//                        count ++;
//                    }
//                    count++;
                    }
                };

                byte[] d = new byte[((1280 * 720) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8];
                for (int i = 0; i< buffNum; i++){
                    mCamera.addCallbackBuffer(d);
                }

                mCamera.setPreviewCallbackWithBuffer(previewCallback);
            }

            this.mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.setPreviewDisplay(holder);//摄像头画面显示在Surface上
            mCamera.startPreview();
            Log.e("77777777", "77777777777777");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getDeviceRotation(){
        int rotation = 0;
        if (getContext() != null) {
            int angle = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getRotation();
            switch (angle) {
                case Surface.ROTATION_0:
                    rotation = 0;
                    break;
                case Surface.ROTATION_90:
                    rotation = 90;
                    break;
                case Surface.ROTATION_180:
                    rotation = 180;
                    break;
                case Surface.ROTATION_270:
                    rotation = 270;
                    break;
            }
        }
        if (getContext() != null){
            if (rotation ==0 && getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                rotation = 90;
            }
        }
        return rotation;
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        Log.e("surface", "surfaceChanged");
        this.width = width;
        this.height = height;
//        setCameraParams(mCamera, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Take care of releasing the Camera preview in your activity.
        Log.e("surface", "surfaceDestroyed");
        release();
        releaseCamera();
    }

    //释放相机资源
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();//停止预览
            mCamera.release();//释放相机资源

            mCamera = null;
        }
    }

    int cameraId = 0;

    public void openCamera(){
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for(int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息

            Log.e(TAG, cameraInfo.facing + " ======" + cameraInfo.orientation);
            //前置
            if(cameraInfo.facing  == cameraPosition) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                cameraId = i;
                mCamera = Camera.open(i);//打开当前选中的摄像头
                setCameraParams(mCamera, width, height);
                startPreview();
                break;
            }
        }
    }

    public void openCamera(int index){
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for(int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息

            Log.e(TAG, cameraInfo.facing + " ======++" + index);
            //前置
            if(i  == index) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                releaseCamera();
                cameraId = i;
                mCamera = Camera.open(i);//打开当前选中的摄像头
                setCameraParams(mCamera, width, height);
                startPreview();
                break;
            }
        }
    }

    //返回是否为前置
    public boolean rotateCamera(){
        //切换前后摄像头
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for(int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if(cameraPosition == Camera.CameraInfo.CAMERA_FACING_BACK) {
                //现在是后置，变更为前置
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    cameraPosition = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    releaseCamera();
                    mCamera = Camera.open(i);//打开当前选中的摄像头
                    setCameraParams(mCamera, width, height);
                    startPreview();

                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    cameraPosition = Camera.CameraInfo.CAMERA_FACING_BACK;
                    releaseCamera();
                    mCamera = Camera.open(i);//打开当前选中的摄像头
                    setCameraParams(mCamera, width, height);
                    startPreview();
                    break;
                }
            }
        }
        if (cameraPosition == Camera.CameraInfo.CAMERA_FACING_BACK){
            return false;
        }else {
            return true;
        }
    }

    private void setCameraParams(Camera camera, int width, int height) {
        Log.i(TAG,"setCameraParams  width="+width+"  height="+height);
        Camera.Parameters parameters = mCamera.getParameters();

        // 获取摄像头支持的PictureSize列表
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        for (Camera.Size size : pictureSizeList) {
            Log.i(TAG, "pictureSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        //从列表中选取合适的分辨率
        Camera.Size picSize = getProperSize(pictureSizeList, ((float) height / width));

        if (null == picSize) {
            Log.i(TAG, "null == picSize");
            picSize = parameters.getPictureSize();
        }
        Log.i(TAG, "picSize.width=" + picSize.width + "  picSize.height=" + picSize.height);
        // 根据选出的PictureSize重新设置SurfaceView大小
        parameters.setPictureSize(picSize.width, picSize.height);
//        this.setLayoutParams(new ConstraintLayout.LayoutParams((int) ((height*w)/h), height));
//        this.setLayoutParams(new FrameLayout.LayoutParams(width, (int) (width*(w/h))));

        // 获取摄像头支持的PreviewSize列表
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();

        for (Camera.Size size : previewSizeList) {
            Log.i(TAG, "previewSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        Camera.Size preSize = getProperSize(previewSizeList, ((float) height) / width);
        if (null != preSize) {
            Log.i(TAG, "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
            this.mScreenWidth = preSize.width;
            this.mScreenHeight = preSize.height;
            parameters.setPreviewSize(preSize.width, preSize.height);
        }

        //自动对焦
        List<String> focusModes = parameters.getSupportedFocusModes();
        String supportedMode = focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO : "";
        if (!supportedMode.equals("")) {
            parameters.setFocusMode(supportedMode);
        }

        parameters.setPreviewFormat(ImageFormat.NV21);//基本都支持NV21

        List<int[]> fpsList = parameters.getSupportedPreviewFpsRange();
        Log.e(TAG, "===========fpsList:" + fpsList.size());

        if(fpsList.size() > 0) {
            int[] maxFps = fpsList.get(0);

            for (int[] fps: fpsList) {
                Log.e(TAG, "===========" + fps[0] + "   " + fps[1] + "  "  + Arrays.toString(fps));

                if(maxFps[0] * maxFps[1] < fps[0] * fps[1]) {
                    maxFps = fps;
                }
            }

            //注意setPreviewFpsRange的输入参数是帧率*1000，如30帧/s则参数为30*1000
            if (mMinFPS >= maxFps[0]/1000 && mMaxFPS <= maxFps[1]/1000){
                parameters.setPreviewFpsRange(mMinFPS * 1000 , mMaxFPS * 1000);
                Toast.makeText(getContext(), "setPreviewFpsRange:" + mMinFPS +" - " + mMaxFPS , Toast.LENGTH_LONG).show();
            }

            //setPreviewFrameRate的参数是实际的帧率
//            parameters.setPreviewFrameRate(MAX_FPS);
        }
        if (parameters.isVideoStabilizationSupported()) {
            parameters.setVideoStabilization(true);
        }
        mCamera.cancelAutoFocus();//自动对焦。
        setCameraDisplayOrientation(mCamera);
//        mCamera.setDisplayOrientation(90);// 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
        mCamera.setParameters(parameters);
    }

    private int mMinFPS = 0;
    private int mMaxFPS = 0;
    private int buffNum = 3;

    public void setBuffNum(int buffNum) {
        this.buffNum = buffNum;
    }

    public void setmMinFPS(int mMinFPS) {
        this.mMinFPS = mMinFPS;
    }

    public void setmMaxFPS(int mMaxFPS) {
        this.mMaxFPS = mMaxFPS;
    }

    private int mCurrentFrameCnt = 0;
    private int mMaxFrameCnt = 10;
    private Long mLastOneHundredFrameTimeStamp = 0L;
    private double shareFps = 0;//接收到屏幕共享的帧率

    public double getCurFPS(){
        return shareFps;
    }

    /*更新FPS日志*/
    private void benchmarkFPS() {
        if (++mCurrentFrameCnt == mMaxFrameCnt) {
            mCurrentFrameCnt = 0;
            shareFps = (double)mMaxFrameCnt * 1000000000L / (System.nanoTime() - mLastOneHundredFrameTimeStamp);
            mLastOneHundredFrameTimeStamp = System.nanoTime();
        }
    }


    public List<int[]> getFpsRange(){
        if (mCamera != null){
            return mCamera.getParameters().getSupportedPreviewFpsRange();
        }else {
            return new ArrayList<>();
        }
    }

    public void setCameraDisplayOrientation (Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo (cameraId , info);
        int rotation = ((Activity)getContext()).getWindowManager ().getDefaultDisplay ().getRotation ();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Log.e(TAG, "====== degrees:" + degrees);

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;   // compensate the mirror
        } else {
            // back-facing
            result = ( info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation (result);
    }

    /**
     * 从列表中选取合适的分辨率
     * 默认w:h = 4:3
     * <p>注意：这里的w对应屏幕的height
     *            h对应屏幕的width<p/>
     */
    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio) {
        Log.i(TAG, "screenRatio=" + screenRatio);
        Camera.Size result = null;
        for (Camera.Size size : pictureSizeList) {
            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0) {
                if (size.width*size.height>640*480 && size.width*size.height<=1920*1080) {
                    result = size;
                    break;
                }
            }
        }

        if (null == result) {
            for (Camera.Size size : pictureSizeList) {
                float curRatio = ((float) size.width) / size.height;
                if (curRatio == 16f / 9) {// 默认w:h = 4:3
                    if (size.width*size.height>640*480 && size.width*size.height<=1920*1080) {
                        result = size;
                        break;
                    }
                }
            }
        }

        if (null == result) {
            for (Camera.Size size : pictureSizeList) {
                //小于100W像素
                if (size.width*size.height>640*480 && size.width*size.height<=1920*1080) {
                    result = size;
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public void onAutoFocus(boolean success, Camera Camera) {
    }

}