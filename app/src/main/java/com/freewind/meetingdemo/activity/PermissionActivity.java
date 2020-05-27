package com.freewind.meetingdemo.activity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.freewind.meetingdemo.util.PermissionUtil;
import com.freewind.meetingdemo.util.ToastUtil;

public class PermissionActivity extends AppCompatActivity {
    // 所需的全部权限
    static String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSION_REQUEST_CODE = 10086;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkPermission()){
            onCreate();
        }
    }

    public void onCreate() {

    }

    private boolean checkPermission(){
        if (PERMISSIONS == null){
            return false;
        }
        for (String it : PERMISSIONS){
            //判断此权限是否已打开
            if (!PermissionUtil.getInstance().isOpenPermisson(this, it)) {
                // 缺少权限时, 申请权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
                    return false;
                } else {
                    //拒绝授权
                    ToastUtil.getInstance().showLongToast("未开启相机或麦克风权限");
                    finish();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && PermissionUtil.getInstance().isOpenPermissons(grantResults) && PermissionUtil.getInstance().isOpenPermissons(this, permissions)) {
            if (checkPermission()){
                onCreate();
            }
        }else {
            boolean grand = true;
            for (String permission : permissions) {
                if (!PermissionUtil.getInstance().isOpenPermisson(this, permission)) {
                    //勾选了对话框中”Don’t ask again”的选项, shouldShowRequestPermissionRationale返回false,则走自定义弹窗
                    boolean flag = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || shouldShowRequestPermissionRationale(permission);
                    if (!flag) {
                        //拒绝授权
                        ToastUtil.getInstance().showLongToast("未开启相机或麦克风权限");
                        finish();
                        return;
                    }
                    if (grand){
                        grand = false;
                    }
                }
            }
            if (!grand){
                ToastUtil.getInstance().showLongToast("未开启相机或麦克风权限");
                finish();
            }
        }
    }
}
