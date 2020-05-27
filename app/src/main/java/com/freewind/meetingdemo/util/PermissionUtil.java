package com.freewind.meetingdemo.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.freewind.vcs.util.ActivityLauncher;

import static android.app.Activity.RESULT_OK;

/**
 * author superK
 * update_at 2019/12/21
 * description
 */
public class PermissionUtil {
    private static PermissionUtil instance;

    private PermissionUtil(){}

    public static PermissionUtil getInstance(){
        if(instance == null){
            synchronized (PermissionUtil.class) {
                if (instance == null) {
                    instance = new PermissionUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 判断权限集合
     * permissions 权限数组
     * return true-表示全部打开了  false-表示有没有打开的
     */
    public boolean isOpenPermissons(Context context, String[] permissions) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }

        for (String permission : permissions){
            if (!isOpenPermisson(context, permission)) {
                return false;
            }
        }
        return true;
    }

    //判断某个权限是否已打开
    /**
     * 检查权限是否获取（android6.0及以上系统可能默认关闭权限，且没提示）
     *
     * 一般android6以下会在安装时自动获取权限,但在小米机上，可能通过用户权限管理更改权限,checkSelfPermission会始终是true，
     * targetSdkVersion<23时 即便运行在android6及以上设备 ContextWrapper.checkSelfPermission和Context.checkSelfPermission失效
     * 返回值始终为PERMISSION_GRANTED,此时必须使用PermissionChecker.checkSelfPermission
     */
    public boolean isOpenPermisson(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED &&
                context.getPackageManager().checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            return PermissionChecker.checkPermission(
                    context,
                    permission,
                    Binder.getCallingPid(),
                    Binder.getCallingUid(),
                    context.getPackageName()
            ) == PermissionChecker.PERMISSION_GRANTED;
        } else {
            return false;
        }
    }

    public boolean isOpenPermissons(int[] permissions) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        for (int permission : permissions){
            if (permission != PermissionChecker.PERMISSION_GRANTED){
                return false;

            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isGrandFloating(Context context){
        return Settings.canDrawOverlays(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setPermissionFloat(Activity activity, PermissionCallBack callBack){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
        ActivityLauncher.init(activity)
                .startActivityForResult(intent, (resultCode, data) -> callBack.onResult(resultCode == RESULT_OK));
    }

    public interface PermissionCallBack{
        void onResult(boolean success);
    }

}
