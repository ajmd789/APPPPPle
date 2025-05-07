package com.example.appppple.ui.reader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appppple.R;
import com.example.appppple.domain.model.Bookmark;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {
    private List<Bookmark> bookmarks;
    private final OnBookmarkClickListener listener;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(Bookmark bookmark);
        void onDeleteClick(Bookmark bookmark);
    }

    public BookmarkAdapter(List<Bookmark> bookmarks, OnBookmarkClickListener listener) {
        this.bookmarks = bookmarks;
        this.listener = listener;
    }

    public void updateBookmarks(List<Bookmark> newBookmarks) {
        this.bookmarks = newBookmarks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bookmark, parent, false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        Bookmark bookmark = bookmarks.get(position);
        holder.bind(bookmark);
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    class BookmarkViewHolder extends RecyclerView.ViewHolder {
        private final TextView bookmarkPageText;
        private final ImageButton deleteButton;

        BookmarkViewHolder(View itemView) {
            super(itemView);
            bookmarkPageText = itemView.findViewById(R.id.bookmarkPageText);
            deleteButton = itemView.findViewById(R.id.btnDeleteBookmark);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onBookmarkClick(bookmarks.get(position));
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(bookmarks.get(position));
                }
            });
        }

        void bind(Bookmark bookmark) {
            bookmarkPageText.setText(String.format("第 %d 页", bookmark.getPageNumber() + 1));
        }
    }
} 