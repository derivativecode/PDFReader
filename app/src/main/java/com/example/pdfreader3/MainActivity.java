package com.example.pdfreader3;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CustomAdapter.ItemClickListener {

    private final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 42;
    private final int PICK_PDF_FILE = 69;

    private DocumentDatabase documentDatabase;
    private List<Document> documents;
    private Document document;

    private RecyclerView recyclerView;
    private CustomAdapter adapter;


    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        documents = new ArrayList<>();

        /*
        Check Permission
        */
        if (!checkSelfPermission()) {
            finish();
        } else {
            initializeViews();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        initializeViews();
    }

    /**
     * Check Permission to read file from storage
     * Quits application if permission NOT granted to avoid errors
    */
    public boolean checkSelfPermission() {
        boolean flag;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            // No Permission; finish!
            //finish();
            flag = false;
        } else {
            flag = true;
        }
        return flag;
    }


    public void initializeViews(){
        Log.d(TAG, "initializeViews: ");

        // Instantiate Database, fetch entries
        documentDatabase = DocumentDatabase.getInstance(MainActivity.this);
        new RetrieveTask(this).execute();

        // Bind Views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tvStart = findViewById(R.id.tv_Start);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        //adapter = new CustomAdapter(dataSet);
        // recyclerView.setAdapter(adapter);
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    /**
     * RetrieveTask: fetch recent documents from database
     * @return List of Documents
     */
    private static class RetrieveTask extends AsyncTask<Void, Void, List<Document>> {

        private WeakReference<MainActivity> activityReference;

        // only retain a weak reference to the activity
        RetrieveTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }


        @Override
        protected List<Document> doInBackground(Void... voids) {
            if (activityReference.get() != null)
                return activityReference.get().documentDatabase.getDocumentDao().getAll();
            else
                return null;
        }

        @Override
        protected void onPostExecute(List<Document> documents) {
            if (documents != null && documents.size() > 0) {

                activityReference.get().documents = documents;
                Log.d("RetrieveTask", "onPostExecute: " + documents.size());
                // create and set the adapter on RecyclerView instance to display list
                activityReference.get().adapter = new CustomAdapter(activityReference.get(), documents);
                activityReference.get().recyclerView.setAdapter(activityReference.get().adapter);
                activityReference.get().adapter.setClickListener(activityReference.get());
            }
        }
    }


    /**
     * InsertTask: put recently opened document to database
     */
    private static class InsertTask extends AsyncTask<Void,Void,Boolean> {

        private WeakReference<MainActivity> activityReference;
        private Document document;

        // only retain a weak reference to the activity
        InsertTask(MainActivity context, Document document) {
            activityReference = new WeakReference<>(context);
            this.document = document;
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected Boolean doInBackground(Void... objs) {
            Log.d("InsertTask", "doInBackground: " + document.toString());
            Log.d("InsertTask", "doInBackground: " + document.getId() + ", " + document.getFilename());
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



    private void openFile() {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            // intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

            startActivityForResult(intent, PICK_PDF_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PICK_PDF_FILE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();

                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Insert into database
                document = new Document(getFilename(uri), uri.toString());
                new InsertTask(MainActivity.this, document).execute();

                startReader(uri);
            }
        }
    }


    /**
    * Starts Browser or File Dialog Activity:
    * @return path to selected file, which is passed to ReaderActivity
     */
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                // Handle the returned Uri
                if(uri != null){
                    Log.d(TAG, "selected File: " + uri + ", filename:" + getFilename(uri));

                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    // Insert into database
                    document = new Document(getFilename(uri), uri.toString());
                    new InsertTask(MainActivity.this, document).execute();

                    startReader(uri);
                }
            });


    /**
     * Start Reader Activity Intent
     * @param uri: path to PDF to open
     */
    public void startReader(Uri uri) {
        Intent intent = new Intent(this, SubActivityReader.class);

        // pass uri / filepath / fd / pfd
        intent.setData(uri);
        startActivity(intent);
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

    /*
    Menu Toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // TODO: Settings menu
                return true;

            case R.id.action_openFile:
                //mGetContent.launch("application/pdf");
                openFile();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }



    // RecyclerView onItemClick-Event
    @Override
    public void onItemClick(View view, int position) {

        document = new Document(documents.get(position).getFilename(), documents.get(position).getFilepath());
        new InsertTask(MainActivity.this, document).execute();

        Uri uri = Uri.parse(documents.get(position).getFilepath());

        if (uri != null ) startReader(uri);
        else Toast.makeText(this, "Error opening file", Toast.LENGTH_SHORT).show();
    }
}