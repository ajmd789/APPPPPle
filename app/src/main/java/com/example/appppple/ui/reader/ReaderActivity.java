// ui/reader/ReaderActivity.java
package com.example.appppple.ui.reader;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appppple.R;
import com.example.appppple.domain.model.Book;
import com.example.appppple.domain.parser.BookParser;
import com.example.appppple.domain.parser.ParserFactory;
import com.example.appppple.domain.pagination.PaginationManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ReaderActivity extends AppCompatActivity {
    private static final String TAG = "ReaderActivity";
    
    private TextView mTxtContent;
    private TextView mTxtProgress;
    private List<String> mPages;
    private int mCurrentPage = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_activity);
        
        // 初始化视图
        mTxtContent = findViewById(R.id.txtContent);
        mTxtProgress = findViewById(R.id.txtProgress);
        Button btnPrev = findViewById(R.id.btnPrev);
        Button btnNext = findViewById(R.id.btnNext);
        
        // 加载书籍内容
        loadBookContent();
        
        // 按钮事件
        btnPrev.setOnClickListener(v -> showPreviousPage());
        btnNext.setOnClickListener(v -> showNextPage());
    }
    
    /**
     * 加载书籍内容并分页
     */
    private void loadBookContent() {
        // 1. 从Intent获取文件路径
        String filePath = getIntent().getStringExtra("BOOK_PATH");
        
        // 2. 创建解析器（工厂模式便于扩展）
        BookParser parser = ParserFactory.createParser(filePath);
        try {
            Book book = parser.parse(new File(filePath));
            
            // 3. 使用分页管理器进行分页
            PaginationManager pagination = new PaginationManager(
                mTxtContent.getPaint(),
                getScreenWidth(),
                getScreenHeight()
            );
            
            mPages = pagination.paginate(book.getContent());
            updatePageDisplay();
        } catch (IOException e) {
            Log.e(TAG, "Error reading book", e);
            Toast.makeText(this, "打开书籍失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * 更新页面显示
     */
    private void updatePageDisplay() {
        if (mPages == null || mPages.isEmpty()) return;
        
        mTxtContent.setText(mPages.get(mCurrentPage));
        mTxtProgress.setText(String.format("%d/%d", 
            mCurrentPage + 1, 
            mPages.size()));
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
    
    // 获取屏幕尺寸
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
}