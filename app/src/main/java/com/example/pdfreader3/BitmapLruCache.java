package com.example.pdfreader3;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;


public class BitmapLruCache extends LruCache<Integer, Bitmap> implements ImageCache {

    private static final int DEFAULT_CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / 1024) / 8;
    String TAG = BitmapLruCache.class.getSimpleName();

    public BitmapLruCache() {
        this(DEFAULT_CACHE_SIZE);
    }

    public BitmapLruCache(int maxSize) {
        super(maxSize);
        Log.d(TAG, "BitmapLruCache available Size: " + maxSize + "kb");
    }

    @Override
    public Bitmap getBitmap(int key) {
        return get(key);
    }

    @Override
    public void putBitmap(int key, Bitmap bitmap) {
        if(getBitmap(key) == null) {
            put(key, bitmap);
        }
    }

    @Override
    protected int sizeOf(Integer key, Bitmap value) {
        //return super.sizeOf(key, value);
        return value.getByteCount() / 1024;
    }

    @Override
    public boolean containsBitmap(int key) {
        return get(key) != null;
    }


    /*
    @Override
    protected int sizeOf(int key, Bitmap value) {
        return value == null ? 0 : value.getRowBytes() * value.getHeight() / 1024;
    }
    */
}
