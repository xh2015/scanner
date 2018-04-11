package com.xh2015.scanner.scan_lib.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.zxing.Result;
import com.xh2015.scanner.scan_lib.R;
import com.xh2015.scanner.scan_lib.scaner.CameraManager;
import com.xh2015.scanner.scan_lib.scaner.CaptureActivityHandler;
import com.xh2015.scanner.scan_lib.scaner.decoding.InactivityTimer;
import com.xh2015.scanner.scan_lib.utils.AnimationUtils;
import com.xh2015.scanner.scan_lib.utils.BeepUtils;
import com.xh2015.scanner.scan_lib.utils.PhotoUtils;
import com.xh2015.scanner.scan_lib.utils.ScanConfig;
import com.xh2015.scanner.scan_lib.utils.StatusBarUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author：gary
 * Email: xuhaozv@163.com
 * description:二维码扫描界面
 * Date: 2018/4/11 上午10:53
 */
public class ScannerCodeActivity extends FragmentActivity {

    public static final String SCAN_RESULT = "scan_result";
    public static final String SCAN_CONFIG = "SCAN_config";

    private InactivityTimer inactivityTimer;

    /**
     * 扫描处理
     */
    private CaptureActivityHandler handler;

    /**
     * 整体根布局
     */
    private RelativeLayout mContainer = null;

    /**
     * 扫描框根布局
     */
    private RelativeLayout mCropLayout = null;

    /**
     * 扫描边界的宽度
     */
    private int mCropWidth = 0;

    /**
     * 扫描边界的高度
     */
    private int mCropHeight = 0;

    /**
     * 是否有预览
     */
    private boolean hasSurface;

    /**
     * 扫描成功后是否震动
     */
    private boolean vibrate = true;

    /**
     * 扫描成功后是否有声音
     */
    private boolean voice = false;

    /**
     * 是否可以图片选择
     */
    private boolean pic = false;

    /**
     * 闪光灯开启状态
     */
    private boolean mFlashing = true;

    /**
     * 生成二维码 & 条形码 布局
     */
    private LinearLayout mLlScanHelp;

    /**
     * 闪光灯 按钮
     */
    private ImageView mIvLight;
    private ImageView mIvPic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.setNoTitle(this);
        setContentView(R.layout.activity_scaner_code);
        StatusBarUtils.setTransparentStatusBar(this);
        initData();
        //界面控件初始化
        initView();
        //权限初始化
        initPermission();
        //扫描动画初始化
        initScannerAnimation();
        //初始化 CameraManager
        CameraManager.init(this);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    private void initData() {
        Intent intent = getIntent();
        ScanConfig config = (ScanConfig) intent.getSerializableExtra(SCAN_CONFIG);
        if (config != null) {
            vibrate = config.vibrate;
            voice = config.voice;
            pic = config.selectPhoto;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.capture_preview);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            //Camera初始化
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if (!hasSurface) {
                        hasSurface = true;
                        initCamera(holder);
                    }
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    hasSurface = false;

                }
            });
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    private void initView() {
        mIvLight = (ImageView) findViewById(R.id.top_mask);
        mIvPic = (ImageView) findViewById(R.id.top_openpicture);
        mContainer = (RelativeLayout) findViewById(R.id.capture_containter);
        mCropLayout = (RelativeLayout) findViewById(R.id.capture_crop_layout);
        mLlScanHelp = (LinearLayout) findViewById(R.id.ll_scan_help);
        mIvPic.setVisibility(pic ? View.VISIBLE : View.GONE);
    }

    private void initPermission() {
        //请求Camera权限 与 文件读写 权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        }
    }

    private void initScannerAnimation() {
        ImageView mQrLineView = (ImageView) findViewById(R.id.capture_scan_line);
        AnimationUtils.ScaleUpDown(mQrLineView);
    }

    public int getCropWidth() {
        return mCropWidth;
    }

    public void setCropWidth(int cropWidth) {
        mCropWidth = cropWidth;
        CameraManager.FRAME_WIDTH = mCropWidth;

    }

    public int getCropHeight() {
        return mCropHeight;
    }

    public void setCropHeight(int cropHeight) {
        this.mCropHeight = cropHeight;
        CameraManager.FRAME_HEIGHT = mCropHeight;
    }

    public void btn(View view) {
        int viewId = view.getId();
        if (viewId == R.id.top_mask) {
            light();
        } else if (viewId == R.id.top_back) {
            finish();
        } else if (viewId == R.id.top_openpicture) {
            PhotoUtils.openLocalImage(this);
        }
    }

    private void light() {
        if (mFlashing) {
            mFlashing = false;
            // 开闪光灯
            CameraManager.get().openLight();
        } else {
            mFlashing = true;
            // 关闪光灯
            CameraManager.get().offLight();
        }

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            Point point = CameraManager.get().getCameraResolution();
            AtomicInteger width = new AtomicInteger(point.y);
            AtomicInteger height = new AtomicInteger(point.x);
            int cropWidth = mCropLayout.getWidth() * width.get() / mContainer.getWidth();
            int cropHeight = mCropLayout.getHeight() * height.get() / mContainer.getHeight();
            setCropWidth(cropWidth);
            setCropHeight(cropHeight);
        } catch (IOException | RuntimeException ioe) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(ScannerCodeActivity.this);
        }
    }
    //========================================打开本地图片识别二维码 end=================================

    //--------------------------------------打开本地图片识别二维码 start---------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            ContentResolver resolver = getContentResolver();
            // 照片的原始资源地址
            Uri originalUri = data.getData();
            try {
                // 使用ContentProvider通过URI获取原始图片
                Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, originalUri);

                // 开始对图像资源解码
                Result rawResult = PhotoUtils.decodeFromPhoto(photo);
                if (rawResult != null) {
                    String info = rawResult.getText();
                    Log.v("二维码/条形码 解析结果", info);
                    setScanResult(info);
                } else {
                    Log.v("二维码/条形码 解析结果", "失败");
                    finish();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.v("二维码/条形码 解析结果", "失败");
                finish();
            }
        }
    }
    //==============================================================================================解析结果 及 后续处理 end

    public void handleDecode(Result result) {
        inactivityTimer.onActivity();
        //扫描成功之后的振动与声音提示
        BeepUtils.playSystemBeep(this, voice, vibrate);

        String info = result.getText();
        Log.v("二维码/条形码 扫描结果", info);
        setScanResult(info);
    }

    private void setScanResult(String info) {
        Intent intent = new Intent();
        intent.putExtra(SCAN_RESULT, info);
        setResult(RESULT_OK, intent);
        finish();
    }

    public Handler getHandler() {
        return handler;
    }

    public static void startActivityForResult(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, ScannerCodeActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startActivityForResult(Activity activity, ScanConfig config, int requestCode) {
        Intent intent = new Intent(activity, ScannerCodeActivity.class);
        intent.putExtra(SCAN_CONFIG, config);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startActivityForResult(Activity activity, Fragment fragment, int requestCode) {
        Intent intent = new Intent(activity, ScannerCodeActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void startActivityForResult(Activity activity, Fragment fragment, ScanConfig config, int requestCode) {
        Intent intent = new Intent(activity, ScannerCodeActivity.class);
        intent.putExtra(SCAN_CONFIG, config);
        fragment.startActivityForResult(intent, requestCode);
    }

}
