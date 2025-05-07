package com.example.appppple.domain.manager;

import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class UriTypeAdapter implements JsonSerializer<Uri>, JsonDeserializer<Uri> {
    private static final String TAG = "UriTypeAdapter";

    @Override
    public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        
        if (json.isJsonPrimitive()) {
            String uriString = json.getAsString();
            return uriString.isEmpty() ? null : Uri.parse(uriString);
        }
        
        if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            if (jsonObject.has("uri")) {
                String uriString = jsonObject.get("uri").getAsString();
                return uriString.isEmpty() ? null : Uri.parse(uriString);
            }
        }
        
        Log.w(TAG, "无法解析 Uri JSON: " + json);
        return null;
    }
} 