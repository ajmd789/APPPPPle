package com.example.appppple.domain.model;

import android.net.Uri;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 书籍模型类
 */
public class Book {
    private static final String TAG = "Book";
    private static final int BUFFER_SIZE = 8192; // 8KB 缓冲区
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String mTitle;
    private List<Chapter> mChapters;
    private String mAuthor;
    private String mCoverPath;
    private long mLastReadTime;
    private int mLastReadPosition;
    private Uri mUri;
    private String mFileName;
    private List<String> mPages;
    private AtomicInteger mTotalPages;
    private boolean mIsPaginating;

    public interface PaginationListener {
        void onPaginationProgress(int currentPage, int totalPages);
        void onPaginationComplete(List<String> pages);
        void onPaginationError(Exception e);
    }

    public Book() {
        mLastReadTime = System.currentTimeMillis();
        mLastReadPosition = 0;
        mPages = new ArrayList<>();
        mTotalPages = new AtomicInteger(0);
        mIsPaginating = false;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public List<Chapter> getChapters() {
        return mChapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.mChapters = chapters;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        this.mAuthor = author;
    }

    public String getCoverPath() {
        return mCoverPath;
    }

    public void setCoverPath(String coverPath) {
        this.mCoverPath = coverPath;
    }

    public long getLastReadTime() {
        return mLastReadTime;
    }

    public void setLastReadTime(long lastReadTime) {
        this.mLastReadTime = lastReadTime;
    }

    public int getLastReadPosition() {
        return mLastReadPosition;
    }

    public void setLastReadPosition(int lastReadPosition) {
        this.mLastReadPosition = lastReadPosition;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        this.mUri = uri;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    public List<String> getPages() {
        return mPages;
    }

    public int getTotalPages() {
        return mTotalPages.get();
    }

    public boolean isPaginating() {
        return mIsPaginating;
    }

    /**
     * 获取书籍内容
     * @return 所有章节内容的组合
     */
    public String getContent() {
        if (mChapters == null || mChapters.isEmpty()) {
            return "";
        }

        StringBuilder content = new StringBuilder();
        for (Chapter chapter : mChapters) {
            content.append(chapter.getTitle()).append("\n\n");
            content.append(chapter.getContent()).append("\n\n");
        }
        return content.toString();
    }

    /**
     * 将书籍内容分页
     * @param charsPerPage 每页的字符数
     * @param listener 分页进度监听器
     */
    public void paginate(int charsPerPage, PaginationListener listener) {
        if (charsPerPage <= 0) {
            throw new IllegalArgumentException("每页字符数必须大于0");
        }

        if (mIsPaginating) {
            Log.w(TAG, "分页正在进行中，忽略新的分页请求");
            return;
        }

        mIsPaginating = true;
        executor.execute(() -> {
            try {
                String content = getContent();
                if (content.isEmpty()) {
                    mPages.clear();
                    mTotalPages.set(0);
                    if (listener != null) {
                        listener.onPaginationComplete(mPages);
                    }
                    return;
                }

                List<String> newPages = new ArrayList<>();
                int length = content.length();
                int start = 0;
                int pageCount = 0;

                while (start < length) {
                    int end = Math.min(start + charsPerPage, length);
                    
                    // 如果不是最后一页，尝试在合适的位置分页
                    if (end < length) {
                        // 查找最后一个换行符或空格
                        int lastNewline = content.lastIndexOf('\n', end);
                        int lastSpace = content.lastIndexOf(' ', end);
                        
                        // 选择最接近分页点的位置
                        if (lastNewline > start && lastNewline > lastSpace) {
                            end = lastNewline;
                        } else if (lastSpace > start) {
                            end = lastSpace;
                        }
                    }
                    
                    String page = content.substring(start, end).trim();
                    newPages.add(page);
                    pageCount++;
                    
                    // 每处理10页通知一次进度
                    if (pageCount % 10 == 0 && listener != null) {
                        final int currentPage = pageCount;
                        listener.onPaginationProgress(currentPage, (int) Math.ceil((double) length / charsPerPage));
                    }
                    
                    start = end + 1;
                }

                mPages = newPages;
                mTotalPages.set(pageCount);
                
                if (listener != null) {
                    listener.onPaginationComplete(mPages);
                }
            } catch (Exception e) {
                Log.e(TAG, "分页过程发生错误", e);
                if (listener != null) {
                    listener.onPaginationError(e);
                }
            } finally {
                mIsPaginating = false;
            }
        });
    }
} 