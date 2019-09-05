package com.evasler.digipediamasteredition;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Attribute.class, Family.class, Level.class, Type.class, Digimon.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MyDao myDao();
    private static AppDatabase INSTANCE;
    private static final String DB_NAME = "DigipediaMasterEdition.db";

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NotNull SupportSQLiteDatabase database) {
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DB_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
