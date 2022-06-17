package com.example.pdfreader3;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = { Document.class }, version = 1)
public abstract class DocumentDatabase extends RoomDatabase {

    public abstract DocumentDao getDocumentDao();

    private static DocumentDatabase documentDB;

    public static DocumentDatabase getInstance(Context context) {
        if (null == documentDB) {
            documentDB = buildDatabaseInstance(context);
        }
        return documentDB;
    }

    private static DocumentDatabase buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(context,
                DocumentDatabase.class, "documentDB")
                .allowMainThreadQueries().build();
    }

    public void cleanUp(){
        documentDB = null;
    }

}
