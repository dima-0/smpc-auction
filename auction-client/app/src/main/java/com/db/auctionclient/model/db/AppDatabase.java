package com.db.auctionclient.model.db;

import android.app.Application;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.db.auctionclient.model.entities.Auction;
import com.db.auctionclient.model.entities.AuctionTask;
import com.db.auctionclient.model.entities.Converters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Provides access to the database.
 */
@Database(entities = {AuctionTask.class, Auction.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    /** Execution service for database related operations (with thread pool size of 2).*/
    public static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    /**
     * Provides an instance of {@link AuctionTaskDao}.
     * @return an instance of {@link AuctionTaskDao}.
     */
    public abstract AuctionTaskDao auctionTaskDao();

    /**
     * Provides an instance of {@link AuctionDao}.
     * @return an instance of {@link AuctionDao}.
     */
    public abstract AuctionDao auctionDao();

    /**
     * Provides an instance of {@link AppDatabase}. The instance is initialized only one time,
     * when this method is called for the first time.
     * @param applicationContext context of the application.
     * @return an instance of {@link AppDatabase}.
     */
    public static AppDatabase getInstance(Application applicationContext){
        if(instance == null){
            synchronized (AppDatabase.class){
                instance = Room.databaseBuilder(applicationContext, AppDatabase.class, "app-database")
                        .build();
            }
        }
        return instance;
    }
}
