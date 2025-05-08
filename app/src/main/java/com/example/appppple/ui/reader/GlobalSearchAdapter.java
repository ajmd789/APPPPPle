package com.example.appppple.ui.reader;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appppple.R;

import java.util.ArrayList;
import java.util.List;

public class GlobalSearchAdapter extends RecyclerView.Adapter<GlobalSearchAdapter.ViewHolder> {

    private List<SearchResult> searchResults;
    private Context context;
    private OnItemClickListener listener;

    public GlobalSearchAdapter(Context context) {
        this.context = context;
        this.searchResults = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResult result = searchResults.get(position);
        holder.chapterTitle.setText(result.getChapterTitle());
        holder.contentSnippet.setText(highlightKeyword(result.getContentSnippet(), result.getKeyword()));
        holder.pageNumber.setText(String.format("第 %d 页", result.getPageNumber()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(result);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public void setResults(List<SearchResult> results) {
        this.searchResults = results;
        notifyDataSetChanged();
    }

    public void addResults(List<SearchResult> moreResults) {
        int startPosition = this.searchResults.size();
        this.searchResults.addAll(moreResults);
        notifyItemRangeInserted(startPosition, moreResults.size());
    }

    public void clearResults() {
        this.searchResults.clear();
        notifyDataSetChanged();
    }

    public void setKeyword(String keyword) {
        for (SearchResult result : searchResults) {
            result.setKeyword(keyword);
        }
        notifyDataSetChanged();
    }

    private SpannableString highlightKeyword(String content, String keyword) {
        SpannableString spannableString = new SpannableString(content);
        int startIndex = content.toLowerCase().indexOf(keyword.toLowerCase());
        if (startIndex >= 0) {
            spannableString.setSpan(new BackgroundColorSpan(context.getResources().getColor(R.color.highlight_color)),
                    startIndex, startIndex + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(SearchResult result);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView chapterTitle;
        TextView contentSnippet;
        TextView pageNumber;

        ViewHolder(View itemView) {
            super(itemView);
            chapterTitle = itemView.findViewById(R.id.chapterTitle);
            contentSnippet = itemView.findViewById(R.id.contentSnippet);
            pageNumber = itemView.findViewById(R.id.pageNumber);
        }
    }

    public static class SearchResult {
        private String chapterTitle;
        private String contentSnippet;
        private int pageNumber;
        private String keyword;

        public SearchResult(String chapterTitle, String contentSnippet, int pageNumber) {
            this.chapterTitle = chapterTitle;
            this.contentSnippet = contentSnippet;
            this.pageNumber = pageNumber;
        }

        public String getChapterTitle() {
            return chapterTitle;
        }

        public String getContentSnippet() {
            return contentSnippet;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }
    }
} 