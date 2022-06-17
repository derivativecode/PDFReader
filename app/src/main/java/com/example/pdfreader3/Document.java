package com.example.pdfreader3;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;


@Entity(tableName = "documents")
public class Document {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "filename")
    private String filename;

    @ColumnInfo(name = "filepath")
    private String filepath;

    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    @TypeConverters(DateConverter.class)
    private Date createdAt;

    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    @TypeConverters(DateConverter.class)
    private Date updatedAt;


    /*
    Constructor
    */

    public Document(String filename, String filepath) {
        this.filename = filename;
        this.filepath = filepath;
    }

    public Document(long id, String filename, String filepath, Date created_at, Date updated_at ) {
        this.id = id;
        this.filename = filename;
        this.filepath = filepath;
        this.createdAt = created_at;
        this.updatedAt = updated_at;
    }


    /*
    Getters & Setters
    */

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }


    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", filepath='" + filepath + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
