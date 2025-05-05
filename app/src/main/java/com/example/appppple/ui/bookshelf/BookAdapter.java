package com.example.appppple.ui.bookshelf;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appppple.R;
import com.example.appppple.domain.manager.ReadingProgressManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<ReadingProgressManager.ReadingProgress> books;
    private final OnBookClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public interface OnBookClickListener {
        void onBookClick(ReadingProgressManager.ReadingProgress progress);
    }

    public BookAdapter(List<ReadingProgressManager.ReadingProgress> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }

    public void updateBooks(List<ReadingProgressManager.ReadingProgress> newBooks) {
        this.books = newBooks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        ReadingProgressManager.ReadingProgress progress = books.get(position);
        holder.bind(progress);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private final ImageView bookCoverImageView;
        private final TextView bookTitleTextView;
        private final TextView bookProgressTextView;
        private final TextView lastReadTimeTextView;

        BookViewHolder(View itemView) {
            super(itemView);
            bookCoverImageView = itemView.findViewById(R.id.bookCoverImageView);
            bookTitleTextView = itemView.findViewById(R.id.bookTitleTextView);
            bookProgressTextView = itemView.findViewById(R.id.bookProgressTextView);
            lastReadTimeTextView = itemView.findViewById(R.id.lastReadTimeTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onBookClick(books.get(position));
                }
            });
        }

        void bind(ReadingProgressManager.ReadingProgress progress) {
            bookTitleTextView.setText(progress.getBookName());
            bookProgressTextView.setText(String.format("阅读进度: %d/%d", 
                progress.getCurrentPage() + 1, 
                progress.getTotalPages()));
            lastReadTimeTextView.setText(String.format("上次阅读: %s", 
                dateFormat.format(new Date(progress.getLastReadTime()))));
        }
    }
} 