package com.runvision.broadcast;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import java.io.File;

/**
 * U盘升级广播
 */
public class UdiskReceiver extends BroadcastReceiver {
    private static final String TAG = "UdiskReceiver";
    /**
     * G69A系统 U盘路径
     */
    private String path_G69A = "/mnt/usb_storage/USB_DISK1/udisk0/G702_SN_AGM.apk";

    /**
     * G702系统 U盘路径
     */
    private String path_G702 = "/mnt/usb_storage/G702_SN_AGM.apk";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Intent.ACTION_MEDIA_CHECKING)) {
            Log.i(TAG, "正在挂载U盘");
        } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            Toast.makeText(context, "U盘挂载成功", Toast.LENGTH_SHORT).show();
            try {
                showUpdateDialog(context);
            } catch (PackageManager.NameNotFoundException e) {
                Toast.makeText(context, "U盘未找到升级文件", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
            Toast.makeText(context, "U盘已移除", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示是否更新对话框
     * @param context
     * @throws PackageManager.NameNotFoundException
     */
    private void showUpdateDialog(Context context) throws PackageManager.NameNotFoundException {
        StringBuffer sb = new StringBuffer();
        sb.append("当前版本：");
        sb.append(getVersionName(context));
        sb.append("     VerCode:");
        sb.append(getVersionCode(context));
        sb.append("\n是否更新？");
        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle("软件更新")
                .setMessage(sb.toString())
                // 设置内容
                .setPositiveButton("更新",
                        (dialog1, which) -> {
                            String fileName = path_G702;
                            Uri uri = Uri.fromFile(new File(fileName));
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "application/vnd.android.package-archive");
                            context.startActivity(intent);
                        })
                .setNegativeButton("暂不更新",
                        (dialog12, whichButton) -> {
                            // 点击"取消"按钮
                            dialog12.dismiss();
                        }).create();
        dialog.show();
    }

    /**
     * 获取apk版本信息
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = null;
        try {
            versionName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return versionName;
    }

    /**
     * 获取apk的versionCode
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return versionCode;
    }
}
