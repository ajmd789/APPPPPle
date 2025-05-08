// ui/reader/ReaderActivity.java
package com.example.appppple.ui.reader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appppple.R;
import com.example.appppple.domain.manager.BookmarkManager;
import com.example.appppple.domain.manager.ReadingProgressManager;
import com.example.appppple.domain.model.Book;
import com.example.appppple.domain.model.Chapter;
import com.example.appppple.domain.parser.BookParser;
import com.example.appppple.domain.parser.ParserFactory;
import com.example.appppple.domain.parser.TxtParser;
import com.example.appppple.domain.model.Bookmark;

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
    private static final float SWIPE_THRESHOLD = 100; // 滑动阈值

    private TextView contentTextView;
    private TextView progressTextView;
    private View loadingLayout;
    private TextView loadingText;
    private View bookmarkHintOverlay;
    private List<String> pages;
    private int currentPage = 0;
    private int totalPages = 0;
    private Book currentBook;
    private ReadingProgressManager progressManager;
    private BookmarkManager bookmarkManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Uri currentBookUri;
    private String currentBookName;
    private GestureDetector gestureDetector;
    private float startY;
    private boolean isBookmarkHintVisible = false;
    private BookmarkAdapter bookmarkAdapter;
    private Dialog bookmarkDialog;

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
        initGestureDetector();
        progressManager = ReadingProgressManager.getInstance(this);
        bookmarkManager = BookmarkManager.getInstance(this);

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
        bookmarkHintOverlay = findViewById(R.id.bookmarkHintOverlay);

        // 移除重复的点击监听器，只使用GestureDetector处理点击事件
        contentTextView.setClickable(true);
        contentTextView.setFocusable(true);

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

        findViewById(R.id.btnJump).setOnClickListener(v -> showJumpPageDialog());
        
        findViewById(R.id.btnBookmarks).setOnClickListener(v -> showBookmarkListDialog());
    }

    private void initGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                startY = e.getY();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (e1 == null || e2 == null) return false;

                float deltaY = e2.getY() - startY;
                if (deltaY > SWIPE_THRESHOLD && !isBookmarkHintVisible) {
                    showBookmarkHint();
                }
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d(TAG, "检测到单击事件");
                // 检查点击是否在contentTextView的范围内
                int[] location = new int[2];
                contentTextView.getLocationOnScreen(location);
                int left = location[0];
                int top = location[1];
                int right = left + contentTextView.getWidth();
                int bottom = top + contentTextView.getHeight();

                if (e.getRawX() >= left && e.getRawX() <= right &&
                    e.getRawY() >= top && e.getRawY() <= bottom) {
                    Log.d(TAG, "点击在contentTextView范围内，显示中央菜单");
                    showCenterMenu();
                    return true;
                }
                return false;
            }
        });

        // 设置contentTextView的触摸监听器
        contentTextView.setOnTouchListener((v, event) -> {
            Log.d(TAG, "contentTextView触摸事件: " + event.getAction());
            return gestureDetector.onTouchEvent(event);
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

        // 检查当前页是否有书签
        if (currentBookUri != null && bookmarkManager.isBookmarked(currentBookUri, currentPage)) {
            contentTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_bookmark, 0);
        } else {
            contentTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    private void saveReadingProgress() {
        if (currentBookUri != null && currentBookName != null) {
            progressManager.saveProgress(currentBookName, currentBookUri, currentPage, totalPages);
        }
    }

    /**
     * 显示跳页对话框
     */
    private void showJumpPageDialog() {
        if (pages == null || pages.isEmpty()) {
            Toast.makeText(this, "书籍内容为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建输入对话框
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("跳转到指定页");

        // 创建输入框
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint(String.format("请输入页码 (1-%d)", totalPages));
        builder.setView(input);

        // 设置按钮
        builder.setPositiveButton("确定", (dialog, which) -> {
            String pageStr = input.getText().toString();
            if (!pageStr.isEmpty()) {
                try {
                    int targetPage = Integer.parseInt(pageStr) - 1; // 转换为0基索引
                    if (targetPage >= 0 && targetPage < totalPages) {
                        currentPage = targetPage;
                        updatePageDisplay();
                        saveReadingProgress();
                    } else {
                        Toast.makeText(this, 
                            String.format("页码必须在1-%d之间", totalPages), 
                            Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "请输入有效的页码", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        // 显示对话框
        builder.show();
    }

    private void showBookmarkHint() {
        isBookmarkHintVisible = true;
        bookmarkHintOverlay.setVisibility(View.VISIBLE);
        bookmarkHintOverlay.setAlpha(0f);
        bookmarkHintOverlay.animate()
                .alpha(1f)
                .setDuration(200)
                .start();
    }

    private void hideBookmarkHint() {
        isBookmarkHintVisible = false;
        bookmarkHintOverlay.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> bookmarkHintOverlay.setVisibility(View.GONE))
                .start();
    }

    private void handleBookmarkAction() {
        if (currentBookUri == null || currentBookName == null) return;

        boolean hasBookmark = bookmarkManager.isBookmarked(currentBookUri, currentPage);
        if (hasBookmark) {
            bookmarkManager.toggleBookmark(currentBookName, currentBookUri, currentPage);
            Toast.makeText(this, "书签已删除", Toast.LENGTH_SHORT).show();
        } else {
            bookmarkManager.toggleBookmark(currentBookName, currentBookUri, currentPage);
            Toast.makeText(this, "书签已添加", Toast.LENGTH_SHORT).show();
        }
    }

    private void showBookmarkListDialog() {
        if (currentBookUri == null) return;

        bookmarkDialog = new Dialog(this);
        bookmarkDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bookmarkDialog.setContentView(R.layout.dialog_bookmark_list);

        RecyclerView recyclerView = bookmarkDialog.findViewById(R.id.bookmarkRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Bookmark> bookmarks = bookmarkManager.getBookmarksForBook(currentBookUri);
        bookmarkAdapter = new BookmarkAdapter(bookmarks, new BookmarkAdapter.OnBookmarkClickListener() {
            @Override
            public void onBookmarkClick(Bookmark bookmark) {
                currentPage = bookmark.getPage();
                updatePageDisplay();
                saveReadingProgress();
                bookmarkDialog.dismiss();
            }

            @Override
            public void onDeleteClick(Bookmark bookmark) {
                bookmarkManager.toggleBookmark(currentBookName, currentBookUri, bookmark.getPage());
                bookmarkAdapter.updateBookmarks(bookmarkManager.getBookmarksForBook(currentBookUri));
                Toast.makeText(ReaderActivity.this, "书签已删除", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(bookmarkAdapter);
        bookmarkDialog.show();
    }

    private void showCenterMenu() {
        Log.d(TAG, "显示中央菜单");
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_center_menu);

        // 设置对话框宽度为屏幕宽度的80%
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
            window.setAttributes(params);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 设置目录按钮点击事件
        dialog.findViewById(R.id.btnCatalog).setOnClickListener(v -> {
            Log.d(TAG, "点击了目录按钮");
            dialog.dismiss();
            showCatalogDialog();
        });

        // 设置搜索按钮点击事件
        dialog.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Log.d(TAG, "点击了搜索按钮");
            dialog.dismiss();
            showSearchDialog();
        });

        dialog.show();
    }

    private void showCatalogDialog() {
        if (currentBook == null || currentBook.getChapters() == null) {
            Toast.makeText(this, "目录信息不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_catalog);

        RecyclerView recyclerView = dialog.findViewById(R.id.catalogRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CatalogAdapter adapter = new CatalogAdapter(currentBook.getChapters(), chapter -> {
            // 计算章节对应的页码
            int targetPage = calculatePageForChapter(chapter);
            if (targetPage >= 0) {
                currentPage = targetPage;
                updatePageDisplay();
                saveReadingProgress();
            }
            dialog.dismiss();
        });

        recyclerView.setAdapter(adapter);
        dialog.show();
    }

    private void showSearchDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_global_search);

        EditText searchInput = dialog.findViewById(R.id.searchInput);
        TextView resultCount = dialog.findViewById(R.id.resultCount);
        RecyclerView searchResults = dialog.findViewById(R.id.searchResults);
        searchResults.setLayoutManager(new LinearLayoutManager(this));

        GlobalSearchAdapter adapter = new GlobalSearchAdapter(this);
        searchResults.setAdapter(adapter);

        // 设置搜索监听
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                if (keyword.length() > 0) {
                    searchInBook(keyword, adapter, resultCount);
                } else {
                    adapter.clearResults();
                    resultCount.setText("");
                }
            }
        });

        // 设置搜索结果点击事件
        adapter.setOnItemClickListener(result -> {
            currentPage = result.getPageNumber();
            updatePageDisplay();
            saveReadingProgress();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void searchInBook(String keyword, GlobalSearchAdapter adapter, TextView resultCount) {
        if (pages == null || pages.isEmpty()) return;

        List<GlobalSearchAdapter.SearchResult> results = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            String pageContent = pages.get(i);
            if (pageContent.toLowerCase().contains(keyword.toLowerCase())) {
                // 获取包含关键词的上下文
                int start = Math.max(0, pageContent.toLowerCase().indexOf(keyword.toLowerCase()) - 20);
                int end = Math.min(pageContent.length(), start + 60);
                String snippet = pageContent.substring(start, end);
                if (start > 0) snippet = "..." + snippet;
                if (end < pageContent.length()) snippet = snippet + "...";

                results.add(new GlobalSearchAdapter.SearchResult(
                    "第" + (i + 1) + "页",
                    snippet,
                    i
                ));
            }
        }

        adapter.setResults(results);
        adapter.setKeyword(keyword);
        resultCount.setText(String.format("找到 %d 个结果", results.size()));
    }

    private int calculatePageForChapter(Chapter chapter) {
        if (pages == null || pages.isEmpty()) return -1;

        // 遍历所有页面，查找包含章节标题的页面
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).contains(chapter.getTitle())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        if (bookmarkDialog != null && bookmarkDialog.isShowing()) {
            bookmarkDialog.dismiss();
        }
    }
}