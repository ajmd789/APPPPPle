package com.example.appppple.ui.bookshelf;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appppple.R;
import com.example.appppple.domain.manager.ReadingProgressManager;
import com.example.appppple.ui.reader.ReaderActivity;
import com.example.appppple.util.BookFileScanner;
import com.example.appppple.util.ScanPermissionHelper;

import java.util.ArrayList;
import java.util.List;

public class BookshelfActivity extends AppCompatActivity implements ScanPermissionHelper.ScanCallback {
    private static final int PICK_BOOK_REQUEST = 1;
    private static final String TAG = "BookshelfActivity";
    
    // UI Components
    private RecyclerView booksRecyclerView;
    private BookAdapter bookAdapter;
    private ProgressDialog scanProgress;
    
    // Managers & Helpers
    private ReadingProgressManager progressManager;
    private ScanPermissionHelper permissionHelper;
    private BookFileScanner fileScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookshelf);

        // 初始化组件
        progressManager = ReadingProgressManager.getInstance(this);
        permissionHelper = new ScanPermissionHelper(this, this);
        fileScanner = new BookFileScanner(getContentResolver());

        initViews();
        setupButtonClickListeners();
        loadBooks();
    }

    private void setupButtonClickListeners() {
        findViewById(R.id.buttonSearchBook).setOnClickListener(v -> startFileScan());
        findViewById(R.id.buttonAddBook).setOnClickListener(v -> pickBook());
    }

    private void initViews() {
        booksRecyclerView = findViewById(R.id.booksRecyclerView);
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookAdapter = new BookAdapter(new ArrayList<>(), this::onBookClick);
        booksRecyclerView.setAdapter(bookAdapter);
    }

    // region 权限回调
    @Override
    public void onPermissionGranted() {
        startFileScan();
    }

    @Override
    public void onPermissionDenied() {
        runOnUiThread(() -> 
            Toast.makeText(this, "需要存储权限才能扫描书籍", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onShowRationale() {
        ScanPermissionHelper.showRationaleDialog(this, (dialog, which) -> 
            permissionHelper.requestPermission()
        );
    }
    // endregion

    // region 文件扫描
    private void startFileScan() {
        showProgressDialog("正在扫描书籍...");
        
        fileScanner.scanBooksAsync(new BookFileScanner.ScanResultCallback() {
            @Override
            public void onScanComplete(List<Uri> bookUris) {
                runOnUiThread(() -> {
                    dismissProgressDialog();
                    if (bookUris.isEmpty()) {
                        showToast("未找到电子书文件");
                        return;
                    }
                    addBooksToShelf(bookUris);
                    loadBooks();
                });
            }

            @Override
            public void onScanError(Exception e) {
                runOnUiThread(() -> {
                    dismissProgressDialog();
                    showToast("扫描失败: " + e.getMessage());
                    Log.e(TAG, "Scan error", e);
                });
            }
        });
    }

    private void addBooksToShelf(List<Uri> bookUris) {
        Log.d("FileScan", "===== 开始添加书籍到书架 =====");
        for (Uri uri : bookUris) {
            String fileName = getFileNameFromUri(uri);
            Log.d("FileScan", String.format(
                "添加书籍 [文件名: %s]\n        [URI: %s]",
                fileName, 
                uri.toString()
            ));
            progressManager.saveProgress(fileName, uri, 0, 0);
        }
        Log.d("FileScan", "===== 添加完成 =====");
    }
    // endregion

    // region 辅助方法
    private void showProgressDialog(String message) {
        runOnUiThread(() -> {
            scanProgress = new ProgressDialog(this);
            scanProgress.setMessage(message);
            scanProgress.setCancelable(false);
            scanProgress.show();
        });
    }

    private void dismissProgressDialog() {
        runOnUiThread(() -> {
            if (scanProgress != null && scanProgress.isShowing()) {
                scanProgress.dismiss();
            }
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> 
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        );
    }

    private String getFileNameFromUri(Uri uri) {
        try (Cursor cursor = getContentResolver().query(
                uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    return cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Get filename error", e);
        }
        return "未知文件";
    }
    // endregion

    // region 核心功能
    private void loadBooks() {
        progressManager.getAllReadingProgress().observe(this, progressList -> 
            bookAdapter.updateBooks(progressList)
        );
    }

    private void onBookClick(ReadingProgressManager.ReadingProgress progress) {
        ReaderActivity.start(this, progress.getBookUri(), progress.getBookName());
    }

    private void pickBook() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
            "application/epub+zip",
            "text/plain"
        });
        startActivityForResult(intent, PICK_BOOK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_BOOK_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String fileName = getFileNameFromUri(uri);
                ReaderActivity.start(this, uri, fileName);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.onRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBooks();
    }
    // endregion
}