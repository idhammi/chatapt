package id.idham.chatapt

import android.app.Application
import id.idham.chatapt.di.appModule
import id.idham.chatapt.di.databaseModule
import id.idham.chatapt.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ChatAptApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ChatAptApp)
            modules(
                networkModule,
                databaseModule,
                appModule
            )
        }
    }
}
