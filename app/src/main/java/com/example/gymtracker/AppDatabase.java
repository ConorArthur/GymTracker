package com.example.gymtracker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {TypeEntity.class, NameEntity.class, DataEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract GymTrackerDao GymTrackerDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "gym_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                GymTrackerDao dao = INSTANCE.GymTrackerDao();
                dao.deleteAllData();


                TypeEntity cardio = new TypeEntity("Cardio");
                long typeIdCardio = dao.insert(cardio);
                TypeEntity weights = new TypeEntity("Weights");
                long typeIdWeights = dao.insert(weights);

            });
        }
    };
}