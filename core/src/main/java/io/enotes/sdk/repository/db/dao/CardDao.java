package io.enotes.sdk.repository.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.enotes.sdk.repository.db.entity.Card;


@Dao
public interface CardDao {
    @Query("select * from card order by updateTime desc")
    LiveData<List<Card>> getCardList();

    @Query("select * from card where id = :coinId LIMIT 1")
    LiveData<Card> getCard(long coinId);

    @Query("select * from card where currencyPubKey = :currencyPubKey LIMIT 1")
    Card getCardByPubKey(String currencyPubKey);

    @Query("select * from card where txId = :txId LIMIT 1")
    Card getCardByTxId(String txId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Card card);

    @Query("DELETE FROM card where id = :cardId")
    int delete(long cardId);

    @Delete
    int delete(Card card);

    @Update
    int update(Card card);

}
