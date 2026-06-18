package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ComputerTraining
import com.example.data.model.Notice
import com.example.data.model.ServiceApplication
import com.example.data.model.UserAccount

@Database(
    entities = [ServiceApplication::class, Notice::class, ComputerTraining::class, UserAccount::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceApplicationDao(): ServiceApplicationDao
    abstract fun noticeDao(): NoticeDao
    abstract fun computerTrainingDao(): ComputerTrainingDao
    abstract fun userAccountDao(): UserAccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "udc_service_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
