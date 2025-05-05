package com.example.appppple.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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

        Button btnSelectBook = findViewById(R.id.btnSelectBook);
        btnSelectBook.setOnClickListener(v -> selectBook());
    }

    private void selectBook() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
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
                    if (!parser.canParse(this, uri)) {
                        Toast.makeText(this, "不支持的文件格式", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 启动阅读器
                    Intent readerIntent = new Intent(this, ReaderActivity.class);
                    readerIntent.setData(uri);
                    startActivity(readerIntent);
                } catch (Exception e) {
                    Log.e(TAG, "选择文件失败", e);
                    Toast.makeText(this, "选择文件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "未选择文件", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 