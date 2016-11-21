package com.aqilix.mobile.aqilix.orm.helper;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.aqilix.mobile.aqilix.orm.model.PairData;
import com.aqilix.mobile.aqilix.R;

import java.sql.SQLException;

/**
 * Created by dolly on 11/19/16.
 */
public class PairDataOpenDB extends OrmLiteSqliteOpenHelper{

    private static final String DATABASE_NAME = "aqilix_db";

    private static final int DATABASE_VERSION = 1;

    /**
     * The data access object used to interact with the Sqlite database to do C.R.U.D operations.
     */
    private Dao<PairData, String> pairDataDao;

    public PairDataOpenDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION,
                /**
                 * R.raw.ormlite_config is a reference to the ormlite_config.txt file in the
                 * /res/raw/ directory of this project
                 * */
                R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {

            /**
             * creates the PairData database table
             */
            TableUtils.createTable(connectionSource, PairData.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            /**
             * Recreates the database when onUpgrade is called by the framework
             */
            TableUtils.dropTable(connectionSource, PairData.class, false);
            onCreate(database, connectionSource);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    * Returns an instance of the data access object
    * @return
    * @throws SQLException
    */
    public Dao<PairData, String> getDao() throws SQLException {
        if(pairDataDao == null) {
            pairDataDao = getDao(PairData.class);
        }

        return pairDataDao;
    }

    public String getValue(String key) {
        try {
            Dao<PairData, String> pairDataDao = getDao(PairData.class);
            return pairDataDao.queryForId(key).getValue();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
