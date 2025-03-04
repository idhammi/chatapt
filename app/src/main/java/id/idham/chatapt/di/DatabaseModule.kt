package id.idham.chatapt.di

import androidx.room.Room
import id.idham.chatapt.database.AppDatabase
import id.idham.chatapt.database.ChatMessageDao
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            "chatapt.db"
        ).build()
    }

    single<ChatMessageDao> {
        get<AppDatabase>().chatMessageDao()
    }
}
