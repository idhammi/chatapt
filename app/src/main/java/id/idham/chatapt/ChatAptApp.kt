package id.idham.chatapt

import android.app.Application
import androidx.room.Room
import id.idham.chatapt.data.Repository
import id.idham.chatapt.data.RepositoryImpl
import id.idham.chatapt.database.AppDatabase
import id.idham.chatapt.network.ApiService
import id.idham.chatapt.ui.chat.ChatViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatAptApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(module {
                single {
                    Retrofit.Builder()
                        .baseUrl("https://generativelanguage.googleapis.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }

                single {
                    val retrofit: Retrofit = get()
                    retrofit.create(ApiService::class.java)
                }

                single {
                    Room.databaseBuilder(
                        this@ChatAptApp,
                        AppDatabase::class.java,
                        "chatapt.db"
                    )
                }

                single {
                    val apiKey: String = BuildConfig.API_KEY
                    val apiService: ApiService = get()
                    RepositoryImpl(apiKey, apiService)
                } bind Repository::class

                viewModelOf(::ChatViewModel)
            })
        }
    }
}
