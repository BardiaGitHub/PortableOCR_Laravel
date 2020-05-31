package com.bardia.pocr.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "object")
public class TextObject {

    @NonNull
    @PrimaryKey(autoGenerate = false)
    public String id;

    @ColumnInfo(name = "userid")
    public int userid;

    @ColumnInfo(name = "text")
    public String text;

    @ColumnInfo(name = "date")
    public String date;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public TextObject(@NonNull String id, int userid, String text, String date) {
        this.id = id;
        this.userid = userid;
        this.text = text;
        this.date = date;
    }

    @Override
    public String toString() {
        return "TextObject{" +
                "id='" + id + '\'' +
                ", userid=" + userid +
                ", text='" + text + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
