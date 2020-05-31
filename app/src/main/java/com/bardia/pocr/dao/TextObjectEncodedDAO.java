package com.bardia.pocr.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bardia.pocr.model.TextObject;

import java.util.List;

@Dao
public interface TextObjectEncodedDAO {

    @Query("Select * from object")
    List<TextObject> getEncodedObjectsList();

    @Query("Select * from object where id = :id")
    TextObject getEncodedObject(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEncodedObject(TextObject objectEncodedOffline);

    @Update
    void updateObjectEncoded(TextObject objectEncoded);

    @Delete
    void deleteObjectEncoded(TextObject objectEncoded);

    @Query("Delete from object")
    void deleteAllObjects();
}
