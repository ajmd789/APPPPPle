// domain/pagination/PaginationManager.java
package com.example.appppple.domain.pagination;

import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本分页管理器
 */
public class PaginationManager {
    private final TextPaint mPaint;
    private final int mPageWidth;
    private final int mPageHeight;
    private final float mLineSpacingExtra;
    private final float mLineSpacingMultiplier;

    public PaginationManager(TextPaint paint, int pageWidth, int pageHeight) {
        this(paint, pageWidth, pageHeight, 0, 1.0f);
    }

    public PaginationManager(TextPaint paint, int pageWidth, int pageHeight, 
                           float lineSpacingExtra, float lineSpacingMultiplier) {
        this.mPaint = paint;
        this.mPageWidth = pageWidth;
        this.mPageHeight = pageHeight;
        this.mLineSpacingExtra = lineSpacingExtra;
        this.mLineSpacingMultiplier = lineSpacingMultiplier;
    }

    /**
     * 将文本分页
     * @param text 要分页的文本
     * @return 分页后的文本列表
     */
    public List<String> paginate(String text) {
        if (TextUtils.isEmpty(text)) {
            return new ArrayList<>();
        }

        List<String> pages = new ArrayList<>();
        StringBuilder currentPage = new StringBuilder();
        float currentHeight = 0;
        float lineHeight = mPaint.getFontMetrics(null) * mLineSpacingMultiplier + mLineSpacingExtra;

        // 按行分割文本
        String[] lines = text.split("\n");
        for (String line : lines) {
            // 如果当前行是空行，直接添加
            if (line.trim().isEmpty()) {
                currentPage.append("\n");
                currentHeight += lineHeight;
                continue;
            }

            // 测量当前行的宽度
            float lineWidth = mPaint.measureText(line);
            
            // 如果行宽度超过页面宽度，需要换行
            if (lineWidth > mPageWidth) {
                // 计算每行可以容纳的字符数
                int charsPerLine = (int) (mPageWidth / mPaint.measureText("中")); // 使用中文字符作为基准
                
                // 将长行分割成多行
                int start = 0;
                while (start < line.length()) {
                    int end = Math.min(start + charsPerLine, line.length());
                    String subLine = line.substring(start, end);
                    
                    // 检查是否需要换页
                    if (currentHeight + lineHeight > mPageHeight) {
                        pages.add(currentPage.toString());
                        currentPage = new StringBuilder();
                        currentHeight = 0;
                    }
                    
                    currentPage.append(subLine).append("\n");
                    currentHeight += lineHeight;
                    start = end;
                }
            } else {
                // 检查是否需要换页
                if (currentHeight + lineHeight > mPageHeight) {
                    pages.add(currentPage.toString());
                    currentPage = new StringBuilder();
                    currentHeight = 0;
                }
                
                currentPage.append(line).append("\n");
                currentHeight += lineHeight;
            }
        }

        // 添加最后一页
        if (currentPage.length() > 0) {
            pages.add(currentPage.toString());
        }

        return pages;
    }
}