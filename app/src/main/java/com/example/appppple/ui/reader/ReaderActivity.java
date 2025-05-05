// ui/reader/ReaderActivity.java
package com.example.appppple.ui.reader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appppple.R;
import com.example.appppple.domain.manager.ReadingProgressManager;
import com.example.appppple.domain.model.Book;
import com.example.appppple.domain.model.Chapter;
import com.example.appppple.domain.parser.BookParser;
import com.example.appppple.domain.parser.ParserFactory;
import com.example.appppple.domain.pagination.PaginationManager;

import java.io.IOException;
import java.util.List;

public class ReaderActivity extends AppCompatActivity {
    private static final String TAG = "ReaderActivity";
    private static final String EXTRA_BOOK_URI = "book_uri";
    private static final String EXTRA_BOOK_NAME = "book_name";
    
    private TextView mTxtContent;
    private TextView mTxtProgress;
    private List<String> mPages;
    private int mCurrentPage = 0;
    private int mTotalPages = 0;
    private Book currentBook;
    private ReadingProgressManager progressManager;

    public static void start(Context context, Uri bookUri, String bookName) {
        Intent intent = new Intent(context, ReaderActivity.class);
        intent.putExtra(EXTRA_BOOK_URI, bookUri);
        intent.putExtra(EXTRA_BOOK_NAME, bookName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_activity);
        
        // 初始化视图
        mTxtContent = findViewById(R.id.txtContent);
        mTxtProgress = findViewById(R.id.txtProgress);
        Button btnPrev = findViewById(R.id.btnPrev);
        Button btnNext = findViewById(R.id.btnNext);
        
        progressManager = ReadingProgressManager.getInstance(this);

        // 获取传递的书籍信息
        Uri bookUri = getIntent().getParcelableExtra(EXTRA_BOOK_URI);
        String bookName = getIntent().getStringExtra(EXTRA_BOOK_NAME);

        if (bookUri == null || bookName == null) {
            Toast.makeText(this, "书籍信息不完整", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 检查是否有上次的阅读进度
        ReadingProgressManager.ReadingProgress lastProgress = progressManager.getLastReadingProgress();
        if (lastProgress != null && lastProgress.getBookUri().equals(bookUri)) {
            mCurrentPage = lastProgress.getCurrentPage();
            mTotalPages = lastProgress.getTotalPages();
            Log.d(TAG, String.format("恢复阅读进度 - 书名: %s, 当前页: %d/%d",
                    bookName, mCurrentPage, mTotalPages));
        }

        // 加载书籍内容
        loadBookContent(bookUri, bookName);
        
        // 按钮事件
        btnPrev.setOnClickListener(v -> showPreviousPage());
        btnNext.setOnClickListener(v -> showNextPage());
    }
    
    /**
     * 加载书籍内容并分页
     */
    private void loadBookContent(Uri bookUri, String bookName) {
        try {
            // 1. 创建解析器
            BookParser parser = ParserFactory.createParser(this, bookUri);
            currentBook = parser.parse(this, bookUri);
            
            if (currentBook == null || currentBook.getChapters().isEmpty()) {
                Toast.makeText(this, "书籍内容为空", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // 3. 使用分页管理器进行分页
            PaginationManager pagination = new PaginationManager(
                mTxtContent.getPaint(),
                getScreenWidth(),
                getScreenHeight()
            );
            
            mPages = pagination.paginate(currentBook.getContent());
            calculateTotalPages();
            updatePageDisplay();
            
            // 保存初始进度
            saveReadingProgress(bookName, bookUri);
            
        } catch (IOException e) {
            Log.e(TAG, "读取书籍失败", e);
            Toast.makeText(this, "读取书籍失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        } catch (SecurityException e) {
            Log.e(TAG, "没有文件访问权限", e);
            Toast.makeText(this, "没有文件访问权限", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "参数错误", e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Log.e(TAG, "未知错误", e);
            Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * 更新页面显示
     */
    private void updatePageDisplay() {
        if (mPages == null || mPages.isEmpty()) {
            Toast.makeText(this, "书籍内容为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        mTxtContent.setText(mPages.get(mCurrentPage));
        mTxtProgress.setText(String.format("%d/%d", 
            mCurrentPage + 1, 
            mTotalPages));
        
        // 更新标题显示当前页数
        setTitle(String.format("%s (%d/%d)", currentBook.getTitle(), mCurrentPage + 1, mTotalPages));
    }
    
    private void showNextPage() {
        if (mCurrentPage < mPages.size() - 1) {
            mCurrentPage++;
            updatePageDisplay();
        }
    }
    
    private void showPreviousPage() {
        if (mCurrentPage > 0) {
            mCurrentPage--;
            updatePageDisplay();
        }
    }
    
    private void calculateTotalPages() {
        if (currentBook == null || currentBook.getChapters().isEmpty()) {
            mTotalPages = 0;
            return;
        }

        // 这里需要根据实际的分页逻辑来计算总页数
        // 示例：假设每页显示 1000 个字符
        int charsPerPage = 1000;
        int totalChars = 0;
        
        for (Chapter chapter : currentBook.getChapters()) {
            totalChars += chapter.getContent().length();
        }
        
        mTotalPages = (int) Math.ceil((double) totalChars / charsPerPage);
    }
    
    private int getScreenWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels - 32; // 减去padding
    }
    
    private int getScreenHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels - 96; // 减去控制栏高度
    }

    private void saveReadingProgress(String bookName, Uri bookUri) {
        progressManager.saveProgress(bookName, bookUri, mCurrentPage, mTotalPages);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 保存当前阅读进度
        if (currentBook != null) {
            saveReadingProgress(currentBook.getTitle(), getIntent().getParcelableExtra(EXTRA_BOOK_URI));
        }
    }
}