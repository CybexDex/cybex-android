package com.cybex.database;

import android.content.Context;

import com.cybex.database.dao.DaoMaster;
import com.cybex.database.dao.DaoSession;

import org.greenrobot.greendao.database.Database;

public class DBManager {

    private static final String PASSWORD = "cybex-android";
    private static final String TABLE_NAME = "cybex.db";

    private static DBManager mDBManager;
    private static DBProvider mDbProvider;
    private static DaoSession mDaoSession;

    private DBManager(Context context) {
        if(mDaoSession == null){
            DBOpenHelper dbOpenHelper = new DBOpenHelper(context, TABLE_NAME, null);
            Database database = dbOpenHelper.getWritableDb();
            DaoMaster daoMaster = new DaoMaster(database);
            mDaoSession = daoMaster.newSession();
        }
    }

    public static DBManager getInstance(Context context){
        if (mDBManager == null) {
            synchronized (DBProvider.class){
                if(mDBManager == null){
                    mDBManager = new DBManager(context.getApplicationContext());
                }
            }
        }
        return mDBManager;
    }

    public static DBProvider getDbProvider(Context context){
        if (mDbProvider == null) {
            synchronized (DBProvider.class){
                if(mDbProvider == null){
                    mDbProvider = new DBProviderImpl(getInstance(context.getApplicationContext()).getDaoSession());
                }
            }
        }
        return mDbProvider;
    }

    private DaoSession getDaoSession(){
        return mDaoSession;
    }
}
