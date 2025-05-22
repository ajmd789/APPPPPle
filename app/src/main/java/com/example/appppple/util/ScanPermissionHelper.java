// File: ScanPermissionHelper.java
package com.example.appppple.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ScanPermissionHelper {
    private static final int STORAGE_PERMISSION_CODE = 1001;
    private final Activity mActivity;
    private ScanCallback mCallback;

    public interface ScanCallback {
        void onPermissionGranted();
        void onPermissionDenied();
        void onShowRationale();
    }

    public ScanPermissionHelper(Activity activity, ScanCallback callback) {
        this.mActivity = activity;
        this.mCallback = callback;
    }

    public void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(mActivity, 
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mCallback.onPermissionGranted();
        } else {
            handlePermissionRequest();
        }
    }

    private void handlePermissionRequest() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            mCallback.onShowRationale();
        } else {
            requestPermission();
        }
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(mActivity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mCallback.onPermissionGranted();
            } else {
                mCallback.onPermissionDenied();
            }
        }
    }

    public static void showRationaleDialog(Context context, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setTitle("需要存储权限")
                .setMessage("扫描本地书籍需要访问存储权限")
                .setPositiveButton("授予权限", listener)
                .setNegativeButton("取消", null)
                .show();
    }

    public static void showPermissionDeniedToast(Context context) {
        Toast.makeText(context, "权限被拒绝，无法扫描", Toast.LENGTH_SHORT).show();
    }
}