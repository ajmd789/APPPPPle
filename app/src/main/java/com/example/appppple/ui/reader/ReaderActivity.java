// ui/reader/ReaderActivity.java
package com.example.appppple.ui.reader;

import android.net.Uri;
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
        try {
            // 1. 从Intent获取文件URI
            Uri uri = getIntent().getData();
            if (uri == null) {
                throw new IllegalArgumentException("未选择书籍文件");
            }

            // 2. 创建解析器
            BookParser parser = ParserFactory.createParser(this, uri);
            Book book = parser.parse(this, uri);
            
            // 3. 使用分页管理器进行分页
            PaginationManager pagination = new PaginationManager(
                mTxtContent.getPaint(),
                getScreenWidth(),
                getScreenHeight()
            );
            
            mPages = pagination.paginate(book.getContent());
            updatePageDisplay();
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