package com.example.pdfreader3;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;

public class BitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> implements AsyncTaskCallback {

    private final PdfRenderer renderer;
    private final Bitmap.Config config;
    private final int pagePosition;
    private final int densityDpi;
    private final AsyncTaskCallback asyncTaskCallback;
    private PdfRenderer.Page page;
    // static
    private final int RESOLUTION_DPI = 72;
    private final float FACTOR = 0.4f;

    /*
    Constructor
     */
    public BitmapWorkerTask(int pagePosition, PdfRenderer renderer, Bitmap.Config config, int densityDpi, AsyncTaskCallback asyncTaskCallback){
        this.pagePosition = pagePosition;
        this.renderer = renderer;
        this.config = config;
        this.densityDpi = densityDpi;
        this.asyncTaskCallback = asyncTaskCallback;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {

        // Log.d("WorkerTask", "doInBackground: " + pagePosition );

        page = renderer.openPage(pagePosition);
        Bitmap bitmap = newBitmap(toPixelDimension(page.getWidth()), toPixelDimension(page.getHeight()));
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        //super.onPostExecute(bitmap);
        // relay Callback to PageAdapter
        asyncTaskCallback.onSuccess(bitmap);
    }

    @Override
    public void onSuccess(Bitmap bitmap) {
        //Log.d("Callback", "onSuccess: ");
    }

    @Override
    protected void onCancelled() {
        if (page != null) page.close();
        super.onCancelled();
    }

    /*
        Helper Functions
         */
    public Bitmap newBitmap(int width, int height) {
        Bitmap nBitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(nBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(nBitmap, 0f, 0f, null);
        return nBitmap;
    }

    public int toPixelDimension(int dimension) {
        return (int) ((densityDpi * dimension / RESOLUTION_DPI) * FACTOR);
    }
}