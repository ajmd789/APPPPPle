package com.example.appppple.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appppple.R;
import com.example.appppple.domain.parser.BookParser;
import com.example.appppple.domain.parser.ParserFactory;
import com.example.appppple.ui.reader.ReaderActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PICK_BOOK_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // 设置允许选择的文件类型
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
            "application/epub+zip",  // EPUB 类型
            "text/plain"             // TXT 类型
        });
        startActivityForResult(intent, PICK_BOOK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_BOOK_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                
                try {
                    // 检查文件是否可以被解析
                    BookParser parser = ParserFactory.createParser(this, uri);
                    if (parser == null) {
                        Toast.makeText(this, "不支持的文件格式", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 获取文件名
                    String fileName = getFileNameFromUri(uri);
                    if (fileName == null) {
                        Toast.makeText(this, "无法获取文件名", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 启动阅读器
                    ReaderActivity.start(this, uri, fileName);
                    finish();
                    
                } catch (Exception e) {
                    Log.e(TAG, "选择文件失败", e);
                    Toast.makeText(this, "选择文件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "未选择文件", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 用户取消选择或发生错误
            finish();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        
        try {
            // 尝试从 URI 获取文件名
            if (uri.getScheme() != null && uri.getScheme().equals("content")) {
                try (android.database.Cursor cursor = getContentResolver().query(
                        uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if (nameIndex != -1) {
                            fileName = cursor.getString(nameIndex);
                        }
                    }
                }
            }
            
            // 如果无法从 URI 获取，尝试从路径获取
            if (fileName == null) {
                String path = uri.getPath();
                if (path != null) {
                    int lastSlash = path.lastIndexOf('/');
                    if (lastSlash != -1) {
                        fileName = path.substring(lastSlash + 1);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取文件名失败", e);
        }
        
        return fileName != null ? fileName : "未知文件";
    }
} 