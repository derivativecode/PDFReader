package com.example.pdfreader3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;

public class PageAdapter extends RecyclerView.Adapter<PageAdapter.PageViewHolder> {

    private final Context context;
    private PdfRenderer renderer;
    private final ParcelFileDescriptor pdf;

    private BitmapLruCache lruCache;
    private final int densityDpi;

    // static
    private final Bitmap.Config config = Bitmap.Config.ARGB_8888;
    private final int RESOLUTION_DPI = 72;
    private final int POOL_SIZE = 5;

    String TAG = PageAdapter.class.getSimpleName();

    /*
    Constructor
     */
    public PageAdapter(Context context, ParcelFileDescriptor pfd){
        this.context = context;
        this.pdf = pfd;

        densityDpi = context.getResources().getDisplayMetrics().densityDpi;
        initAdapter();
    }

    public void initAdapter(){
        // init cache
        lruCache = new BitmapLruCache();

        // init renderer
        try { renderer = new PdfRenderer(pdf); } catch (IOException e) { e.printStackTrace(); }

        // load first x pages
        int initialLoadCount = Math.min(renderer.getPageCount(), POOL_SIZE);
        for (int i=0; i<initialLoadCount; i++){
            int finalI = i;
            new BitmapWorkerTask(i, renderer, config, densityDpi, bitmap -> {
                Log.d(TAG, "initAdapter: page " + finalI + " loaded");
                lruCache.putBitmap(finalI, bitmap);
            }).execute();
        }
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.screen_slide_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public void onViewRecycled(@NonNull PageViewHolder holder) {
        super.onViewRecycled(holder);
        // RECYCLE??
        // lruCache.remove(holder.getAdapterPosition());
        Log.d(TAG, "[X] onViewRecycled: " + holder.getAdapterPosition() + " || LRU Size : " + lruCache.size() + " || LRU Evictions: " + lruCache.evictionCount());
    }


    @Override
    public int getItemCount() {
        return renderer.getPageCount();
    }

    public void clear(){
        if (renderer != null) renderer.close();
        Log.d(TAG, "[X] cleared: Render closed ");
        lruCache.evictAll();
        Log.d(TAG, "[X] cleared: LRU || Evicted: " + lruCache.evictionCount() + " || Size: " + lruCache.size());
    }

    public class PageViewHolder extends RecyclerView.ViewHolder {

        View view;
//        CustomImageView imageViewPage;
        ImageView imageViewPage;
        ProgressBar progressBar;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
        }

        public void bind(int pagePosition) {
            imageViewPage = this.view.findViewById(R.id.imageViewPage);
            progressBar = this.view.findViewById(R.id.progressBar);

            if (lruCache.getBitmap(pagePosition)!=null) {
                Log.d(TAG, "bind: " + pagePosition + " from CACHE");
                    imageViewPage.setImageBitmap(lruCache.getBitmap(pagePosition));
                    imageViewPage.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "bind: " + pagePosition + " from STORAGE");
                new BitmapWorkerTask(pagePosition, renderer, config, densityDpi, bitmap -> {
                    lruCache.putBitmap(pagePosition, bitmap);
                    imageViewPage.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    imageViewPage.setImageBitmap(bitmap);
                }).execute();
            }

        }
    }
}
