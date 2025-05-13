package com.example.appppple.data.converter;

import androidx.room.TypeConverter;

import android.net.Uri;

/**
 * Uri 类型转换器
 */
public class UriConverter {
    @TypeConverter
    public static String fromUri(Uri uri) {
        return uri == null ? null : uri.toString();
    }

    @TypeConverter
    public static Uri toUri(String uriString) {
        return uriString == null ? null : Uri.parse(uriString);
    }
} 