package com.cybex.provider.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.cybex.provider.db.dao.DaoMaster;

import org.greenrobot.greendao.database.Database;

public class DBOpenHelper extends DaoMaster.OpenHelper{

    public DBOpenHelper(Context context, String name) {
        super(context, name);
    }

    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
    }
}
