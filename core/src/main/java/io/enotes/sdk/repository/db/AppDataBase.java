package io.enotes.sdk.repository.db;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import io.enotes.sdk.repository.db.converter.ValueConverter;
import io.enotes.sdk.repository.db.dao.CardDao;
import io.enotes.sdk.repository.db.dao.MfrDao;
import io.enotes.sdk.repository.db.entity.Card;
import io.enotes.sdk.repository.db.entity.Mfr;

@Database(entities = {Card.class, Mfr.class}, version = 4, exportSchema = false)
@TypeConverters({ValueConverter.class})
public abstract class AppDataBase extends RoomDatabase {
    abstract public CardDao getCardDao();

    abstract public MfrDao getMfrDao();

    public static AppDataBase init(Context application) {
        return Room.databaseBuilder(application, AppDataBase.class, "eNotes.db")
                .addMigrations(MIGRATION_1_2).addMigrations(MIGRATION_3_4).build();
    }

    static final Migration MIGRATION_1_2 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Card "
                    + " ADD COLUMN tokenProtocol TEXT");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Card "
                    + " ADD COLUMN account TEXT");
        }
    };
}

