package com.example.appppple.ui.bookshelf;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.provider.MediaStore;
import android.database.Cursor;
import android.content.ContentResolver;
import android.content.ContentUris;
import java.util.ArrayList;
import java.util.List;

public class BookshelfActivity extends AppCompatActivity implements ScanPermissionHelper.ScanCallback {
    private static final int PICK_BOOK_REQUEST = 1;
    private static final String TAG = "BookshelfActivity";
    
    // UI Components
    private RecyclerView booksRecyclerView;
    private BookAdapter bookAdapter;
    private PopupMenu addMenu;
    private ProgressDialog scanProgress;
    
    // Managers & Helpers
    private ReadingProgressManager progressManager;
    private ScanPermissionHelper permissionHelper;
    private BookFileScanner fileScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookshelf);
        fileScanner = new BookFileScanner(getContentResolver());
        // 初始化组件
        progressManager = ReadingProgressManager.getInstance(this);
        permissionHelper = new ScanPermissionHelper(this, this);
        fileScanner = new BookFileScanner(getContentResolver());

        initViews();
        setupButtonClickListeners(); // 替换原来的setupMenu()
        loadBooks();
    }


    private void setupButtonClickListeners() {
        findViewById(R.id.buttonSearchBook).setOnClickListener(v -> {
            // 更新为使用 MediaStore API 无需权限检查
            startFileScan();
        });

        // 手动加书按钮保持不变
        findViewById(R.id.buttonAddBook).setOnClickListener(v -> pickBook());
    }


    private void initViews() {
        booksRecyclerView = findViewById(R.id.booksRecyclerView);
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookAdapter = new BookAdapter(new ArrayList<>(), this::onBookClick);
        booksRecyclerView.setAdapter(bookAdapter);
    }



    // region Permission Callbacks
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

    public List<Uri> scanForBooks() {
        List<Uri> results = new ArrayList<>();

        // 使用完整包路径引用
        String[] projection = {MediaStore.Files.FileColumns._ID};
        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " IN (?, ?)";
        String[] selectionArgs = {"application/epub+zip", "text/plain"};

        // 使用 Activity 的 ContentResolver
        ContentResolver resolver = getContentResolver();
        try (Cursor cursor = resolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null)) {
            while (cursor != null && cursor.moveToNext()) {
                long id = cursor.getLong(0);
                results.add(ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"), id));
            }
        } catch (Exception e) {
            Log.e(TAG, "Scan error", e);
        }
        return results;
    }

    // region File Scanning
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
                    loadBooks(); // Refresh the list
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
        // TODO: Implement bulk save logic
        Log.d(TAG, "Found books: " + bookUris.size());
        for (Uri uri : bookUris) {
            String fileName = getFileNameFromUri(uri);
            progressManager.saveProgress(fileName, uri, 0, 0); // 示例保存逻辑
        }
    }
    // endregion

    // region Helper Methods
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
        try (android.database.Cursor cursor = getContentResolver().query(
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

    // region Existing Methods
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