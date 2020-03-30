package io.enotes.sdk.repository.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import io.enotes.sdk.repository.db.entity.Mfr;


@Dao
public interface MfrDao {
    @Query("select * from mfr where vendorName = :vendorName and batch = :batch LIMIT 1")
    LiveData<Mfr> getMfr(String vendorName, String batch);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Mfr mfr);

    @Query("DELETE FROM mfr where id = :id")
    int delete(long id);

    @Delete
    int delete(Mfr mfr);

    @Update
    int update(Mfr mfr);
}
