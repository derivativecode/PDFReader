package com.example.pdfreader3;

import android.util.Log;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public abstract class DocumentDao {


        @Insert
        public abstract long actualInsert(Document document);

        public long insert(Document document) {
                document.setCreatedAt(new Date());
                document.setUpdatedAt(new Date());
                Log.d("DocumentDao", "insert: " + document.toString());
                return actualInsert(document);
        }


        @Update
        public abstract void actualUpdate(Document document);

        public void update(Document document) {
                Log.d("DocumentDao", "update: " + document.getUpdatedAt());
                document.setUpdatedAt(new Date());
                Log.d("DocumentDao", "update: " + document.getUpdatedAt());
                actualUpdate(document);
        }

        /*
        Only keep unique filepath in DB
         */
        @Query("SELECT * from documents WHERE filepath = :filepath")
        abstract List<Document> getItemByFilepath(String filepath);

        @Query("UPDATE documents SET updated_at = CURRENT_TIMESTAMP WHERE id = :id")
        abstract void updateQuantity(long id);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertOrUpdate(Document document) {
                List<Document> itemsFromDB = getItemByFilepath(document.getFilepath());
                if (itemsFromDB.isEmpty()) {
                        Log.d("DocumentDao", "insertOrUpdate: INSERT");
                        insert(document);
                } else {
                        Log.d("DocumentDao", "insertOrUpdate: UPDATE");
                        Log.d("DocumentDao", "insertOrUpdate: " + document.toString());
                        // updateQuantity(document.getId());
                        update(document);
                }
        }



        @Delete
        public abstract void delete(Document document);

        @Delete
        public abstract void deleteAll(List<Document> documents);


        @Query("select * from documents order by updated_at desc")
        public abstract List<Document> getAll();

//        @Query("SELECT * from documents WHERE filepath = filepath")
//        public abstract List<Document> getItemByPath(String filepath);

}
