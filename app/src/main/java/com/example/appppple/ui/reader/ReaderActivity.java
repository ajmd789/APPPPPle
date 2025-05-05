// ui/reader/ReaderActivity.java
package com.example.appppple.ui.reader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appppple.R;
import com.example.appppple.domain.manager.ReadingProgressManager;
import com.example.appppple.domain.model.Book;
import com.example.appppple.domain.model.Chapter;
import com.example.appppple.domain.parser.BookParser;
import com.example.appppple.domain.parser.ParserFactory;
import com.example.appppple.domain.parser.TxtParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReaderActivity extends AppCompatActivity {
    private static final String TAG = "ReaderActivity";
    private static final String EXTRA_BOOK_URI = "book_uri";
    private static final String EXTRA_BOOK_NAME = "book_name";
    private static final int CHARS_PER_PAGE = 1000; // 每页显示的字符数

    private TextView contentTextView;
    private TextView progressTextView;
    private View loadingLayout;
    private TextView loadingText;
    private List<String> pages;
    private int currentPage = 0;
    private int totalPages = 0;
    private Book currentBook;
    private ReadingProgressManager progressManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Uri currentBookUri;
    private String currentBookName;

    public static void start(Context context, Uri bookUri, String bookName) {
        Intent intent = new Intent(context, ReaderActivity.class);
        intent.putExtra(EXTRA_BOOK_URI, bookUri);
        intent.putExtra(EXTRA_BOOK_NAME, bookName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        
        initViews();
        progressManager = ReadingProgressManager.getInstance(this);

        // 获取传递的书籍信息
        currentBookUri = getIntent().getParcelableExtra(EXTRA_BOOK_URI);
        currentBookName = getIntent().getStringExtra(EXTRA_BOOK_NAME);

        if (currentBookUri == null || currentBookName == null) {
            Toast.makeText(this, "书籍信息不完整", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 获取文件的持久化权限
        try {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(currentBookUri, takeFlags);
        } catch (SecurityException e) {
            Log.e(TAG, "获取文件持久化权限失败", e);
            Toast.makeText(this, "无法获取文件访问权限", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 检查是否有上次的阅读进度
        ReadingProgressManager.ReadingProgress lastProgress = progressManager.getLastReadingProgress();
        if (lastProgress != null && lastProgress.getBookUri().equals(currentBookUri)) {
            currentPage = lastProgress.getCurrentPage();
            totalPages = lastProgress.getTotalPages();
            Log.d(TAG, String.format("恢复阅读进度 - 书名: %s, 当前页: %d/%d",
                    currentBookName, currentPage, totalPages));
        }

        // 显示加载动画
        showLoading("正在加载书籍...");

        // 异步加载书籍内容
        loadBookContentAsync(currentBookUri, currentBookName);
    }
    
    private void initViews() {
        contentTextView = findViewById(R.id.contentTextView);
        progressTextView = findViewById(R.id.txtProgress);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingText = findViewById(R.id.loadingText);

        findViewById(R.id.btnPrev).setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                updatePageDisplay();
                saveReadingProgress();
            }
        });

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                updatePageDisplay();
                saveReadingProgress();
            }
        });
    }

    private void showLoading(String message) {
        loadingLayout.setVisibility(View.VISIBLE);
        loadingText.setText(message);
    }

    private void hideLoading() {
        loadingLayout.setVisibility(View.GONE);
    }

    private void loadBookContentAsync(Uri bookUri, String bookName) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "开始加载书籍: " + bookName);
                BookParser parser = ParserFactory.createParser(this, bookUri);
                Log.d(TAG, "创建解析器成功: " + parser.getClass().getSimpleName());

                if (parser instanceof TxtParser) {
                    ((TxtParser) parser).addChunkLoadListener(new TxtParser.ChunkLoadListener() {
                        @Override
                        public void onChunkLoaded(int loadedBytes, int totalBytes) {
                            runOnUiThread(() -> {
                                String progress = String.format("已加载: %.1fMB/%.1fMB",
                                        loadedBytes / (1024.0 * 1024.0),
                                        totalBytes / (1024.0 * 1024.0));
                                loadingText.setText(progress);
                            });
                        }

                        @Override
                        public void onLoadComplete() {
                            runOnUiThread(() -> {
                                hideLoading();
                                updatePageDisplay();
                            });
                        }

                        @Override
                        public void onLoadError(Exception e) {
                            runOnUiThread(() -> {
                                hideLoading();
                                Toast.makeText(ReaderActivity.this,
                                        "加载失败: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                    });
                }

                currentBook = parser.parse(this, bookUri);
                Log.d(TAG, "解析完成，书籍信息: " + (currentBook != null ? 
                    "标题=" + currentBook.getTitle() + 
                    ", 章节数=" + (currentBook.getChapters() != null ? currentBook.getChapters().size() : 0) : 
                    "null"));
                
                if (currentBook == null) {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(this, "解析书籍失败", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                if (currentBook.getChapters() == null || currentBook.getChapters().isEmpty()) {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(this, "书籍章节为空", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                // 分页处理
                pages = new ArrayList<>();
                for (Chapter chapter : currentBook.getChapters()) {
                    String content = chapter.getContent();
                    if (content != null && !content.isEmpty()) {
                        // 按每页字符数分割内容
                        for (int i = 0; i < content.length(); i += CHARS_PER_PAGE) {
                            int end = Math.min(i + CHARS_PER_PAGE, content.length());
                            pages.add(content.substring(i, end));
                        }
                    }
                }

                totalPages = pages.size();
                Log.d(TAG, "分页完成，总页数: " + totalPages);
                
                // 保存初始进度
                saveReadingProgress();
                
            } catch (Exception e) {
                Log.e(TAG, "加载书籍失败", e);
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    
    private void updatePageDisplay() {
        if (pages == null || pages.isEmpty()) {
            Log.e(TAG, "页面列表为空");
            Toast.makeText(this, "书籍内容为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (currentPage < 0 || currentPage >= pages.size()) {
            Log.e(TAG, "当前页码无效: " + currentPage);
            currentPage = 0;
        }

        contentTextView.setText(pages.get(currentPage));
        progressTextView.setText(String.format("%d/%d", 
            currentPage + 1, 
            totalPages));
        
        // 更新标题显示当前页数
        setTitle(String.format("%s (%d/%d)", currentBook.getTitle(), currentPage + 1, totalPages));
    }

    private void saveReadingProgress() {
        if (currentBookUri != null && currentBookName != null) {
            progressManager.saveProgress(currentBookName, currentBookUri, currentPage, totalPages);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}