package com.example.appppple.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appppple.R;
import com.example.appppple.ui.reader.ReaderActivity;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_BOOK_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSelectBook = findViewById(R.id.btnSelectBook);
        btnSelectBook.setOnClickListener(v -> selectBook());
    }

    private void selectBook() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_BOOK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_BOOK_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                String filePath = data.getData().getPath();
                Intent readerIntent = new Intent(this, ReaderActivity.class);
                readerIntent.putExtra("BOOK_PATH", filePath);
                startActivity(readerIntent);
            } else {
                Toast.makeText(this, "选择文件失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 