// File: ScanPermissionHelper.java
package com.example.appppple.util;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

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
                // 检查用户是否勾选"不再询问"
                if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showPermissionDeniedDialog(); // 新增引导去设置的对话框
                } else {
                    mCallback.onPermissionDenied();
                }
            }
        }
    }


    public class BookFileScanner {
        private final ContentResolver mResolver;

        public BookFileScanner(ContentResolver resolver) {
            this.mResolver = resolver;
        }
        public List<Uri> scanForBooks() {
            List<Uri> results = new ArrayList<>();

            // 使用 MediaStore 查询 EPUB/TXT 文件（无需权限）
            String[] projection = {MediaStore.Files.FileColumns._ID};
            String selection = MediaStore.Files.FileColumns.MIME_TYPE + " IN (?, ?)";
            String[] selectionArgs = {"application/epub+zip", "text/plain"};

            try (Cursor cursor = mResolver.query(
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                    projection,
                    selection,
                    selectionArgs,
                    null)) {
                while (cursor != null && cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    Uri uri = ContentUris.withAppendedId(
                            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), id);
                    results.add(uri);
                }
            } catch (Exception e) {
                Log.e(TAG, "Scan error", e);
            }
            return results;
        }
    }



    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(mActivity)
                .setTitle("权限被永久拒绝")
                .setMessage("您需要在设置中手动启用存储权限")
                .setPositiveButton("去设置", (d, w) -> openAppSettings())
                .setNegativeButton("取消", null)
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", mActivity.getPackageName(), null));
        mActivity.startActivity(intent);
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