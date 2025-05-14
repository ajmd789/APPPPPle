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
    private Book book;
    private List<Bookmark> bookmarks;

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
        progressManager.getLastReadingProgress().observe(this, lastProgress -> {
            if (lastProgress != null && lastProgress.getBookUri().equals(currentBookUri)) {
                currentPage = lastProgress.getCurrentPage();
                totalPages = lastProgress.getTotalPages();
                Log.d(TAG, String.format("恢复阅读进度 - 书名: %s, 当前页: %d/%d",
                        currentBookName, currentPage, totalPages));
            }
        });

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

        // 设置内容区域的触摸监听
        contentTextView.setOnTouchListener((v, event) -> {
            if (gestureDetector != null) {
                gestureDetector.onTouchEvent(event);
            }
            return true;
        });

        // 上一页区域点击处理
        View prevPageArea = findViewById(R.id.prevPageArea);
        prevPageArea.setOnClickListener(v -> {
            Log.d(TAG, "上一页区域被点击");
            if (currentPage > 0) {
                Log.d(TAG, String.format("从第 %d 页翻到第 %d 页", currentPage + 1, currentPage));
                currentPage--;
                updatePageDisplay();
                saveReadingProgress();
            } else {
                Log.d(TAG, "已经是第一页，无法继续向前翻页");
            }
        });

        // 下一页区域点击处理
        View nextPageArea = findViewById(R.id.nextPageArea);
        nextPageArea.setOnClickListener(v -> {
            Log.d(TAG, "下一页区域被点击");
            if (currentPage < totalPages - 1) {
                Log.d(TAG, String.format("从第 %d 页翻到第 %d 页", currentPage + 1, currentPage + 2));
                currentPage++;
                updatePageDisplay();
                saveReadingProgress();
            } else {
                Log.d(TAG, "已经是最后一页，无法继续向后翻页");
            }
        });

        // 上一页按钮点击处理
        findViewById(R.id.btnPrev).setOnClickListener(v -> {
            Log.d(TAG, "上一页按钮被点击");
            if (currentPage > 0) {
                Log.d(TAG, String.format("从第 %d 页翻到第 %d 页", currentPage + 1, currentPage));
                currentPage--;
                updatePageDisplay();
                saveReadingProgress();
            } else {
                Log.d(TAG, "已经是第一页，无法继续向前翻页");
            }
        });

        // 下一页按钮点击处理
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            Log.d(TAG, "下一页按钮被点击");
            if (currentPage < totalPages - 1) {
                Log.d(TAG, String.format("从第 %d 页翻到第 %d 页", currentPage + 1, currentPage + 2));
                currentPage++;
                updatePageDisplay();
                saveReadingProgress();
            } else {
                Log.d(TAG, "已经是最后一页，无法继续向后翻页");
            }
        });

        findViewById(R.id.btnJump).setOnClickListener(v -> {
            Log.d(TAG, "跳转按钮被点击");
            showJumpPageDialog();
        });
        
        findViewById(R.id.btnBookmarks).setOnClickListener(v -> {
            Log.d(TAG, "书签按钮被点击");
            showBookmarkListDialog();
        });
    }

    private void initGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_MIN_DISTANCE = 120;
            private static final int SWIPE_THRESHOLD_VELOCITY = 200;

            @Override
            public boolean onDown(MotionEvent e) {
                Log.d(TAG, "onDown: 手势开始");
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    if (e1 == null || e2 == null) {
                        Log.d(TAG, "onFling: 事件为空");
                        return false;
                    }

                    float diffX = e2.getX() - e1.getX();
                    float diffY = e2.getY() - e1.getY();
                    
                    Log.d(TAG, String.format("onFling: diffX=%.2f, diffY=%.2f, velocityX=%.2f, velocityY=%.2f",
                            diffX, diffY, velocityX, velocityY));

                    // 确保水平滑动距离大于垂直滑动距离
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        // 确保滑动距离足够大
                        if (Math.abs(diffX) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            if (diffX > 0) {
                                // 向右滑动，显示上一页
                                if (currentPage > 0) {
                                    Log.d(TAG, "onFling: 向右滑动，显示上一页");
                                    currentPage--;
                                    updatePageDisplay();
                                    saveReadingProgress();
                                    return true;
                                }
                            } else {
                                // 向左滑动，显示下一页
                                if (currentPage < totalPages - 1) {
                                    Log.d(TAG, "onFling: 向左滑动，显示下一页");
                                    currentPage++;
                                    updatePageDisplay();
                                    saveReadingProgress();
                                    return true;
                                }
                            }
                        } else {
                            Log.d(TAG, String.format("onFling: 滑动距离或速度不足 - 距离: %.2f, 速度: %.2f",
                                    Math.abs(diffX), Math.abs(velocityX)));
                        }
                    } else {
                        Log.d(TAG, "onFling: 垂直滑动距离大于水平滑动距离");
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "onFling: 手势检测异常", ex);
                }
                return false;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                try {
                    // 获取屏幕宽度
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    float x = e.getX();
                    
                    Log.d(TAG, String.format("onSingleTapConfirmed: x=%.2f, screenWidth=%d", x, screenWidth));
                    
                    // 如果点击在屏幕左侧1/3区域，显示上一页
                    if (x < screenWidth / 3) {
                        if (currentPage > 0) {
                            Log.d(TAG, "onSingleTapConfirmed: 点击左侧区域，显示上一页");
                            currentPage--;
                            updatePageDisplay();
                            saveReadingProgress();
                            return true;
                        }
                    }
                    // 如果点击在屏幕右侧1/3区域，显示下一页
                    else if (x > screenWidth * 2 / 3) {
                        if (currentPage < totalPages - 1) {
                            Log.d(TAG, "onSingleTapConfirmed: 点击右侧区域，显示下一页");
                            currentPage++;
                            updatePageDisplay();
                            saveReadingProgress();
                            return true;
                        }
                    }
                    // 如果点击在屏幕中间1/3区域，显示菜单
                    else {
                        Log.d(TAG, "onSingleTapConfirmed: 点击中间区域，显示菜单");
                        showCenterMenu();
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "onSingleTapConfirmed: 点击事件处理异常", ex);
                }
                return true;
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

    private void loadBookContentAsync(Uri uri, String fileName) {
        executor.execute(() -> {
            try {
                BookParser parser = ParserFactory.createParser(this, uri);
                if (parser == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "不支持的文件格式", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                Log.d(TAG, "创建解析器成功: " + parser.getClass().getSimpleName());
                Book book = parser.parse(this, uri);
                if (book == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "解析文件失败", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                Log.d(TAG, "解析完成，书籍信息: 标题=" + book.getTitle() + ", 章节数=" + book.getChapters().size());
                book.setUri(uri);
                book.setFileName(fileName);

                // 分页处理
                runOnUiThread(() -> showLoading("正在分页..."));
                this.book = book; // 先设置 book 对象
                book.paginate(CHARS_PER_PAGE, new Book.PaginationListener() {
                    @Override
                    public void onPaginationProgress(int currentPage, int totalPages) {
                        runOnUiThread(() -> {
                            showLoading(String.format("正在分页... %d/%d", currentPage, totalPages));
                        });
                    }

                    @Override
                    public void onPaginationComplete(List<String> pages) {
                        runOnUiThread(() -> {
                            hideLoading();
                            if (pages != null && !pages.isEmpty()) {
                                ReaderActivity.this.pages = pages;
                                totalPages = pages.size();
                                updatePageContent();
                                setupBookmarkObserver();
                            } else {
                                Toast.makeText(ReaderActivity.this, "分页失败：内容为空", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onPaginationError(Exception e) {
                        runOnUiThread(() -> {
                            hideLoading();
                            Toast.makeText(ReaderActivity.this, "分页失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "加载书籍失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "加载书籍失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void setupBookmarkObserver() {
        if (book != null) {
            bookmarkManager.getBookmarksForBook(book.getUri())
                .observe(this, bookmarks -> {
                    if (bookmarks != null) {
                        this.bookmarks = bookmarks;
                        updateBookmarkButton();
                    }
                });
        }
    }

    private void updatePageContent() {
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
        setTitle(String.format("%s (%d/%d)", book.getTitle(), currentPage + 1, totalPages));

        // 检查当前页是否有书签
        if (book.getUri() != null) {
            bookmarkManager.isBookmarked(book.getUri(), currentPage).observe(this, isBookmarked -> {
                if (isBookmarked) {
                    contentTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_bookmark, 0);
                } else {
                    contentTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            });
        }
    }

    private void saveReadingProgress() {
        if (book != null && book.getUri() != null) {
            progressManager.saveProgress(book.getFileName(), book.getUri(), currentPage, totalPages)
                .observe(this, success -> {
                    if (success) {
                        Log.d(TAG, String.format("保存阅读进度 - 书名: %s, 当前页: %d/%d",
                                book.getFileName(), currentPage, totalPages));
                    }
                });
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
        if (book.getUri() == null || book.getFileName() == null) return;

        bookmarkManager.isBookmarked(book.getUri(), currentPage).observe(this, hasBookmark -> {
            bookmarkManager.toggleBookmark(book.getFileName(), book.getUri(), currentPage)
                .observe(this, success -> {
                    if (success) {
                        Toast.makeText(this, hasBookmark ? "书签已删除" : "书签已添加", Toast.LENGTH_SHORT).show();
                    }
                });
        });
    }

    private void showBookmarkListDialog() {
        if (book.getUri() == null) return;

        bookmarkDialog = new Dialog(this);
        bookmarkDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bookmarkDialog.setContentView(R.layout.dialog_bookmark_list);

        RecyclerView recyclerView = bookmarkDialog.findViewById(R.id.bookmarkRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookmarkManager.getBookmarksForBook(book.getUri()).observe(this, bookmarks -> {
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
                    bookmarkManager.toggleBookmark(book.getFileName(), book.getUri(), bookmark.getPage())
                        .observe(ReaderActivity.this, success -> {
                            if (success) {
                                Toast.makeText(ReaderActivity.this, "书签已删除", Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            });

            recyclerView.setAdapter(bookmarkAdapter);
        });

        bookmarkDialog.show();
    }

    private void showCenterMenu() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_center_menu);
        
        dialog.findViewById(R.id.btnCatalog).setOnClickListener(v -> {
            dialog.dismiss();
            showCatalogDialog();
        });
        
        dialog.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            dialog.dismiss();
            showSearchDialog();
        });
        
        dialog.show();
    }

    private void showCatalogDialog() {
        if (book == null || book.getChapters() == null) {
            Toast.makeText(this, "目录信息不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_catalog);

        RecyclerView recyclerView = dialog.findViewById(R.id.catalogRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CatalogAdapter adapter = new CatalogAdapter(book.getChapters(), chapter -> {
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
        setTitle(String.format("%s (%d/%d)", book.getTitle(), currentPage + 1, totalPages));

        // 检查当前页是否有书签
        if (book.getUri() != null) {
            bookmarkManager.isBookmarked(book.getUri(), currentPage).observe(this, isBookmarked -> {
                if (isBookmarked) {
                    contentTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_bookmark, 0);
                } else {
                    contentTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            });
        }
    }

    private void updateBookmarkButton() {
        if (bookmarks != null && !bookmarks.isEmpty()) {
            bookmarkAdapter.updateBookmarks(bookmarks);
        }
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