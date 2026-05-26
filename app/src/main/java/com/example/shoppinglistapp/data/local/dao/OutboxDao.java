package com.example.shoppinglistapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.shoppinglistapp.data.local.entity.OutboxEntity;
import java.util.List;

@Dao
public interface OutboxDao {
    @Insert
    void insert(OutboxEntity entity);

    @Query("SELECT * FROM outbox ORDER BY created_at ASC")
    List<OutboxEntity> getAllPending();

    @Delete
    void delete(OutboxEntity entity);

    @Query("DELETE FROM outbox WHERE object_id = :objectId AND type = :type")
    void deleteForObject(String type, long objectId);

    @Query("SELECT COUNT(*) FROM outbox WHERE list_id = :listId")
    int countPendingForList(long listId);
}
