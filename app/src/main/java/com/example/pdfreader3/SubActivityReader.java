package com.example.pdfreader3;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public class SubActivityReader extends AppCompatActivity {

    private DocumentDatabase documentDatabase;
    private static ViewPager2 mPager;
    private ViewPager2.OnPageChangeCallback viewPagerOnPageChangeCallback;
    private PageAdapter pageAdapter;
    private ParcelFileDescriptor parcelFileDescriptor;
    private TextView tvPageCounter;
    private Uri uri;
    private int pagerPosition = 0;

    String TAG = SubActivityReader.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_reader);

        documentDatabase = DocumentDatabase.getInstance(SubActivityReader.this);

        final Intent intent = getIntent();
        uri = intent.getData();

        Log.d(TAG, "onCreate: " + uri.toString());

        /*
        Check: savedInstance ? Shortcut ? from MainActivity ?
         */

        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_VIEW)) {
                Document document = new Document(getFilename(uri), uri.toString());
                new InsertTask(this, document).execute();
            }
        }
        if (savedInstanceState != null) {
            pagerPosition = savedInstanceState.getInt("CURRENT_PAGE");
            uri = Uri.parse(savedInstanceState.getString("CURRENT_PFD"));

            Log.d(TAG, "onCreate: SaveInstances: " + pagerPosition + ", " + uri);
        }
        if (uri == null) {
            Log.d(TAG, "onCreate: URI was null");
            return;
        }

        initializeViews();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: " + Arrays.toString(permissions));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + resultCode + ", " + data);
    }

    /*
        Instantiate viewPager etc
         */
    public void initializeViews(){
        Log.d(TAG, "initializeReader: ");

        tvPageCounter = findViewById(R.id.tv_pageCounter);
        mPager = findViewById(R.id.pager);

        mPager.setClipToPadding(false);
        mPager.setClipChildren(false);
        mPager.setOffscreenPageLimit(1);

        //mPager.setVerticalFadingEdgeEnabled(true);
        //mPager.setVerticalScrollBarEnabled(true);

        viewPagerOnPageChangeCallback= new viewPagerOnPageChangeCallback();
        mPager.registerOnPageChangeCallback(viewPagerOnPageChangeCallback);



        mPager.setPageTransformer(new ZoomOutPageTransformer());

        //VerticalItemDecoration itemDecoration = new VerticalItemDecoration(this, R.dimen.verticalMargin);
        //mPager.addItemDecoration(itemDecoration);

        parcelFileDescriptor = getPath(uri);

        try {
            Os.lseek(parcelFileDescriptor.getFileDescriptor(), 0, OsConstants.SEEK_SET);
            long size = Os.fstat(parcelFileDescriptor .getFileDescriptor()).st_size;
        } catch (ErrnoException ee) {
            throw new IllegalArgumentException("file descriptor not seekable");
        }
        pageAdapter = new PageAdapter(SubActivityReader.this, parcelFileDescriptor);
        mPager.setAdapter(pageAdapter);
        mPager.setCurrentItem(pagerPosition);
    }

    /**
     * @param uri Path to PDF file
     * @return ParcelFileDescriptor of PDF to render
     */
    public ParcelFileDescriptor getPath(Uri uri) {
        try {
            parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            Log.d(TAG, "getPath: " + parcelFileDescriptor);
            return parcelFileDescriptor;
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
            return null;
        }
    }

    // Toggle Page scrolling on/off when Page is zoomed
    public static void toggleZoomed(Boolean zoomed){
        mPager.setUserInputEnabled(!zoomed);
    }


    // Page Counter: receives update when ViewPager is scrolled
    public class viewPagerOnPageChangeCallback extends ViewPager2.OnPageChangeCallback {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            tvPageCounter.setText(new StringBuilder().append(position + 1).append("/").append(pageAdapter.getItemCount()).toString());
        }
    }


    public String getFilename(Uri uri){
        /*
         * Get the file's content URI from the incoming Intent,
         * then query the server app to get the file's display name
         * and size.
         */

        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         * move to the first row in the Cursor, get the data,
         * and display it.
         */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();

        //nameView.setText(returnCursor.getString(nameIndex));
        //sizeView.setText(Long.toString(returnCursor.getLong(sizeIndex)));
        return returnCursor.getString(nameIndex);
    }

    /**
     * InsertTask: put recently opened document to database
     */
    private static class InsertTask extends AsyncTask<Void,Void,Boolean> {

        private WeakReference<SubActivityReader> activityReference;
        private Document document;

        // only retain a weak reference to the activity
        InsertTask(SubActivityReader context, Document document) {
            activityReference = new WeakReference<>(context);
            this.document = document;
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected Boolean doInBackground(Void... objs) {
            Log.d("InsertTask", "doInBackground: " + document.toString());
            activityReference.get().documentDatabase.getDocumentDao().insertOrUpdate(document);
            return true;
        }

        // onPostExecute runs on main thread
/*        @Override
        protected void onPostExecute(Boolean bool) {
            if (bool){
                activityReference.get().setResult(document,1);
            }
        }*/
    }




    /*
    UI & Runtime Operations
     */

    @Override
    public void onBackPressed() {

        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            // destroyElements();
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("CURRENT_PAGE", mPager.getCurrentItem());
        outState.putString("CURRENT_PFD", uri.toString());
        super.onSaveInstanceState(outState);
    }


    private void destroyElements(){
        Log.d(TAG, "[X] DESTROY ELEMENTS: ");
        mPager.unregisterOnPageChangeCallback(viewPagerOnPageChangeCallback);
        Log.d(TAG, "[X] onDestroy: ...recycling..." );
        try {
            if (parcelFileDescriptor != null)
            parcelFileDescriptor.close();
            Log.d(TAG, "[X] onDestroy: " + "parcelFileDescriptor closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (pageAdapter != null)
        pageAdapter.clear();
        Log.d(TAG, "[X] onDestroy: " + "pageAdapter cleared");

        //InsertTask.cancel(true);
    }

    @Override
    protected void onDestroy() {
        destroyElements();
        super.onDestroy();
    }
}