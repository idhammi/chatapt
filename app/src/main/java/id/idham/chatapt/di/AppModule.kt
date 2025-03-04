package id.idham.chatapt.di

import id.idham.chatapt.BuildConfig
import id.idham.chatapt.data.Repository
import id.idham.chatapt.data.RepositoryImpl
import id.idham.chatapt.ui.chat.ChatViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<Repository> {
        RepositoryImpl(BuildConfig.API_KEY, get(), get())
    }

    viewModelOf(::ChatViewModel)
}