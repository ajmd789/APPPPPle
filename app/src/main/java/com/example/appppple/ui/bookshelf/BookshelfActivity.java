package com.example.appppple.ui.bookshelf;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appppple.R;
import com.example.appppple.domain.manager.ReadingProgressManager;
import com.example.appppple.ui.reader.ReaderActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class BookshelfActivity extends AppCompatActivity {
    private static final int PICK_BOOK_REQUEST = 1;
    private RecyclerView booksRecyclerView;
    private BookAdapter bookAdapter;
    private ReadingProgressManager progressManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookshelf);

        progressManager = ReadingProgressManager.getInstance(this);
        
        initViews();
        loadBooks();
    }

    private void initViews() {
        booksRecyclerView = findViewById(R.id.booksRecyclerView);
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookAdapter = new BookAdapter(new ArrayList<>(), this::onBookClick);
        booksRecyclerView.setAdapter(bookAdapter);

        FloatingActionButton addBookFab = findViewById(R.id.addBookFab);
        addBookFab.setOnClickListener(v -> pickBook());
    }

    private void loadBooks() {
        progressManager.getAllReadingProgress().observe(this, progressList -> {
            bookAdapter.updateBooks(progressList);
        });
    }

    private void onBookClick(ReadingProgressManager.ReadingProgress progress) {
        ReaderActivity.start(this, progress.getBookUri(), progress.getBookName());
    }

    private void pickBook() {
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
        if (requestCode == PICK_BOOK_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String fileName = getFileNameFromUri(uri);
                ReaderActivity.start(this, uri, fileName);
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        try (android.database.Cursor cursor = getContentResolver().query(
                uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        }
        return fileName != null ? fileName : "未知文件";
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBooks(); // 每次返回书架时刷新列表
    }
} 