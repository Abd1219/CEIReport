package com.abdapps.ceireport.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.abdapps.ceireport.data.model.Report

@Database(
    entities = [Report::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ReportDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile
        private var INSTANCE: ReportDatabase? = null

        fun getDatabase(context: Context): ReportDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReportDatabase::class.java,
                    "cei_report_database"
                )
                    // En desarrollo: si hay un cambio de versión sin migración definida,
                    // recrea la base de datos en lugar de crashear.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
