package com.unitconverter.app.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room Database for SmartConvert AI
 * 
 * Manages local data persistence:
 * - Conversion history
 * - Favorites
 * - User preferences (future)
 */
@Database(entities = {ConversionHistoryEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "smartconvert_db";
    private static volatile AppDatabase INSTANCE;
    
    /**
     * Get DAO for history operations
     */
    public abstract ConversionHistoryDao historyDao();
    
    /**
     * Singleton pattern - ensures only one database instance
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    // Uncomment for debugging (allows main thread queries)
                    // .allowMainThreadQueries()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
