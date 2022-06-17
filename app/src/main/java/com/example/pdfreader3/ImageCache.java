package com.example.pdfreader3;

import android.graphics.Bitmap;

public interface ImageCache {
    Bitmap getBitmap(int key);
    void putBitmap(int key, Bitmap bitmap);
    boolean containsBitmap(int key);
}



